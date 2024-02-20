import * as cdk from "aws-cdk-lib";
import { Construct } from "constructs";
import { VpcStack } from "./vpc-stack";
import { AuroraPostgresStack } from "./aurora-stack";

export class InfraStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const vpcStack = new VpcStack(this, "VpcStack");

    new AuroraPostgresStack(this, "AuroraStack", vpcStack.vpc);
  }
}
