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

    private void setServerState(ServerState serverState) {
        String serverStateAsString;
        switch (serverState) {
            case OFFLINE:
                serverStateAsString = "OFFLINE";
                break;
            case ONLINE:
                serverStateAsString = "ONLINE";
                break;
            case UNKNOWN:
            default:
                serverStateAsString = "UNKNOWN";
                break;
        }
        server.setStringValue("ServerState", serverStateAsString);
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
        return server.getStringValue("EC2InstanceId");
    }

    /**
     * Returns the Id of the EC2 Spot Request running the server, or null if it does not exist.
     *
     * @return The Id of the EC2 Spot Request running the server, or null if it does not exist.
     */
    public String getSpotRequestId() {
        return server.getStringValue("EC2SpotRequestId");
    }

    /**
     * The ServerState representing whether the server is online. The ServerState can be Unknown, so it is recommended
     * to use {@link #isServerOnline()} in most situations.
     *
     * @return The ServerState representing whether the server is online.
     * @see #isServerOnline()
     */
    public ServerState getServerState() {
        String serverStateAsString = server.getStringValue("ServerState");
        assert serverStateAsString != null;
        switch (serverStateAsString) {
            case "OFFLINE":
                return ServerState.OFFLINE;
            case "ONLINE":
                return ServerState.ONLINE;
            case "UNKNOWN":
                return ServerState.UNKNOWN;
            default:
                // TODO Throw exception or log warning
                return ServerState.UNKNOWN;
        }
    }

    public void startServer() {
        if (!isServerOnline()) {
            throw new IllegalStateException();
        }

        // Once the server starts, it will update the state in the database with a ONLINE state
        // If the server startup fails the database will contain an UNKNOWN state
        // and it will be checked the next time the state is read.
        setServerState(ServerState.UNKNOWN);

        // TODO: Launch EC2 Instance
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
