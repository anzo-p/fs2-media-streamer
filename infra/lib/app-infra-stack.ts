import * as cdk from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import { Construct } from 'constructs';
import { AlbStack } from './alb-stack';
import { CratesStack } from './crates-stack';
import { EcsClusterStack } from './ecs-cluster-stack';
import { EcsTaskExecutionRoleStack } from './ecr-exec-task-role';

export class AppInfraStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const vpc = ec2.Vpc.fromLookup(this, 'Vpc', {
      vpcId: `${process.env.VPC_ID}`
    }) as ec2.Vpc;

    const ecsCluster = new EcsClusterStack(this, 'EcsClusterStack', vpc);

    const taskExecRoleStack = new EcsTaskExecutionRoleStack(this, 'EcsTaskExecRoleStack', [
      ecsCluster.cratesRepositoryName
    ]);

    const albStack = new AlbStack(this, 'AlbStack', vpc);

    new CratesStack(this, 'CratesStack', ecsCluster.ecsCluster, taskExecRoleStack.role, albStack.cratesAlbListener);
  }
}
