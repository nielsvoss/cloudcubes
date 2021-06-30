package osbourn.cloudcubes.core.server;

/**
 * Represents an EC2 instance that corresponds to a Server object.
 * Can be online or offline.
 */
public class ServerInstance {
    private final Server server;

    public ServerInstance(Server server) {
        this.server = server;
    }

    /**
     * Gets the Server object used to construct this class.
     *
     * @return The Server object used to construct this class.
     */
    public Server getServer() {
        return server;
    }

    /**
     * Returns the id of the EC2 instance running the server, or null if it does not exist.
     *
     * @return The EC2InstanceId of the Server, or null if the server is not running.
     */
    public String getEC2InstanceId() {
        return server.getEC2InstanceID();
    }

    /**
     * Returns the Id of the EC2 Spot Request running the server, or null if it does not exist.
     *
     * @return The Id of the EC2 Spot Request running the server, or null if it does not exist.
     */
    public String getSpotRequestId() {
        return server.getEC2SpotRequestID();
    }

    /**
     * The ServerState representing whether the server is online. The ServerState can be Unknown, so it is recommended
     * to use {@link #isServerOnline()} in most situations.
     *
     * @return The ServerState representing whether the server is online.
     * @see #isServerOnline()
     */
    public ServerState getServerState() {
        return server.getServerState();
    }

    /**
     * Returns true if the server is online, false if it isn't. This is often preferable to {@link #getServerState()}
     * because it will verify the server state if it is unknown.
     *
     * @return True if the server is online, false otherwise.
     */
    public boolean isServerOnline() {
        // TODO: Verify the server state if the server state is UNKNOWN
        return getServerState() == ServerState.ONLINE;
    }
}
