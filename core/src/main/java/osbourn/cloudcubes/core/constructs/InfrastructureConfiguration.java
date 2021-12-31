package osbourn.cloudcubes.core.constructs;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class InfrastructureConfiguration {
    private final Map<InfrastructureSetting, String> settings;
    private boolean hasCheckedCompleteness = false;

    public InfrastructureConfiguration() {
        this.settings = new HashMap<>();
    }

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

    public void setValue(@NotNull InfrastructureSetting setting, @NotNull String value) {
        this.settings.put(setting, value);
    }

    public @NotNull String getValue(@NotNull InfrastructureSetting setting) {
        assertCompleteness(String.format("Tried to retrieve value %s when the InfrastructureConfiguration object has" +
                " not yet been constructed", setting.environmentVariableName));
        return Objects.requireNonNull(this.settings.get(setting));
    }

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
