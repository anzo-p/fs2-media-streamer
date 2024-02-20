import * as cdk from "aws-cdk-lib";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as rds from "aws-cdk-lib/aws-rds";
import { Construct } from "constructs";

export class AuroraPostgresStack extends cdk.NestedStack {
  constructor(
    scope: Construct,
    id: string,
    vpc: ec2.Vpc,
    props?: cdk.NestedStackProps
  ) {
    super(scope, id, props);

    const credentials = rds.Credentials.fromPassword(
      process.env.AURORA_USERNAME!,
      cdk.SecretValue.unsafePlainText(process.env.AURORA_PASSWORD!)
    );

    new rds.ServerlessCluster(this, "AudioStreamerAuroraPostgres", {
      credentials,
      engine: rds.DatabaseClusterEngine.auroraPostgres({
        version: rds.AuroraPostgresEngineVersion.VER_13_10,
      }),
      defaultDatabaseName: "audio_streamer",
      removalPolicy: cdk.RemovalPolicy.RETAIN,
      scaling: {
        autoPause: cdk.Duration.minutes(5),
        minCapacity: rds.AuroraCapacityUnit.ACU_8,
        maxCapacity: rds.AuroraCapacityUnit.ACU_32,
      },
      vpc,
      vpcSubnets: {
        subnetType: ec2.SubnetType.PRIVATE_ISOLATED,
      },
    });
  }
}
