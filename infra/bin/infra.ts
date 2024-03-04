#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import { AuroraStack } from '../lib/aurora-stack';
import { AppInfraStack } from '../lib/app-infra-stack';
import * as dotenv from 'dotenv';

dotenv.config();

const app = new cdk.App();
const env = {
  account: process.env.AWS_ACCOUNT,
  region: process.env.AWS_REGION
};

const targetStack = app.node.tryGetContext('stack');

switch (targetStack) {
  case 'aurora':
    new AuroraStack(app, 'AuroraStack', { env });
    break;
  case 'appinfra':
    new AppInfraStack(app, 'AppInfraStack', { env });
    break;
  default:
    console.error('Error: No valid stack specified for deployment. Please specify a valid stack name.');
    process.exit(1);
}
