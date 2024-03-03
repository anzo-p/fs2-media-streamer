import * as cdk from 'aws-cdk-lib';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import { Construct } from 'constructs';

export class EcsClusterStack extends cdk.NestedStack {
  readonly ecsCluster: ecs.Cluster;
  readonly cratesRepositoryName: string = 'audio-streamer-crates';

  constructor(scope: Construct, id: string, vpc: ec2.Vpc, props?: cdk.NestedStackProps) {
    super(scope, id, props);

    this.ecsCluster = new ecs.Cluster(this, 'AudioStreamerCluster', {
      vpc
    });
  }
}
