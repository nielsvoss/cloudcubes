package osbourn.cloudcubes.core.server;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;

/**
 * Represents a server entry on the DynamoDB database.
 */
public class Server {
    private final Table table;
    private String name;

    public final int id;

    private Server(int id, Table table) {
        this.id = id;
        this.table = table;
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

        Item databaseEntry = table.getItem("Id", id);
        assert databaseEntry != null;
        server.name = databaseEntry.getString("Name");
        assert server.name != null;

        return server;
    }
}
