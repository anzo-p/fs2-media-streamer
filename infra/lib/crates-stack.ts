import * as cdk from 'aws-cdk-lib';
import * as ecr from 'aws-cdk-lib/aws-ecr';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as elbv2 from 'aws-cdk-lib/aws-elasticloadbalancingv2';
import * as iam from 'aws-cdk-lib/aws-iam';
import { Construct } from 'constructs';

export class CratesStack extends cdk.NestedStack {
  constructor(
    scope: Construct,
    id: string,
    ecsCluster: ecs.Cluster,
    executionRole: iam.Role,
    cratesAlbListener: elbv2.ApplicationListener,
    props?: cdk.StackProps
  ) {
    super(scope, id, props);

    const cratesSecurityGroup = new ec2.SecurityGroup(this, 'CratesSecurityGroup', {
      vpc: ecsCluster.vpc,
      allowAllOutbound: true
    });

    const taskDefinition = new ecs.FargateTaskDefinition(this, 'CratesTaskDefinition', {
      family: 'CratesTaskDefinition',
      executionRole,
      runtimePlatform: {
        operatingSystemFamily: ecs.OperatingSystemFamily.LINUX,
        cpuArchitecture: ecs.CpuArchitecture.ARM64
      },
      memoryLimitMiB: 1024,
      cpu: 512
    });

    const ecrRepository = ecr.Repository.fromRepositoryName(this, 'ECRRepository', 'audio-streamer-crates');

    taskDefinition.addContainer('CratesContainer', {
      image: ecs.ContainerImage.fromEcrRepository(ecrRepository, 'latest'),
      portMappings: [{ protocol: ecs.Protocol.TCP, containerPort: 8080 }],
      memoryLimitMiB: 1024,
      cpu: 512,
      environment: {
        AWS_ACCESS_KEY_ID: `${process.env.AWS_ACCESS_KEY_ID}`,
        AWS_SECRET_ACCESS_KEY: `${process.env.AWS_SECRET_ACCESS_KEY}`,
        AWS_REGION: `${process.env.AWS_REGION}`,
        CORS_ALLOWED_ORIGINS: `${process.env.CORS_ALLOWED_ORIGINS}`,
        DB_URL: `jdbc:postgresql://${process.env.DB_ENDPOINT}:${process.env.DB_PORT}/${process.env.DB_NAME}`,
        DB_USERNAME: `${process.env.DB_USERNAME}`,
        DB_PASSWORD: `${process.env.DB_PASSWORD}`,
        STREAM_CHUNK_SIZE: `${process.env.STREAM_CHUNK_SIZE}`,
        S3_TRACKS_BUCKET_NAME: `${process.env.S3_TRACKS_BUCKET_NAME}`
      },
      logging: ecs.LogDrivers.awsLogs({ streamPrefix: 'crates' })
    });

    const cratesService = new ecs.FargateService(this, 'CratesEcsService', {
      cluster: ecsCluster,
      taskDefinition,
      vpcSubnets: { subnetType: ec2.SubnetType.PUBLIC },
      securityGroups: [cratesSecurityGroup],
      desiredCount: 2,
      assignPublicIp: true
    });

    cratesService.registerLoadBalancerTargets({
      containerName: 'CratesContainer',
      containerPort: 8080,
      newTargetGroupId: 'CratesTargetGroup',
      listener: ecs.ListenerConfig.applicationListener(cratesAlbListener, {
        protocol: elbv2.ApplicationProtocol.HTTP,
        healthCheck: {
          path: '/health',
          interval: cdk.Duration.seconds(30),
          timeout: cdk.Duration.seconds(15),
          healthyThresholdCount: 3,
          unhealthyThresholdCount: 5,
          port: '8080',
          healthyHttpCodes: '200'
        }
      })
    });

    const auroraSecurityGroup = ec2.SecurityGroup.fromSecurityGroupId(
      this,
      'ImportedAuroraSecurityGroup',
      `${process.env.DB_SECURITY_GROUP_ID}`
    );

    auroraSecurityGroup.addIngressRule(
      cratesSecurityGroup,
      ec2.Port.tcp(5432),
      'Aurora to accept conection from Crates service'
    );
  }
}
