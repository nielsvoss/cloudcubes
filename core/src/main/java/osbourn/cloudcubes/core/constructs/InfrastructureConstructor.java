package osbourn.cloudcubes.core.constructs;

import osbourn.cloudcubes.core.constructs.InfrastructureConfiguration.InfrastructureSetting;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Vpc;

/**
 * Retrieves information from an InfrastructureData object and generates AWS SDK objects.
 * For example, it can return objects representing the DynamoDB Table where the server data is stored.
 */
public class InfrastructureConstructor {
    private final InfrastructureConfiguration infrastructureConfiguration;

    private DynamoDbClient dynamoDBClient = null;
    private Ec2Client ec2Client = null;
    private Vpc serverVpc = null;

    /**
     * Generates an InfrastructureConstructor object from an InfrastructureData object.
     *
     * @param infrastructureData The data to use when generating objects.
     */
    public InfrastructureConstructor(InfrastructureConfiguration infrastructureConfiguration) {
        this.infrastructureConfiguration = infrastructureConfiguration;
    }

    /**
     * Get the InfrastructureConfiguration object used to construct this object.
     *
     * @return The InfrastructureConfiguration object used to construct this object.
     */
    public InfrastructureConfiguration getInfrastructureConfiguration() {
        return infrastructureConfiguration;
    }

    public DynamoDbClient getDynamoDBClient() {
        if (dynamoDBClient == null) {
            dynamoDBClient = DynamoDbClient.builder().region(infrastructureConfiguration.getRegion()).build();
        }
        return dynamoDBClient;
    }

    public Ec2Client getEc2Client() {
        if (ec2Client == null) {
            ec2Client = Ec2Client.builder().region(infrastructureConfiguration.getRegion()).build();
        }
        return ec2Client;
    }

    public Vpc getServerVpc() {
        if (serverVpc == null) {
            String serverVpcId = infrastructureConfiguration.getValue(InfrastructureSetting.SERVERVPCID);
            serverVpc = Vpc.builder().vpcId(serverVpcId).build();
        }
        return serverVpc;
    }
}
