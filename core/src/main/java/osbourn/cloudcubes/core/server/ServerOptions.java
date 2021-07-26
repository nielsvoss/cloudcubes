package osbourn.cloudcubes.core.server;

/**
 * Allows accessing common options in the server database such as the server's display name that are not related to
 * the server instance itself.
 * This class acts as a wrapper around {@link Server}.
 */
public class ServerOptions {
    private final Server server;

    public ServerOptions(Server server) {
        this.server = server;
    }

    /**
     * Returns the server used to construct this object.
     *
     * @return The server used to construct this object.
     */
    public Server getServer() {
        return server;
    }

    /**
     * Gets the display name of the server from the database
     *
     * @return The display name of the server
     */
    public String getDisplayName() {
        String displayName = server.getStringValue("DisplayName");
        assert displayName != null;
        return displayName;
    }

    /**
     * Sets the display name of the server. The database will be updated with the new value.
     *
     * @param displayName The new display name of the server.
     */
    public void setDisplayName(String displayName) {
        server.setStringValue("DisplayName", displayName);
    }
}
