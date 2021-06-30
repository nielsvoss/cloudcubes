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

        InfrastructureData infrastructureData = new InfrastructureData("US-EAST-2", serverTable.getTableName());
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
    }
}
