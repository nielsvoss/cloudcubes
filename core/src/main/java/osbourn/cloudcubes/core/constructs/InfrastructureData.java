package osbourn.cloudcubes.core.constructs;

import software.amazon.awssdk.regions.Region;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
    private final String resourceBucketName;
    private final String serverRoleId;
    private final String serverInstanceProfileArn;
    private final String serverSecurityGroupName;
    private final String serverVpcId;
    private final List<String> serverSubnetIds;

    public InfrastructureData(
            String region,
            String serverDataBaseName,
            String resourceBucketName,
            String serverRoleId,
            String serverInstanceProfileArn,
            String serverSecurityGroupName,
            String serverVpcId,
            List<String> serverSubnetIds
    ) {
        this.region = Region.of(region.toLowerCase().replace('_', '-'));
        this.serverDataBaseName = serverDataBaseName;
        this.resourceBucketName = resourceBucketName;
        this.serverRoleId = serverRoleId;
        this.serverInstanceProfileArn = serverInstanceProfileArn;
        this.serverSecurityGroupName = serverSecurityGroupName;
        this.serverVpcId = serverVpcId;
        this.serverSubnetIds = serverSubnetIds;
    }

    /**
     * Generates an InfrastructureData object from environment variables
     *
     * @return The InfrastructureData object just generated
     */
    public static InfrastructureData fromEnvironment() {
        String region = System.getenv("CLOUDCUBESREGION");
        String serverDataBaseName = System.getenv("CLOUDCUBESSERVERDATABASENAME");
        String resourceBucketName = System.getenv("CLOUDCUBESRESOURCEBUCKETNAME");
        String serverRoleId = System.getenv("CLOUDCUBESSERVERROLEID");
        String serverInstanceProfileArn = System.getenv("CLOUDCUBESSERVERINSTANCEPROFILEARN");
        String serverSecurityGroupName = System.getenv("CLOUDCUBESSERVERSECURITYGROUPNAME");
        String serverVpcId = System.getenv("CLOUDCUBESSERVERVPCID");
        String serverSubnetIdsAsString = System.getenv("CLOUDCUBESSERVERSUBNETIDS");
        List<String> serverSubnetIds = Arrays.asList(serverSubnetIdsAsString.split(","));
        assert region != null;
        assert serverDataBaseName != null;
        assert serverSecurityGroupName != null;
        return new InfrastructureData(
                region,
                serverDataBaseName,
                resourceBucketName,
                serverRoleId,
                serverInstanceProfileArn,
                serverSecurityGroupName,
                serverVpcId,
                serverSubnetIds
        );
    }

    public Region getRegion() {
        return region;
    }

    public String getServerDataBaseName() {
        return serverDataBaseName;
    }

    public String getResourceBucketName() {
        return resourceBucketName;
    }

    public String getServerRoleId() {
        return serverRoleId;
    }

    public String getServerInstanceProfileArn() {
        return serverInstanceProfileArn;
    }

    public String getServerSecurityGroupName() {
        return serverSecurityGroupName;
    }

    public String getServerVpcId() {
        return serverVpcId;
    }

    public List<String> getServerSubnetIds() {
        return serverSubnetIds;
    }

    public HashMap<String, String> convertToMap() {
        HashMap<String, String> outputMap = new HashMap<>();
        outputMap.put("CLOUDCUBESREGION", region.id());
        outputMap.put("CLOUDCUBESSERVERDATABASENAME", serverDataBaseName);
        outputMap.put("CLOUDCUBESRESOURCEBUCKETNAME", resourceBucketName);
        outputMap.put("CLOUDCUBESSERVERROLEID", serverRoleId);
        outputMap.put("CLOUDCUBESSERVERINSTANCEPROFILEARN", serverInstanceProfileArn);
        outputMap.put("CLOUDCUBESSERVERSECURITYGROUPNAME", serverSecurityGroupName);
        outputMap.put("CLOUDCUBESSERVERVPCID", serverVpcId);
        String serverSubnetIdsAsString = String.join(",", serverSubnetIds);
        outputMap.put("CLOUDCUBESSERVERSUBNETIDS", serverSubnetIdsAsString);
        return outputMap;
    }

    /**
     * A helper class used to construct a InfrastructureData object. This class exists because objects of type
     * InfrastructureData cannot be modified. This class allows constructing an InfrastructureData object in parts,
     * allowing this job to be offloaded to different parts of the code and only constructed when finished.
     */
    public static class Builder {
        private String region = null;
        private String serverDataBaseName = null;
        private String resourceBucketName = null;
        private String serverRoleId = null;
        private String serverInstanceProfileArn = null;
        private String serverSecurityGroupName = null;
        private String serverVpcId = null;
        private List<String> serverSubnetIds = null;

        private Builder() {
        }

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

        public String getResourceBucketName() {
            return resourceBucketName;
        }

        public void setResourceBucketName(String resourceBucketName) {
            this.resourceBucketName = resourceBucketName;
        }

        public Builder withResourceBucketName(String resourceBucketName) {
            setResourceBucketName(resourceBucketName);
            return this;
        }

        public String getServerRoleId() {
            return serverRoleId;
        }

        public void setServerRoleId(String serverRoleId) {
            this.serverRoleId = serverRoleId;
        }

        public Builder withServerRoleId(String serverRoleId) {
            setServerRoleId(serverRoleId);
            return this;
        }

        public String getServerInstanceProfileArn() {
            return serverInstanceProfileArn;
        }

        public void setServerInstanceProfileArn(String serverInstanceProfileArn) {
            this.serverInstanceProfileArn = serverInstanceProfileArn;
        }

        public Builder withServerInstanceProfileArn(String serverInstanceProfileArn) {
            setServerInstanceProfileArn(serverInstanceProfileArn);
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

        public List<String> getServerSubnetIds() {
            return serverSubnetIds;
        }

        public void setServerSubnetIds(List<String> serverSubnetIds) {
            this.serverSubnetIds = serverSubnetIds;
        }

        public Builder withServerSubnetIds(List<String> serverSubnetIds) {
            setServerSubnetIds(serverSubnetIds);
            return this;
        }

        /**
         * Returns true if all the values that are required to build an InfrastructureData object have been set.
         *
         * @return If all the values required to build an InfrastructureData object have been set.
         */
        public boolean canBeConstructed() {
            return region != null
                    && serverDataBaseName != null
                    && resourceBucketName != null
                    && serverRoleId != null
                    && serverInstanceProfileArn != null
                    && serverSecurityGroupName != null
                    && serverVpcId != null
                    && serverSubnetIds != null;
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
            return new InfrastructureData(
                    region,
                    serverDataBaseName,
                    resourceBucketName,
                    serverRoleId,
                    serverInstanceProfileArn,
                    serverSecurityGroupName,
                    serverVpcId,
                    serverSubnetIds
            );
        }
    }
}
