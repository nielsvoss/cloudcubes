package osbourn.cloudcubes.infrastructure;

import osbourn.cloudcubes.core.constructs.InfrastructureData;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Duration;
import software.amazon.awscdk.core.RemovalPolicy;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.ec2.Connections;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;

import java.util.Map;

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

        // Create VPC
        Vpc serverVpc = Vpc.Builder.create(this, "ServerVpc")
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

        // Create InfrastructureData object to determine environment variables for the lambda functions
        InfrastructureData infrastructureData = InfrastructureData.Builder.create()
                .withRegion("US-EAST-2")
                .withServerDatabaseName(serverTable.getTableName())
                .withServerSecurityGroupName(serverSecurityGroup.getSecurityGroupName())
                .build();
        Map<String, String> infrastructureDataMap = infrastructureData.convertToMap();

        // Create the server starter function
        Function serverStarter = Function.Builder.create(this, "ServerStarter")
                .code(Code.fromAsset("lambda/server-starter/build/libs/server-starter-all.jar"))
                .handler("osbourn.cloudcubes.lambda.serverstarter.ServerStarterLambdaHandler")
                .runtime(Runtime.JAVA_8)
                .environment(infrastructureDataMap)
                .timeout(Duration.seconds(30))
                .memorySize(512)
                .build();

        serverTable.grantReadWriteData(serverStarter);
    }
}
