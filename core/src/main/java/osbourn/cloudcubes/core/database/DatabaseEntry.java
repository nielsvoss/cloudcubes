package osbourn.cloudcubes.core.database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import osbourn.cloudcubes.core.util.Identifiable;

/**
 * Represents a single entry in a database
 */
public interface DatabaseEntry extends Identifiable {
    /**
     * Gets a string value associated with key "key" from the database
     *
     * @param key The key to get the value of
     * @return The value in the database that matches the key, or null if the key does not exist
     */
    @Nullable String getStringValue(@NotNull String key);

    /**
     * Sets the string value associated with key "key" to the specified value
     *
     * @param key The key of the value to be set
     * @param value The value to put in the database
     */
    void setStringValue(@NotNull String key, @NotNull String value);
}

