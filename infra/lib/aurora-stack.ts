import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as rds from 'aws-cdk-lib/aws-rds';

export class AuroraStack extends cdk.NestedStack {
  readonly endpoint: string;

  constructor(
    scope: Construct,
    id: string,
    vpc: ec2.Vpc,
    clientSecurityGroup: ec2.SecurityGroup,
    props?: cdk.StackProps
  ) {
    super(scope, id, props);

    const auroraSecurityGroup = new ec2.SecurityGroup(this, 'AuroraSecurityGroup', {
      vpc
    });

    const dbSubnetGroupName = new rds.CfnDBSubnetGroup(this, 'MySubnetGroup', {
      dbSubnetGroupDescription: 'Subnet group for Aurora Serverless',
      subnetIds: vpc.selectSubnets({ subnetType: ec2.SubnetType.PRIVATE_ISOLATED }).subnetIds
    }).ref;

    // Cfn tier allows to attach snapshots
    const baseAuroraProps: rds.CfnDBClusterProps = {
      databaseName: process.env.DB_NAME!,
      dbClusterIdentifier: 'aurora-pg-audio-streamer-crates',
      dbSubnetGroupName,
      deletionProtection: true,
      engine: 'aurora-postgresql',
      engineMode: 'serverless',
      engineVersion: rds.AuroraPostgresEngineVersion.VER_13_10.auroraPostgresMajorVersion,
      masterUsername: process.env.DB_USERNAME!,
      masterUserPassword: process.env.DB_PASSWORD!,
      scalingConfiguration: {
        autoPause: true,
        minCapacity: rds.AuroraCapacityUnit.ACU_4,
        maxCapacity: rds.AuroraCapacityUnit.ACU_16,
        secondsUntilAutoPause: 5 * 60
      },
      vpcSecurityGroupIds: [auroraSecurityGroup.securityGroupId]
    };

    const auroraProps: rds.CfnDBClusterProps = process.env.DB_SNAPSHOT_ID
      ? {
          ...baseAuroraProps,
          snapshotIdentifier: process.env.DB_SNAPSHOT_ID
        }
      : baseAuroraProps;

    const auroraCluster = new rds.CfnDBCluster(this, 'MyAuroraCluster', auroraProps);

    this.endpoint = auroraCluster.attrEndpointAddress;

    auroraSecurityGroup.addIngressRule(
      clientSecurityGroup,
      ec2.Port.tcp(5432),
      'Aurora to accept conection from Crates service'
    );
  }
}
