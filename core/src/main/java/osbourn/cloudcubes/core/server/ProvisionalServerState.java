package osbourn.cloudcubes.core.server;

/**
 * The ServerState as it appears in the database. This differs from {@link ServerState} because it contains an UNKNOWN
 * value, which this will be set to if the server state is unknown. In general, it is necessary to verify the server
 * state if the state is UNKNOWN.
 */
public enum ProvisionalServerState {
    OFFLINE,
    ONLINE,
    UNKNOWN
}
