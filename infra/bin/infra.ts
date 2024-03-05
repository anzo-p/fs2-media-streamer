#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import * as dotenv from 'dotenv';
import { AudioStreamerStack } from '../lib/audio-streamer-stack';

dotenv.config();

const app = new cdk.App();
new AudioStreamerStack(app, 'AudioStreamerStack', {
  env: {
    account: process.env.AWS_ACCOUNT,
    region: process.env.AWS_REGION
  }
});
