package osbourn.cloudcubes.core.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents an object which has a UUID
 */
public interface Identifiable {
    /**
     * Gets the unique id of the object. This will not change between invocations.
     * This could also represent the id of the structure that the object represents
     * (e.g. the unique key for a database entry).
     *
     * @return The unique id of the object
     */
    @Contract(pure = true)
    @NotNull UUID getId();
}
