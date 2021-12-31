package osbourn.cloudcubes.core.database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import osbourn.cloudcubes.core.server.ServerOptions;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a entry on the DynamoDB database.
 * This class primarily acts as an interface to the DynamoDB table, allowing you to read and set values in the database.
 * In most situations, the database values will be set using helper classes such as {@link ServerOptions}.
 */
public class DynamoDBEntry implements DatabaseEntry {
    public final UUID id;
    private final DynamoDbClient dynamoDbClient;
    private final String tableName;
    /**
     * Contains a local cache of values the user requested from the database.
     * Format for each entry is ("nameOfKey", "valueInDatabase")
     */
    private final Map<String, String> stringValueCache = new HashMap<>();

    private DynamoDBEntry(UUID id, DynamoDbClient dynamoDbClient, String tableName) {
        this.id = id;
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    /**
     * Creates a new DynamoDBEntry object that corresponds to the object with the given id in the server database.
     *
     * @param id             The id of the server in the server database
     * @param dynamoDbClient The DynamoDB client used to make requests
     * @param tableName      The name of the database table
     * @return The server object that was just created
     */
    public static DynamoDBEntry fromId(UUID id, DynamoDbClient dynamoDbClient, String tableName) {
        return new DynamoDBEntry(id, dynamoDbClient, tableName);
    }

    @Override
    public @NotNull UUID getId() {
        return this.id;
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
    public @Nullable String getStringValue(@NotNull String valueToGet) {
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
    public @Nullable String requestStringValueFromDatabase(@NotNull String valueToGet) {
        // Used to let AWS know that we want to get values from the item that has "Id" set to this.id
        Map<String, AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put("Id", AttributeValue.builder()
                .s(this.id.toString())
                .build());

        // Request item
        GetItemRequest request = GetItemRequest.builder()
                .key(keyToGet)
                .tableName(this.tableName)
                .projectionExpression(valueToGet)
                .build();
        Map<String, AttributeValue> returnedItem = dynamoDbClient.getItem(request).item();

        // Request value from database
        String value = returnedItem.get(valueToGet).s();
        stringValueCache.put(valueToGet, value);
        return value;
    }

    public void setStringValue(@NotNull String key, @NotNull String value) {
        // Used to let AWS know that we want to set values for the item that has "Id" set to this.id
        Map<String, AttributeValue> itemKey = new HashMap<>();
        itemKey.put("Id", AttributeValue.builder()
                .s(this.id.toString())
                .build());

        // Tells AWS which value to update and what the new value is
        HashMap<String, AttributeValueUpdate> updatedValues = new HashMap<>();
        updatedValues.put(key, AttributeValueUpdate.builder()
                .value(AttributeValue.builder().s(value).build())
                .action(AttributeAction.PUT)
                .build());

        // Request value from database
        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(this.tableName)
                .key(itemKey)
                .attributeUpdates(updatedValues)
                .build();
        dynamoDbClient.updateItem(request);

        // Cache new value
        stringValueCache.put(key, value);
    }
}
