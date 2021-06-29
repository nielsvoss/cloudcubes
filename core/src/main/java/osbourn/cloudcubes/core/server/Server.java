package osbourn.cloudcubes.core.server;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a server entry on the DynamoDB database.
 */
public class Server {
    private final int id;
    private final AmazonDynamoDB dynamoDB;
    private final String tableName;

    private String name;

    private Server(int id, AmazonDynamoDB dynamoDB, String tableName) {
        this.id = id;
        this.dynamoDB = dynamoDB;
        this.tableName = tableName;
    }

    /**
     * Send request to database to retrieve values, used in {@link #fromId(int, AmazonDynamoDB, String)}
     */
    private void initializeValues() {
        // Generate information needed to specify a request to DynamoDB
        HashMap<String, AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put(this.tableName, new AttributeValue(String.valueOf(this.id)));
        GetItemRequest request = new GetItemRequest(this.tableName, keyToGet);

        // Request database item from DynamoDB
        Map<String, AttributeValue> item = dynamoDB.getItem(request).getItem();
        assert item != null && !item.isEmpty();

        // Get values from database
        this.name = item.get("Name").getS();
    }

    /**
     * Gets the numeric id of the server.
     *
     * @return The id of the server.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the display name of the server.
     *
     * @return The display name of the server.
     */
    public String getName() {
        return name;
    }

    /**
     * Creates a new server object by downloading the entry that corresponds to 'id' from the server database.
     *
     * @param id The id of the server in the server database
     * @param tableName The name of the DynamoDB database to look up the server in
     *
     * @return The server object that was just created
     */
    public static Server fromId(int id, AmazonDynamoDB dynamoDB, String tableName) {
        Server server = new Server(id, dynamoDB, tableName);
        server.initializeValues();
        return server;
    }
}
