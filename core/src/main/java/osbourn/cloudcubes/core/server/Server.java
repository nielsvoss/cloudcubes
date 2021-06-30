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

        this.displayName = databaseEntry.getString("DisplayName");
        assert this.displayName != null;
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
        String updateExpression = "set DisplayName = :n";
        ValueMap valueMap = new ValueMap()
                .withString(":n", this.displayName);
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
