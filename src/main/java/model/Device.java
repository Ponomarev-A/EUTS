package model;

import connections.ConnectionManager;

/**
 * Device class: all information about current testing device
 */
public abstract class Device {

    private ConnectionManager connectionManager;
    private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;

    public Device(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public abstract String getInfo();

    public void checkConnectionStatus() {
        connectionStatus = ConnectionStatus.checkStatus(this);
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
    }
}
