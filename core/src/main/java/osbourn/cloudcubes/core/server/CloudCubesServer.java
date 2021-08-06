package osbourn.cloudcubes.core.server;

import osbourn.cloudcubes.core.constructs.InfrastructureConstructor;
import osbourn.cloudcubes.core.constructs.InfrastructureData;

public class CloudCubesServer implements Server {
    private final int id;
    private final InfrastructureData infrastructureData;
    private final ServerTableEntry serverTableEntry;
    private final ServerInstance serverInstance;
    private final ServerOptions serverOptions;

    private CloudCubesServer(
            int id,
            InfrastructureData infrastructureData,
            ServerTableEntry serverTableEntry,
            ServerInstance serverInstance,
            ServerOptions serverOptions
    ) {
        this.id = id;
        this.infrastructureData = infrastructureData;
        this.serverTableEntry = serverTableEntry;
        this.serverInstance = serverInstance;
        this.serverOptions = serverOptions;
    }

    @Override
    public int getId() {
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

    public static CloudCubesServer fromId(int id, InfrastructureData infrastructureData) {
        InfrastructureConstructor infrastructureConstructor = new InfrastructureConstructor(infrastructureData);
        ServerTableEntry serverTableEntry = ServerTableEntry.fromId(
                id, infrastructureConstructor.getDynamoDBClient(), infrastructureData.getServerDataBaseName());
        ServerOptions serverOptions = new ServerOptions(serverTableEntry);
        ServerInstance serverInstance = new ServerInstance(
                serverTableEntry,
                infrastructureConstructor.getEc2Client(),
                infrastructureData,
                infrastructureData.getServerInstanceProfileArn(),
                infrastructureData.getServerSubnetIds().get(0),
                infrastructureData.getServerSecurityGroupName()
        );
        return new CloudCubesServer(id, infrastructureData, serverTableEntry, serverInstance, serverOptions);
    }
}
