package osbourn.cloudcubes.core.server;

import osbourn.cloudcubes.core.constructs.InfrastructureData;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Represents an EC2 instance that corresponds to a Server object.
 * Can be online or offline.
 */
public class ServerInstance {
    private final Server server;
    private final Ec2Client ec2Client;
    private final InfrastructureData infrastructureData;
    private final String serverInstanceProfileArn;
    private final String subnetId;
    private final String serverSecurityGroup;
    private String userData = null;

    public ServerInstance(Server server,
                          Ec2Client ec2Client,
                          InfrastructureData infrastructureData,
                          String serverInstanceProfileArn,
                          String subnetId,
                          String serverSecurityGroup) {
        this.server = server;
        this.ec2Client = ec2Client;
        this.infrastructureData = infrastructureData;
        this.serverInstanceProfileArn = serverInstanceProfileArn;
        this.subnetId = subnetId;
        this.serverSecurityGroup = serverSecurityGroup;
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
     * Gets the Ec2Client object used to construct this class.
     *
     * @return The Ec2Client object used to construct this class.
     */
    public Ec2Client getEC2Client() {
        return ec2Client;
    }

    /**
     * Gets the InfrastructureData object used to construct this class.
     *
     * @return The InfrastructureData object used to construct this class.
     */
    public InfrastructureData infrastructureData() {
        return infrastructureData;
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

    public void startServer() {
        final String amazonLinux2AmiId = "ami-0233c2d874b811deb";

        if (isServerOnline()) {
            throw new IllegalStateException("The server is currently online");
        }

        // Once the server starts, it will update the state in the database with a ONLINE state
        // If the server startup fails the database will contain an UNKNOWN state
        // and it will be checked the next time the state is read.
        setServerState(ServerState.UNKNOWN);

        // Request EC2 Instance
        RequestSpotLaunchSpecification launchSpecification = RequestSpotLaunchSpecification.builder()
                .instanceType(InstanceType.M5_LARGE)
                .subnetId(subnetId)
                .imageId(amazonLinux2AmiId)
                .iamInstanceProfile(IamInstanceProfileSpecification.builder().arn(serverInstanceProfileArn).build())
                .securityGroupIds(serverSecurityGroup)
                .userData(Base64.getEncoder().encodeToString(getUserData().getBytes()))
                .build();
        RequestSpotInstancesRequest spotInstancesRequest = RequestSpotInstancesRequest.builder()
                .instanceCount(1)
                .launchSpecification(launchSpecification)
                .build();
        RequestSpotInstancesResponse requestResult = ec2Client.requestSpotInstances(spotInstancesRequest);

        List<SpotInstanceRequest> requestResponses = requestResult.spotInstanceRequests();
        // requestResponses should only contain one request
        assert requestResponses.size() == 1;
        String spotInstanceId = requestResponses.get(0).spotInstanceRequestId();

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

    /**
     * Generates the user data for an EC2 instance. User data is a series of (usually shell) commands that will be run
     * from the root account as soon as the instance starts up. Note that you will probably need to convert the output
     * of this method to base64 before using it.
     *
     * @return The generated user data
     */
    private String getUserData() {
        if (userData == null) {
            // Strings not matching this regex may contain values that are not interpreted literally by bash
            // (i.e. they need to be escaped)
            final String allowedCharactersPattern = "^[a-zA-Z0-9,._+:@%/-]+$";

            String resourceBucketName = infrastructureData.getResourceBucketName();
            if (!resourceBucketName.matches(allowedCharactersPattern)) {
                throw new IllegalStateException(
                        "The resource bucket name stored inside the provided infrastructure data contains invalid characters");
            }

            StringBuilder builder = new StringBuilder();
            // Lets server know that the remaining commands should be run with bash
            builder.append("#!/bin/bash\n");
            builder.append("cd /home/ec2-user\n");
            // Set environment variables to the values in infrastructureData
            for (Map.Entry<String, String> entry : infrastructureData.convertToMap().entrySet()) {
                if (!entry.getKey().matches(allowedCharactersPattern) || !entry.getValue().matches(allowedCharactersPattern)) {
                    // TODO: Log warning
                    continue;
                }
                builder.append(String.format("export %s=%s\n", entry.getKey(), entry.getValue()));
            }
            // Download and invoke script (the hyphen at the end of the s3 command tells it to print to stdout)
            String s3command = "aws s3 cp s3://" + resourceBucketName + "/server-startup/startup.sh -";
            builder.append("su -c 'curl ").append(s3command).append(" | bash' ec2-user\n");
            userData = builder.toString();
        }
        return userData;
    }
}
