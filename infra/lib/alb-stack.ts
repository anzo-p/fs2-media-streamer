import * as cdk from 'aws-cdk-lib';
import * as acm from 'aws-cdk-lib/aws-certificatemanager';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as elbv2 from 'aws-cdk-lib/aws-elasticloadbalancingv2';
import * as route53 from 'aws-cdk-lib/aws-route53';
import * as targets from 'aws-cdk-lib/aws-route53-targets';
import { Construct } from 'constructs';

export class AlbStack extends cdk.NestedStack {
  readonly cratesAlbListener: elbv2.ApplicationListener;

  constructor(scope: Construct, id: string, vpc: ec2.Vpc, props?: cdk.NestedStackProps) {
    super(scope, id, props);

    const hostedZone = route53.HostedZone.fromLookup(this, 'HostedZone', {
      domainName: 'anzop.net'
    });

    const cratesAlbCertificate = acm.Certificate.fromCertificateArn(
      this,
      'Certificate',
      `arn:aws:acm:${process.env.AWS_REGION}:${process.env.AWS_ACCOUNT}:certificate/${process.env.CRATES_ALB_CERT}`
    );

    const cratesAlb = new elbv2.ApplicationLoadBalancer(this, 'CratesAlb', {
      vpc,
      internetFacing: true
    });

    new route53.ARecord(this, 'CratesAlbAliasRecord', {
      zone: hostedZone,
      recordName: `${process.env.CRATES_SUBDOMAIN}`,
      target: route53.RecordTarget.fromAlias(new targets.LoadBalancerTarget(cratesAlb))
    });

    this.cratesAlbListener = cratesAlb.addListener('CratesAlbListener', {
      port: 443,
      protocol: elbv2.ApplicationProtocol.HTTPS,
      certificates: [cratesAlbCertificate]
    });
  }
}
