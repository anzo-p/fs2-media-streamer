#!/usr/bin/env node
import "source-map-support/register";
import * as cdk from "aws-cdk-lib";
import { InfraStack } from "../lib/infra-stack";
import * as dotenv from "dotenv";

dotenv.config();

const app = new cdk.App();
new InfraStack(app, "InfraStack", {
  env: {
    account: process.env.AWS_ACCOUNT,
    region: process.env.AWS_REGION,
  },
});
