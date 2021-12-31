package osbourn.cloudcubes.core.server;

import osbourn.cloudcubes.core.constructs.InfrastructureConfiguration;
import osbourn.cloudcubes.core.constructs.InfrastructureConfiguration.InfrastructureSetting;
import osbourn.cloudcubes.core.constructs.InfrastructureConstructor;
import osbourn.cloudcubes.core.database.ServerTableEntry;

import java.util.UUID;

public class CloudCubesServer implements Server {
    private final UUID id;
    private final InfrastructureConfiguration infrastructureConfiguration;
    private final ServerTableEntry serverTableEntry;
    private final ServerInstance serverInstance;
    private final ServerOptions serverOptions;

    private CloudCubesServer(
            UUID id,
            InfrastructureConfiguration infrastructureConfiguration,
            ServerTableEntry serverTableEntry,
            ServerInstance serverInstance,
            ServerOptions serverOptions
    ) {
        this.id = id;
        this.infrastructureConfiguration = infrastructureConfiguration;
        this.serverTableEntry = serverTableEntry;
        this.serverInstance = serverInstance;
        this.serverOptions = serverOptions;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public ServerState getServerState() {
        return null;
    }

    @Override
    public void startServer() {
        serverInstance.startServer();
    }

    @Override
    public String getDisplayName() {
        return serverOptions.getDisplayName();
    }

    @Override
    public void setDisplayName(String displayName) {
        serverOptions.setDisplayName(displayName);
    }

    public static CloudCubesServer fromId(UUID id, InfrastructureConfiguration infrastructureConfiguration) {
        InfrastructureConstructor infrastructureConstructor = new InfrastructureConstructor(infrastructureConfiguration);
        ServerTableEntry serverTableEntry = ServerTableEntry.fromId(
                id,
                infrastructureConstructor.getDynamoDBClient(),
                infrastructureConfiguration.getValue(InfrastructureSetting.SERVERDATABASENAME));
        ServerOptions serverOptions = new ServerOptions(serverTableEntry);
        ServerInstance serverInstance = new ServerInstance(
                serverTableEntry,
                infrastructureConstructor.getEc2Client(),
                infrastructureConfiguration,
                infrastructureConfiguration.getValue(InfrastructureSetting.SERVERINSTANCEPROFILEARN),
                infrastructureConfiguration.getServerSubnetIds().get(0),
                infrastructureConfiguration.getValue(InfrastructureSetting.SERVERSECURITYGROUPID)
        );
        return new CloudCubesServer(id, infrastructureConfiguration, serverTableEntry, serverInstance, serverOptions);
    }
}
