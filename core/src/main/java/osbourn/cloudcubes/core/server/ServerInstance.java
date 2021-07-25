package osbourn.cloudcubes.core.server;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;

import java.util.List;

/**
 * Represents an EC2 instance that corresponds to a Server object.
 * Can be online or offline.
 */
public class ServerInstance {
    private final Server server;
    private final AmazonEC2 ec2;
    private final String subnetId;
    private final String serverSecurityGroup;

    public ServerInstance(Server server, AmazonEC2 ec2, String subnetId, String serverSecurityGroup) {
        this.server = server;
        this.ec2 = ec2;
        this.subnetId = subnetId;
        this.serverSecurityGroup = serverSecurityGroup;
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
     * Gets the AmazonEC2 object used to construct this class.
     *
     * @return The AmazonEC2 object used to construct this class.
     */
    public AmazonEC2 getEC2() {
        return ec2;
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
        final String amazonLinux2AmiId = "ami-0277b52859bac6f4b";

        if (isServerOnline()) {
            throw new IllegalStateException("The server is currently online");
        }

        // Once the server starts, it will update the state in the database with a ONLINE state
        // If the server startup fails the database will contain an UNKNOWN state
        // and it will be checked the next time the state is read.
        setServerState(ServerState.UNKNOWN);

        // Request EC2 Instance
        LaunchSpecification launchSpecification = new LaunchSpecification()
                .withImageId(amazonLinux2AmiId)
                .withInstanceType(InstanceType.M5Large)
                .withSubnetId(subnetId)
                .withSecurityGroups(serverSecurityGroup);
        RequestSpotInstancesRequest spotInstancesRequest = new RequestSpotInstancesRequest()
                .withInstanceCount(1)
                .withLaunchSpecification(launchSpecification);
        RequestSpotInstancesResult requestResult = ec2.requestSpotInstances(spotInstancesRequest);

        List<SpotInstanceRequest> requestResponses = requestResult.getSpotInstanceRequests();
        // requestResponses should only contain one request
        assert requestResponses.size() == 1;
        String spotInstanceId = requestResponses.get(0).getSpotInstanceRequestId();

        // Update database with requestId
        server.setStringValue("EC2SpotRequestId", spotInstanceId);

        // TODO: Update database with the EC2 Instance Id once the server has started
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
