import * as cdk from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import { Construct } from 'constructs';
import { AlbStack } from './alb-stack';
import { AuroraStack } from './aurora-stack';
import { CratesStack } from './crates-stack';
import { EcsClusterStack } from './ecs-cluster-stack';
import { EcsTaskExecutionRoleStack } from './ecr-exec-task-role';
import { VpcStack } from './vpc-stack';

export class AudioStreamerStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const vpcStack = new VpcStack(this, 'VpcStack');

    // defining aurora sg egress to client app sg at client stack leads to circular deps in CFN template
    const cratesSecurityGroup = new ec2.SecurityGroup(this, 'CratesSecurityGroup', {
      vpc: vpcStack.vpc,
      allowAllOutbound: true
    });

    const auroraStack = new AuroraStack(this, 'AuroraStack', vpcStack.vpc, cratesSecurityGroup);

    const ecsCluster = new EcsClusterStack(this, 'EcsClusterStack', vpcStack.vpc);

    const taskExecRoleStack = new EcsTaskExecutionRoleStack(this, 'EcsTaskExecRoleStack', [
      ecsCluster.cratesRepositoryName
    ]);

    const albStack = new AlbStack(this, 'AlbStack', vpcStack.vpc);

    const cratesStack = new CratesStack(
      this,
      'CratesStack',
      cratesSecurityGroup,
      ecsCluster.ecsCluster,
      taskExecRoleStack.role,
      albStack.cratesAlbListener,
      auroraStack.endpoint
    );
    cratesStack.addDependency(auroraStack);
  }
}
