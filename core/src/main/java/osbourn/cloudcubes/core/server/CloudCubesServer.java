package osbourn.cloudcubes.core.server;

import org.jetbrains.annotations.NotNull;
import osbourn.cloudcubes.core.constructs.InfrastructureConfiguration;
import osbourn.cloudcubes.core.constructs.InfrastructureConfiguration.InfrastructureSetting;
import osbourn.cloudcubes.core.constructs.InfrastructureConstructor;
import osbourn.cloudcubes.core.database.DatabaseEntry;
import osbourn.cloudcubes.core.database.DynamoDBEntry;

import java.util.UUID;

public class CloudCubesServer implements Server {
    private final UUID id;
    private final DatabaseEntry databaseEntry;
    private final EC2SpotInstanceManager EC2SpotInstanceManager;

    private CloudCubesServer(
            UUID id,
            DynamoDBEntry databaseEntry,
            EC2SpotInstanceManager EC2SpotInstanceManager
    ) {
        this.id = id;
        this.databaseEntry = databaseEntry;
        this.EC2SpotInstanceManager = EC2SpotInstanceManager;
    }

    @Override
    public @NotNull UUID getId() {
        return id;
    }

    @Override
    public ProvisionalServerState getServerState() {
        return null;
    }

    @Override
    public void startServer() {
        EC2SpotInstanceManager.startServer();
    }

    /**
     * Gets the display name of the server from the database
     *
     * @return The display name of the server
     */
    @Override
    public String getDisplayName() {
        String displayName = this.databaseEntry.getStringValue("DisplayName");
        assert displayName != null;
        return displayName;
    }

    /**
     * Sets the display name of the server. The database will be updated with the new value.
     *
     * @param displayName The new display name of the server.
     */
    @Override
    public void setDisplayName(String displayName) {
        this.databaseEntry.setStringValue("DisplayName", displayName);
    }

    public static CloudCubesServer fromId(UUID id, InfrastructureConfiguration infrastructureConfiguration) {
        InfrastructureConstructor infrastructureConstructor = new InfrastructureConstructor(infrastructureConfiguration);
        DynamoDBEntry dynamoDBEntry = DynamoDBEntry.fromId(
                id,
                infrastructureConstructor.getDynamoDBClient(),
                infrastructureConfiguration.getValue(InfrastructureSetting.SERVERDATABASENAME));
        EC2SpotInstanceManager EC2SpotInstanceManager = new EC2SpotInstanceManager(
                dynamoDBEntry,
                infrastructureConstructor.getEc2Client(),
                infrastructureConfiguration,
                infrastructureConfiguration.getValue(InfrastructureSetting.SERVERINSTANCEPROFILEARN),
                infrastructureConfiguration.getServerSubnetIds().get(0),
                infrastructureConfiguration.getValue(InfrastructureSetting.SERVERSECURITYGROUPID)
        );
        return new CloudCubesServer(id, dynamoDBEntry, EC2SpotInstanceManager);
    }
}
