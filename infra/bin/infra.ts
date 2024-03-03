#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import { AuroraStack } from '../lib/aurora-stack';
import { AppInfraStack } from '../lib/app-infra-stack';
import * as dotenv from 'dotenv';

dotenv.config();

let deployed = false;

const app = new cdk.App();
const args = process.argv.slice(2);
const env = {
  account: process.env.AWS_ACCOUNT,
  region: process.env.AWS_REGION
};

const stacks: { [key: string]: () => void } = {
  StackDB: () => new AuroraStack(app, 'AuroraStack', { env }),
  StackApp: () => new AppInfraStack(app, 'InfraStack', { env })
};

Object.keys(stacks).forEach((stackName) => {
  if (args.includes(stackName)) {
    stacks[stackName]();
    deployed = true;
  }
});

if (!deployed) {
  console.error('Error: No stack specified for deployment. Please specify a stack name.');
  process.exit(1);
}
