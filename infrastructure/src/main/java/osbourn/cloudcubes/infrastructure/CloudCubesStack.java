package osbourn.cloudcubes.infrastructure;

import osbourn.cloudcubes.core.constructs.InfrastructureConfiguration;
import osbourn.cloudcubes.core.constructs.InfrastructureConfiguration.InfrastructureSetting;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.deployment.BucketDeployment;
import software.amazon.awscdk.services.s3.deployment.Source;
import software.constructs.Construct;

import java.util.*;

public class CloudCubesStack extends Stack {
    public CloudCubesStack(final Construct parent, final String name) {
        super(parent, name);

        // Create the DynamoDB table that stores the data for the servers
        Attribute serverTablePartitionKey = Attribute.builder()
                .name("Id")
                .type(AttributeType.NUMBER)
                .build();
        Table serverTable = Table.Builder.create(this, "ServerTable")
                .removalPolicy(RemovalPolicy.RETAIN)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .partitionKey(serverTablePartitionKey)
                .build();

        // Resources bucket: the contents of the resources folder will be made available as an S3 bucket
        Bucket resourceBucket = Bucket.Builder.create(this, "ResourceBucket")
                .removalPolicy(RemovalPolicy.DESTROY)
                .autoDeleteObjects(true)
                .build();
        BucketDeployment resourceBucketDeployment = BucketDeployment.Builder.create(this, "ResourceBucketDeployment")
                .destinationBucket(resourceBucket)
                .sources(Collections.singletonList(Source.asset("./resources")))
                .build();

        // Create VPC
        Vpc serverVpc = Vpc.Builder.create(this, "ServerVpc")
                // This will force AWS to create public subnets instead of private subnets
                // Without this setting, AWS will create a NAT Gateway for the private subnets, costing $0.045 per hour
                .natGateways(0)
                .build();

        // Create security group used by server instances
        final int sshPort = 22;
        final int minecraftPort = 25565;
        SecurityGroup serverSecurityGroup = SecurityGroup.Builder.create(this, "ServerSecurityGroup")
                .description("Security group for EC2 Instances launched by the CloudCubes application")
                .vpc(serverVpc)
                .allowAllOutbound(true)
                .build();
        Connections connections = serverSecurityGroup.getConnections();
        connections.allowFromAnyIpv4(Port.tcp(minecraftPort), "Allow TCP access to the Minecraft Server");
        connections.allowFromAnyIpv4(Port.udp(minecraftPort), "Allow UDP access to the Minecraft Server");
        connections.allowFromAnyIpv4(Port.tcp(sshPort), "Allows TCP access through SSH");

        // IAM Role for EC2 instances
        Role serverRole = Role.Builder.create(this, "ServerRole")
                .description("Grants EC2 instances permission to access resources from the Cloudcubes stack")
                .assumedBy(ServicePrincipal.Builder.create("ec2.amazonaws.com").build())
                .build();
        serverRole.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName("AmazonSSMManagedInstanceCore"));
        serverTable.grantReadWriteData(serverRole);
        resourceBucket.grantRead(serverRole);
        CfnInstanceProfile serverInstanceProfile = CfnInstanceProfile.Builder.create(this, "ServerInstanceProfile")
                .roles(Collections.singletonList(serverRole.getRoleName()))
                .build();

        // Create InfrastructureConfiguration object to determine environment variables for the lambda functions
        List<String> serverSubnetIds = new ArrayList<>();
        for (ISubnet subnet : serverVpc.getPublicSubnets()) {
            serverSubnetIds.add(subnet.getSubnetId());
        }
        InfrastructureConfiguration ic = new InfrastructureConfiguration();
        ic.setValue(InfrastructureSetting.REGIONASSTRING, "US-EAST-2");
        ic.setValue(InfrastructureSetting.SERVERDATABASENAME, serverTable.getTableName());
        ic.setValue(InfrastructureSetting.RESOURCEBUCKETNAME, resourceBucket.getBucketName());
        ic.setValue(InfrastructureSetting.SERVERROLEID, serverRole.getRoleId());
        ic.setValue(InfrastructureSetting.SERVERINSTANCEPROFILEARN, serverInstanceProfile.getAttrArn());
        ic.setValue(InfrastructureSetting.SERVERSECURITYGROUPID, serverSecurityGroup.getSecurityGroupId());
        ic.setValue(InfrastructureSetting.SERVERVPCID, serverVpc.getVpcId());
        ic.setServerSubnetIds(serverSubnetIds);

        Map<String, String> infrastructureDataMap = ic.toEnvironmentVariableMap();

        // Create the server starter function
        Function serverStarter = Function.Builder.create(this, "ServerStarter")
                .code(Code.fromAsset("lambda/server-starter/build/libs/server-starter-all.jar"))
                .handler("osbourn.cloudcubes.lambda.serverstarter.ServerStarterLambdaHandler")
                .runtime(Runtime.JAVA_11)
                .environment(infrastructureDataMap)
                .timeout(Duration.seconds(30))
                .memorySize(512)
                .build();
        assert serverStarter.getRole() != null;
        serverStarter.getRole().addToPrincipalPolicy(PolicyStatement.Builder.create()
                .effect(Effect.ALLOW)
                .resources(Collections.singletonList("*"))
                .actions(Collections.singletonList("ec2:RequestSpotInstances"))
                .build());
        // serverStarter needs a special permission in order to launch servers with IAM roles
        serverStarter.getRole().addToPrincipalPolicy(PolicyStatement.Builder.create()
                .effect(Effect.ALLOW)
                .resources(Collections.singletonList(serverRole.getRoleArn()))
                .actions(Arrays.asList("iam:GetRole", "iam:PassRole"))
                .build());
        serverTable.grantReadWriteData(serverStarter);
    }
}
