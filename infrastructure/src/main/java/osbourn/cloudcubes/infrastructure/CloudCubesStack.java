package osbourn.cloudcubes.infrastructure;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.RemovalPolicy;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;

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
    }
}
