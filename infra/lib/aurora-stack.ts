import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as rds from 'aws-cdk-lib/aws-rds';

export class AuroraStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const vpc = new ec2.Vpc(this, 'AudioStreamerVpc', {
      maxAzs: 2,
      subnetConfiguration: [
        {
          name: 'public',
          subnetType: ec2.SubnetType.PUBLIC
        },
        {
          name: 'isolated',
          subnetType: ec2.SubnetType.PRIVATE_ISOLATED
        }
      ],
      natGateways: 0
    });

    const auroraSg = new ec2.SecurityGroup(this, 'AuroraSecurityGroup', {
      vpc
    });

    const credentials = rds.Credentials.fromPassword(
      process.env.DB_USERNAME!,
      cdk.SecretValue.unsafePlainText(process.env.DB_PASSWORD!)
    );

    new rds.ServerlessCluster(this, 'AudioStreamerAuroraPostgres', {
      credentials,
      engine: rds.DatabaseClusterEngine.auroraPostgres({
        version: rds.AuroraPostgresEngineVersion.VER_13_10
      }),
      defaultDatabaseName: `${process.env.DB_NAME}`,
      removalPolicy: cdk.RemovalPolicy.RETAIN,
      scaling: {
        autoPause: cdk.Duration.minutes(5),
        minCapacity: rds.AuroraCapacityUnit.ACU_8,
        maxCapacity: rds.AuroraCapacityUnit.ACU_32
      },
      securityGroups: [auroraSg],
      vpc,
      vpcSubnets: {
        subnetType: ec2.SubnetType.PRIVATE_ISOLATED
      }
    });

    new cdk.CfnOutput(this, 'ExportedVpcId', {
      value: vpc.vpcId
    });

    new cdk.CfnOutput(this, 'ExportedAuroraSecurityGroupId', {
      value: auroraSg.securityGroupId
    });
  }
}
