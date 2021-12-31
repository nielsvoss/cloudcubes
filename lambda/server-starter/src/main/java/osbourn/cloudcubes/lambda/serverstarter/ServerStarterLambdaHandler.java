package osbourn.cloudcubes.lambda.serverstarter;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import osbourn.cloudcubes.core.constructs.InfrastructureConfiguration;
import osbourn.cloudcubes.core.constructs.InfrastructureData;
import osbourn.cloudcubes.core.server.CloudCubesServer;
import osbourn.cloudcubes.core.server.Server;

import java.util.Map;

public class ServerStarterLambdaHandler implements RequestHandler<Map<String, String>, String> {
    @Override
    public String handleRequest(Map<String, String> event, Context context) {
        LambdaLogger logger = context.getLogger();
        String response = "200 OK";

        InfrastructureConfiguration infrastructureConfiguration = InfrastructureConfiguration.fromEnvironment();
        Server server = CloudCubesServer.fromId(1, infrastructureConfiguration);
        server.startServer();

        return response;
    }
}
