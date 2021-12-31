package osbourn.cloudcubes.core.constructs;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * This class functions as a centralized way to keep track of information about the infrastructure objects that are
 * created when the CDK stack is deployed. Specifically, it stores the ids and names of resources in the environment as
 * well as some other information such as the region.
 * </p>
 *
 * <p>
 * This class serves to help construct those environment variables by containing the values that will be sent to the
 * Lambda functions. A InfrastructureData object will be constructed during `cdk synth` and it will be converted to
 * a HashMap of environment variables. The lambda functions will read the environment variables and turn it back into
 * an InfrastructureConfiguration object.
 * </p>
 */
public final class InfrastructureConfiguration {
    private final Map<InfrastructureSetting, String> settings;
    private boolean hasCheckedCompleteness = false;

    /**
     * Create a new, empty instance of the InfrastructureConfiguration object that is suitable for storing
     * infrastructure information in. This constructor is intended to be called during 'cdk synth' so that information
     * about the infrastructure can be recorded in it and passed to Lambda functions.
     */
    public InfrastructureConfiguration() {
        this.settings = new HashMap<>();
    }

    /**
     * Construct an InfrastructureConfiguration object from the values stored in environment variables. This method is
     * intended to be called from within Lambda functions.
     *
     * @return The InfrastructureConfiguration object that was just constructed
     * @throws IncompleteInfrastructureConfigurationException If the environment variables did not contain all the
     * necessary values to construct an InfrastructureConfiguration object
     */
    public static @NotNull InfrastructureConfiguration fromEnvironment() {
        InfrastructureConfiguration configuration = new InfrastructureConfiguration();
        for (InfrastructureSetting setting : InfrastructureSetting.values()) {
            String settingValue = System.getenv(setting.environmentVariableName);
            if (settingValue == null) {
                throw new IncompleteInfrastructureConfigurationException("Environment did not contain all values" +
                        " needed to construct an InfrastructureConfiguration object");
            }
            configuration.setValue(setting, settingValue);
        }
        return configuration;
    }

    /**
     * Set the value of the specified setting.
     *
     * @param setting The setting whose value should be set
     * @param value The value to set the setting to
     */
    public void setValue(@NotNull InfrastructureSetting setting, @NotNull String value) {
        this.settings.put(setting, value);
    }

    /**
     * Get the value of the specified setting. This method will throw an exception if the InfrastructureConfiguration
     * object does not yet have all required values set.
     *
     * @param setting The setting to get the value of
     * @return The value of the setting
     * @throws IncompleteInfrastructureConfigurationException If the configuration is not yet complete
     */
    public @NotNull String getValue(@NotNull InfrastructureSetting setting) {
        assertCompleteness(String.format("Tried to retrieve value %s when the InfrastructureConfiguration object has" +
                " not yet been constructed", setting.environmentVariableName));
        return Objects.requireNonNull(this.settings.get(setting));
    }

    /**
     * Generate a Map of environment variable names and their corresponding values that can later be read by
     * {@link #getValue(InfrastructureSetting)}. This method is intended to be called from within the infrastructure
     * project during 'cdk synth'.
     *
     * @return A map of environment variables that represents the InfrastructureConfiguration object.
     */
    public Map<String, String> toEnvironmentVariableMap() {
        assertCompleteness("Tried to construct environment variable map when the " +
                        "InfrastructureConfiguration object was not complete");
        HashMap<String, String> environmentVariables = new HashMap<>();
        for (Map.Entry<InfrastructureSetting, String> entry : this.settings.entrySet()) {
            environmentVariables.put(entry.getKey().environmentVariableName, entry.getValue());
        }
        return environmentVariables;
    }

    private void assertCompleteness(String errorMessage) {
        if (!this.hasCheckedCompleteness) {
            if (!this.isComplete()) {
                throw new IncompleteInfrastructureConfigurationException(errorMessage);
            }
            this.hasCheckedCompleteness = true;
        }
    }

    @Contract(pure = true)
    private boolean isComplete() {
        for (InfrastructureSetting setting : InfrastructureSetting.values()) {
            if (!this.settings.containsKey(setting)) {
                return false;
            }
        }
        return true;
    }

    public enum InfrastructureSetting {
        REGION("REGION");
        private final @NotNull String environmentVariableName;

        InfrastructureSetting(@NotNull String environmentVariableName) {
            this.environmentVariableName = environmentVariableName;
        }
    }

    /**
     * Thrown when trying to read values of an infrastructure configuration when the object hasn't been fully
     * constructed yet.
     */
    public static class IncompleteInfrastructureConfigurationException extends IllegalStateException {
        private IncompleteInfrastructureConfigurationException(String errorMessage) {
            super(errorMessage);
        }
    }
}
