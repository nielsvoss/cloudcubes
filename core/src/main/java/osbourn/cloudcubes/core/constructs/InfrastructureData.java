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
    private final String serverVpcId;

    public InfrastructureData(String region, String serverDataBaseName, String serverSecurityGroupName, String serverVpcId) {
        this.region = Region.getRegion(Regions.fromName(region.toLowerCase().replace('_', '-')));
        this.serverDataBaseName = serverDataBaseName;
        this.serverSecurityGroupName = serverSecurityGroupName;
        this.serverVpcId = serverVpcId;
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

    public String getServerVpcId() {
        return serverVpcId;
    }

    public HashMap<String, String> convertToMap() {
        HashMap<String, String> outputMap = new HashMap<>();
        outputMap.put("CLOUDCUBESREGION", region.getName());
        outputMap.put("CLOUDCUBESSERVERDATABASENAME", serverDataBaseName);
        outputMap.put("CLOUDCUBESSERVERSECURITYGROUPNAME", serverSecurityGroupName);
        outputMap.put("CLOUDCUBESSERVERVPCID", serverVpcId);
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
        String serverVpcId = System.getenv("CLOUDCUBESSERVERVPCID");
        assert region != null;
        assert serverDataBaseName != null;
        assert serverSecurityGroupName != null;
        return new InfrastructureData(region, serverDataBaseName, serverSecurityGroupName, serverVpcId);
    }

    /**
     * A helper class used to construct a InfrastructureData object. This class exists because objects of type
     * InfrastructureData cannot be modified. This class allows constructing an InfrastructureData object in parts,
     * allowing this job to be offloaded to different parts of the code and only constructed when finished.
     */
    public static class Builder {
        private String region = null;
        private String serverDataBaseName = null;
        private String serverSecurityGroupName = null;
        private String serverVpcId = null;

        private Builder() {}

        public static Builder create() {
            return new Builder();
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public Builder withRegion(String region) {
            setRegion(region);
            return this;
        }

        public String getServerDataBaseName() {
            return serverDataBaseName;
        }

        public void setServerDataBaseName(String serverDataBaseName) {
            this.serverDataBaseName = serverDataBaseName;
        }

        public Builder withServerDatabaseName(String serverDataBaseName) {
            setServerDataBaseName(serverDataBaseName);
            return this;
        }

        public String getServerSecurityGroupName() {
            return serverSecurityGroupName;
        }

        public void setServerSecurityGroupName(String serverSecurityGroupName) {
            this.serverSecurityGroupName = serverSecurityGroupName;
        }

        public Builder withServerSecurityGroupName(String serverSecurityGroupName) {
            setServerSecurityGroupName(serverSecurityGroupName);
            return this;
        }

        public String getServerVpcId() {
            return serverVpcId;
        }

        public void setServerVpcId(String serverVpcId) {
            this.serverVpcId = serverVpcId;
        }

        public Builder withServerVpcId(String serverVpcId) {
            setServerVpcId(serverVpcId);
            return this;
        }

        /**
         * Returns true if all the values that are required to build an InfrastructureData object have been set.
         *
         * @return If all the values required to build an InfrastructureData object have been set.
         */
        public boolean canBeConstructed() {
            return region != null && serverDataBaseName != null && serverSecurityGroupName != null && serverVpcId != null;
        }

        /**
         * Builds the InfrastructureData object
         *
         * @return The newly built InfrastructureData object.
         * @throws IllegalStateException If the Builder does not have all required values set
         */
        public InfrastructureData build() {
            if (!canBeConstructed()) {
                throw new IllegalStateException("Builder object does not yet have all the required values set.");
            }
            return new InfrastructureData(region, serverDataBaseName, serverSecurityGroupName, serverVpcId);
        }
    }
}
