package osbourn.cloudcubes.core.server;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;

/**
 * Represents a server entry on the DynamoDB database.
 */
public class Server {
    private final Table table;
    private String displayName;
    private String EC2InstanceID;
    private String EC2SpotRequestID;
    private ServerState serverState;

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

    protected ServerState getServerState() {
        return serverState;
    }

    protected void setServerState(ServerState state) {
        this.serverState = state;
        dirty = true;
    }

    protected String getEC2InstanceID() {
        return EC2InstanceID;
    }

    protected void setEC2InstanceID(String EC2InstanceID) {
        this.EC2InstanceID = EC2InstanceID;
        dirty = true;
    }

    protected String getEC2SpotRequestID() {
        return EC2SpotRequestID;
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
        return displayName;
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
