package osbourn.cloudcubes.core.server;

import org.jetbrains.annotations.NotNull;

/**
 * Manages the launching and stopping of an AWS server, such as an EC2 instance or an EC2 spot instance
 */
public interface InstanceManager {
    /**
     * Starts the server if state is ONLINE, and stops the server if state is OFFLINE
     *
     * @param state The state to set the server to
     * @return true if the server was launched or stopped, false if it was already in the requested state
     */
    boolean setState(@NotNull ServerState state);

    /**
     * Gets whether the server is online or offline. Note that this method may perform additional calculations if the
     * server state is unknown at the time.
     *
     * @return ONLINE if the server is online, OFFLINE if it is offline
     */
    ServerState getState();
}
