package osbourn.cloudcubes.core.server;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a server entry on the DynamoDB database.
 */
public class Server {
    private final Table table;

    /**
     * Contains a local cache of values the user requested from the database.
     * Format for each entry is ("nameOfKey", "valueInDatabase")
     */
    private final Map<String, String> stringValueCache = new HashMap<>();

    private boolean dirty = false;

    public final int id;

    private Server(int id, Table table) {
        this.id = id;
        this.table = table;
    }

    /**
     * Send request to database to retrieve values, used in {@link #fromId(int, Table)}
     */
    private void initializeValues() {
        Item databaseEntry = this.table.getItem("Id", id);
        assert databaseEntry != null;

        // Get values from database
        this.displayName = databaseEntry.getString("DisplayName");
        this.EC2InstanceID = databaseEntry.getString("EC2InstanceId");
        this.EC2SpotRequestID = databaseEntry.getString("EC2SpotRequestId");

        // Make sure values that shouldn't be null aren't null
        assert this.displayName != null;

        // Get server state
        String serverStateDatabaseEntry = databaseEntry.getString("ServerState");
        if (serverStateDatabaseEntry != null) {
            switch (serverStateDatabaseEntry) {
                case "OFFLINE":
                    this.serverState = ServerState.OFFLINE;
                    break;
                case "ONLINE":
                    this.serverState = ServerState.ONLINE;
                    break;
                case "UNKNOWN":
                    this.serverState = ServerState.UNKNOWN;
                    // TODO: Log warning
                    break;
                default:
                    this.serverState = ServerState.UNKNOWN;
                    // TODO: Log error
                    break;
            }
        } else {
            this.serverState = ServerState.UNKNOWN;
            // TODO: Log error
        }
    }

    /**
     * <p>
     * Gets a value associated with a key from the database, assuming the value is a String.
     * For example, calling this method with the argument "DisplayName" would read the database for that value and
     * would return it.
     * </p>
     *
     * <p>
     * This method differs from {@link #requestStringValueFromDatabase(String)} because it uses a local cache to
     * determine if a value has been downloaded before. As such, it can be slightly more efficient if multiple
     * parts of the code try to download the same value, but you can call requestStringValueFromDatabase if you need
     * to make sure that the value is up to date.
     * </p>
     *
     * @param valueToGet The key of the value in the database
     * @return The value in the database, or null if it does not exist.
     * @see #requestStringValueFromDatabase(String)
     */
    protected String getStringValue(String valueToGet) {
        // Check if value has been cached
        if (stringValueCache.containsKey(valueToGet)) {
            return stringValueCache.get(valueToGet);
        } else {
            return requestStringValueFromDatabase(valueToGet);
        }
    }
    /**
     * <p>
     * Gets a value associated with a key from the database, assuming the value is a String.
     * For example, calling this method with the argument "DisplayName" would read the database for that value and
     * would return it.
     * </p>
     *
     * <p>
     * This method differs from {@link #getStringValue(String)} because getStringValue uses a cache and will only
     * download values that haven't yet been downloaded. This method will redownload the value even if it is already
     * cached.
     * </p>
     *
     * @param valueToGet The key of the value in the database
     * @return The value in the database, or null if it does not exist.
     */
    protected String requestStringValueFromDatabase(String valueToGet) {
        // Request value from database
        GetItemSpec getItemSpec = new GetItemSpec()
                .withPrimaryKey("Id", id)
                .withAttributesToGet(valueToGet);
        Item item = table.getItem(getItemSpec);
        String value = item.getString(valueToGet);
        stringValueCache.put(valueToGet, value);
        return value;
    }

    protected String getEC2InstanceID() {
        return getStringValue("EC2InstanceId");
    }

    protected void setEC2InstanceID(String EC2InstanceID) {
        this.EC2InstanceID = EC2InstanceID;
        dirty = true;
    }

    protected String getEC2SpotRequestID() {
        return getStringValue("EC2SpotRequestId");
    }

    protected void setEC2SpotRequestID(String EC2SpotRequestID) {
        this.EC2SpotRequestID = EC2SpotRequestID;
        dirty = true;
    }

    /**
     * Returns the display name of the server
     *
     * @return The display name of the server
     */
    public String getDisplayName() {
        return getStringValue("DisplayName");
    }

    /**
     * Sets the display name of the server. Note that this value will not be updated in the database until
     * {@link #writeChangesToTable()} is called.
     *
     * @param displayName The new display name of the server.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        dirty = true;
    }

    /**
     * When a value in this class is set via a setter method, it doesn't write the changes out to the database until
     * {@link #writeChangesToTable()} is called. This object is considered dirty if a value has been set but the
     * database table has not been updated. This method returns true if the object is dirty and the database needs to
     * be updated.
     *
     * @return True if the database needs to be updated, false otherwise.
     * @see #writeChangesToTable()
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * When a value in this class is set via a setter method, it doesn't write teh changes out to the database until
     * this method is called. (You can check if you need to call this method by running {@link #isDirty()}.)
     * This method will update the database will the new values that are stored locally in this object.
     *
     * @see #isDirty()
     */
    public void writeChangesToTable() {
        String serverStateAsString = this.serverState.toString();
        String updateExpression = "set DisplayName = :n, EC2InstanceId = :i, EC2SpotRequestId = :r, ServerState = :s";
        ValueMap valueMap = new ValueMap()
                .withString(":n", this.displayName)
                .withString(":i", this.EC2InstanceID)
                .withString(":r", this.EC2SpotRequestID)
                .withString(":s", serverStateAsString);
        UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("Id", this.id)
                .withUpdateExpression(updateExpression)
                .withValueMap(valueMap)
                .withReturnValues(ReturnValue.UPDATED_NEW);
        this.table.updateItem(updateItemSpec);
        dirty = false;
    }

    /**
     * Creates a new server object by downloading the entry that corresponds to 'id' from the server database.
     *
     * @param id The id of the server in the server database
     * @param table The DynamoDB database to look up the server in
     *
     * @return The server object that was just created
     */
    public static Server fromId(int id, Table table) {
        Server server = new Server(id, table);
        server.initializeValues();
        return server;
    }
}
