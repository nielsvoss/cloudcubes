package osbourn.cloudcubes.core.constructs;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;

/**
 * Retrieves information from an InfrastructureData object and generates AWS SDK objects.
 * For example, it can return objects representing the DynamoDB Table where the server data is stored.
 */
public class InfrastructureConstructor {
    private final InfrastructureData infrastructureData;

    private AmazonDynamoDB amazonDynamoDB = null;
    private DynamoDB dynamoDB = null;
    private Table serverTable = null;

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

    public AmazonDynamoDB getAmazonDynamoDB() {
        if (amazonDynamoDB == null) {
            amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                    .withRegion(infrastructureData.getRegion().getName())
                    .build();
        }
        return amazonDynamoDB;
    }

    public DynamoDB getDynamoDB() {
        if (dynamoDB == null) {
            dynamoDB = new DynamoDB(getAmazonDynamoDB());
        }
        return dynamoDB;
    }

    public Table getServerTable() {
        if (serverTable == null) {
            serverTable = getDynamoDB().getTable(infrastructureData.getServerDataBaseName());
        }
        return serverTable;
    }
}
