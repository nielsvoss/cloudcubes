package osbourn.cloudcubes.core.constructs;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import java.util.HashMap;

/**
 * <p>
 * Certain parts of the code, such as lambda functions, need certain context information in order to be able to
 * function properly. In particular, they need access to the name of the database so they know which database to
 * send requests to. In the case of Lambda functions, these values can be made available using environment variables.
 * </p>
 *
 * <p>
 * This class serves to help construct those environment variables by containing the values that will be sent to the
 * Lambda functions. A InfrastructureData object will be constructed during `cdk synth` and it will be converted to
 * a HashMap of environment variables. The lambda functions will read the environment variables and turn it back into
 * and InfrastructureData object.
 * </p>
 */
public class InfrastructureData {
    private final Region region;
    private final String serverDataBaseName;
    private final String serverSecurityGroupName;

    public InfrastructureData(String region, String serverDataBaseName, String serverSecurityGroupName) {
        this.region = Region.getRegion(Regions.fromName(region.toLowerCase().replace('_', '-')));
        this.serverDataBaseName = serverDataBaseName;
        this.serverSecurityGroupName = serverSecurityGroupName;
    }

    public Region getRegion() {
        return region;
    }

    public String getServerDataBaseName() {
        return serverDataBaseName;
    }

    public String getServerSecurityGroupName() {
        return serverSecurityGroupName;
    }

    public HashMap<String, String> convertToMap() {
        HashMap<String, String> outputMap = new HashMap<>();
        outputMap.put("CLOUDCUBESREGION", region.getName());
        outputMap.put("CLOUDCUBESSERVERDATABASENAME", serverDataBaseName);
        outputMap.put("CLOUDCUBESSERVERSECURITYGROUPNAME", serverSecurityGroupName);
        return outputMap;
    }

    /**
     * Generates an InfrastructureData object from environment variables
     *
     * @return The InfrastructureData object just generated
     */
    public static InfrastructureData fromEnvironment() {
        String region = System.getenv("CLOUDCUBESREGION");
        String serverDataBaseName = System.getenv("CLOUDCUBESSERVERDATABASENAME");
        String serverSecurityGroupName = System.getenv("CLOUDCUBESSERVERSECURITYGROUPNAME");
        assert region != null;
        assert serverDataBaseName != null;
        assert serverSecurityGroupName != null;
        return new InfrastructureData(region, serverDataBaseName, serverSecurityGroupName);
    }
}
