package model;

import connections.ConnectionManager;
import packet.Command;
import packet.Packet;

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

    public abstract boolean readInfo();

    public abstract String getID();

    public abstract String getModel();

    public abstract String getScheme();

    public abstract String getFirmware();

    public void checkConnectionStatus() {
        connectionStatus = ConnectionStatus.checkStatus(this);
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public boolean set(Command command) {
        return connectionManager.sendPacket(new Packet(command));
    }

    public boolean set(Command command, Short shortValue) {
        return connectionManager.sendPacket(new Packet(command, shortValue));
    }

    public boolean set(Command command, Integer integerValue) {
        return connectionManager.sendPacket(new Packet(command, integerValue));
    }

    public short[] getArray() {
        return connectionManager.receivePacket().getDataAsShortArray();
    }

    public short getShort() {
        return connectionManager.receivePacket().getDataAsShort();
    }

    public int getInteger() {
        return connectionManager.receivePacket().getDataAsInt();
    }

    public String getString() {
        return connectionManager.receivePacket().getDataAsString();
    }




}
