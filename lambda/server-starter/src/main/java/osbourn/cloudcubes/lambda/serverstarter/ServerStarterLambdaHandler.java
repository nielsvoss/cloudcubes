package osbourn.cloudcubes.lambda.serverstarter;

import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import osbourn.cloudcubes.core.constructs.InfrastructureConstructor;
import osbourn.cloudcubes.core.constructs.InfrastructureData;
import osbourn.cloudcubes.core.server.Server;
import osbourn.cloudcubes.core.server.ServerInstance;

import java.util.Map;

public class ServerStarterLambdaHandler implements RequestHandler<Map<String, String>, String> {
    @Override
    public String handleRequest(Map<String, String> event, Context context) {
        LambdaLogger logger = context.getLogger();
        String response = "200 OK";

        InfrastructureData infrastructureData = InfrastructureData.fromEnvironment();
        InfrastructureConstructor infrastructureConstructor = new InfrastructureConstructor(infrastructureData);
        Table serverTable = infrastructureConstructor.getServerTable();
        Server server = Server.fromId(1, serverTable);
        ServerInstance serverInstance = new ServerInstance(
                server,
                infrastructureConstructor.getAmazonEC2(),
                infrastructureData.getServerSecurityGroupName());
        serverInstance.startServer();

        return response;
    }
}
