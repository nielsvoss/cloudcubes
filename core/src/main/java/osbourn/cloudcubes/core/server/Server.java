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
 * This class primarily acts as an interface to the DynamoDB table, allowing you to read and set values in the database.
 * In most situations, the database values will be set using helper classes such as {@link ServerOptions}.
 */
public class Server {
    private final Table table;

    /**
     * Contains a local cache of values the user requested from the database.
     * Format for each entry is ("nameOfKey", "valueInDatabase")
     */
    private final Map<String, String> stringValueCache = new HashMap<>();

    public final int id;

    private Server(int id, Table table) {
        this.id = id;
        this.table = table;
    }

    /**
     * Some values, such as the server display name and the EC2InstanceId are likely to be used by the program.
     * This method caches those values so they don't need to be retrieved later.
     * This method is primarily intended to be used in {@link #fromId(int, Table)}.
     */
    private void cacheInitialValues() {
        // The output of this method call can be ignored because we only care about caching the values
        requestStringValuesFromDatabase(
                "DisplayName",
                "EC2InstanceId",
                "EC2SpotRequestId",
                "ServerState"
        );
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
    public String getStringValue(String valueToGet) {
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
     * cached. As such, getStringValue is preferable in most circumstances but this method is useful if you need to
     * make sure the value is up to date.
     * </p>
     *
     * @param valueToGet The key of the value in the database
     * @return The value in the database, or null if it does not exist.
     * @see #getStringValue(String) 
     */
    public String requestStringValueFromDatabase(String valueToGet) {
        // Request value from database
        GetItemSpec getItemSpec = new GetItemSpec()
                .withPrimaryKey("Id", id)
                .withAttributesToGet(valueToGet);
        Item item = table.getItem(getItemSpec);
        String value = item.getString(valueToGet);
        stringValueCache.put(valueToGet, value);
        return value;
    }

    /**
     * <p>
     * Gets multiple values from the database. This functions similar to {@link #requestStringValueFromDatabase(String)}
     * but allows you to request multiple values at one time. This function may be slightly more efficient than calling
     * requestStringValueFromDatabase multiple times.
     * </p>
     *
     * <p>
     * The entries in the array this method returns correspond in position to the entries in the 'valuesToGet' parameter.
     * For example:
     * <pre>{@code
     * String displayName = requestStringValueFromDatabase("DisplayName");
     * String EC2InstanceId = requestStringValueFromDatabase("EC2InstanceId");
     * String EC2SpotRequestId = requestStringValueFromDatabase("EC2SpotRequestId");
     * }</pre>
     * can be replaced with
     * <pre>{@code
     * String[] values = requestStringValuesFromDatabase("DisplayName", "EC2InstanceId", "EC2SpotRequestId");
     * String displayName = values[0];
     * String EC2InstanceId = values[1];
     * String EC2SpotRequestId = values[2];
     * }</pre>
     * </p>
     *
     * @param valuesToGet The keys of the values you want to request
     * @return An array of the values you requested. An entry in the array may be null if the value did not exist.
     */
    public String[] requestStringValuesFromDatabase(String... valuesToGet) {
        // Request value from database
        GetItemSpec getItemSpec = new GetItemSpec()
                .withPrimaryKey("Id", id)
                .withAttributesToGet(valuesToGet);
        Item item = table.getItem(getItemSpec);

        // Get values from 'item' variable
        String[] values = new String[valuesToGet.length];
        for (int i = 0; i < valuesToGet.length; i++) {
            String value = item.getString(valuesToGet[i]);
            stringValueCache.put(valuesToGet[i], value);
            values[i] = value;
        }
        return values;
    }

    public void setStringValue(String key, String value) {
        String updateExpression = String.format("SET %s = :v", key);
        ValueMap valueMap = new ValueMap().withString(":v", value);
        UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("Id", this.id)
                .withUpdateExpression(updateExpression)
                .withValueMap(valueMap)
                .withReturnValues(ReturnValue.NONE);
        this.table.updateItem(updateItemSpec);

        // Cache new value
        stringValueCache.put(key, value);
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
        server.cacheInitialValues();
        return server;
    }
}
