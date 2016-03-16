package model;

import connections.ConnectionManager;
import controller.Controller;
import packet.Command;
import packet.Packet;

/**
 * Device class: all information about current testing device
 */
public abstract class Device {

    final ConnectionManager connectionManager;
    final Controller controller;
    ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;

    public Device(ConnectionManager connectionManager, Controller controller) {
        this.connectionManager = connectionManager;
        this.controller = controller;
    }

    public abstract boolean readInfo();

    public abstract String getID();

    public abstract String getModel();

    public abstract String getScheme();

    public abstract String getFirmware();

    public void checkConnectionStatus() {
        try {
            connectionStatus = (connectionManager.getConnection().isOpened()) ?
                    ConnectionStatus.checkStatus(this) :
                    ConnectionStatus.DISCONNECTED;
        } catch (Exception e) {
            connectionStatus = ConnectionStatus.DISCONNECTED;
            controller.showErrorMessage(
                    "Check " + this + " connection status",
                    "Read " + this + " connection status is failed.\nTry again connect to device!",
                    e);
        }
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public void set(Command command) {
        set(new Packet(command));
    }

    public void set(Packet packet) {
        try {
            connectionManager.sendPacket(packet);
        } catch (Exception e) {
            controller.showErrorMessage(
                    "Send packet fail",
                    String.format("Can't send command %s\n with value %s\n to device %s",
                            packet.getCommand(), packet.getDataAsInt(), this.toString()),
                    e);
            e.printStackTrace();
        }
    }

    public void set(Command command, Integer integerValue) {
        set(new Packet(command, integerValue));
    }

    public short[] getArray() throws Exception {
        return get().getDataAsShortArray();
    }

    public Packet get() throws Exception {
        return connectionManager.receivePacket();
    }

    public int getInteger() throws Exception {
        return get().getDataAsInt();
    }

    public String getString() throws Exception {
        return get().getDataAsString();
    }




}
