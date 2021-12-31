package osbourn.cloudcubes.lambda.serverstarter;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import osbourn.cloudcubes.core.constructs.InfrastructureConfiguration;
import osbourn.cloudcubes.core.server.CloudCubesServer;
import osbourn.cloudcubes.core.server.Server;

import java.util.Map;
import java.util.UUID;

public class ServerStarterLambdaHandler implements RequestHandler<Map<String, String>, String> {
    @Override
    public String handleRequest(Map<String, String> event, Context context) {
        LambdaLogger logger = context.getLogger();
        String response = "200 OK";

        InfrastructureConfiguration infrastructureConfiguration = InfrastructureConfiguration.fromEnvironment();
        // Sample UUID
        UUID serverId = UUID.fromString("80000000-0000-0000-8000-000000000000");
        Server server = CloudCubesServer.fromId(serverId, infrastructureConfiguration);
        server.startServer();

        return response;
    }
}
