package model;

import connections.ConnectionManager;

/**
 * Device class: all information about current testing device
 */
public abstract class Device {

    private ConnectionManager connectionManager;

    public Device(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public abstract String getInfo();

    public ConnectionStatus getConnectionStatus() {
        return ConnectionStatus.checkStatus(this);
    }
}
