package osbourn.cloudcubes.core.server;

/**
 * Manages the launching and stopping of an AWS server, such as an EC2 instance or an EC2 spot instance
 */
public interface InstanceManager {
    /**
     * Starts the server
     *
     * @throws InstanceAlreadyOnlineException If the server is already online
     */
    void startServer();

    /**
     * Stops the server
     *
     * @throws InstanceAlreadyOfflineException If the server is already offline
     */
    void stopServer();

    /**
     * Returns true if the server is online. Note that this method may perform additional calculations if the server
     * state is unknown at the time.
     *
     * @return true if the server is online, false otherwise
     */
    boolean isServerOnline();

    class InstanceStateException extends IllegalStateException {
        public InstanceStateException(String message) {
            super(message);
        }
    }

    class InstanceAlreadyOnlineException extends IllegalStateException {
        public InstanceAlreadyOnlineException() {
            super("Instance was already online");
        }
    }

    class InstanceAlreadyOfflineException extends IllegalStateException {
        public InstanceAlreadyOfflineException() {
            super("Instance was already offline");
        }
    }
}
