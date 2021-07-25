package osbourn.cloudcubes.core.constructs;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Vpc;

/**
 * Retrieves information from an InfrastructureData object and generates AWS SDK objects.
 * For example, it can return objects representing the DynamoDB Table where the server data is stored.
 */
public class InfrastructureConstructor {
    private final InfrastructureData infrastructureData;

    private DynamoDbClient dynamoDBClient = null;
    private Ec2Client ec2Client = null;
    private Vpc serverVpc = null;

    /**
     * Generates an InfrastructureConstructor object from an InfrastructureData object.
     *
     * @param infrastructureData The data to use when generating objects.
     */
    public InfrastructureConstructor(InfrastructureData infrastructureData) {
        this.infrastructureData = infrastructureData;
    }

    /**
     * Get the InfrastructureData object used to construct this object.
     *
     * @return The InfrastructureData object used to construct this object.
     */
    public InfrastructureData getInfrastructureData() {
        return infrastructureData;
    }

    public DynamoDbClient getDynamoDBClient() {
        if (dynamoDBClient == null) {
            dynamoDBClient = DynamoDbClient.builder().region(infrastructureData.getRegion()).build();
        }
        return dynamoDBClient;
    }

    public Ec2Client ec2Client() {
        if (ec2Client == null) {
            ec2Client = Ec2Client.builder().region(infrastructureData.getRegion()).build();
        }
        return ec2Client;
    }

    public Vpc getServerVpc() {
        if (serverVpc == null) {
            serverVpc = Vpc.builder().vpcId(infrastructureData.getServerVpcId()).build();
        }
        return serverVpc;
    }
}
