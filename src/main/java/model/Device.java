package model;

import connections.ConnectionManager;
import controller.Controller;
import exception.FailSendPacket;
import packet.Command;
import packet.Packet;

/**
 * Device class: all information about current testing device
 */
public abstract class Device {

    private final ConnectionManager connectionManager;
    private final Controller controller;

    private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;

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
        connectionStatus = (connectionManager.getConnection().isOpened()) ?
                ConnectionStatus.checkStatus(this) :
                ConnectionStatus.DISCONNECTED;
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public void set(Command command) {
        try {
            if (!connectionManager.sendPacket(new Packet(command)))
                controller.showErrorMessage("Send packet fail",
                        new FailSendPacket("Can't send command " + command + "\n  to " + this));
        } catch (Exception e) {
            controller.showErrorMessage("Send packet fail", e);
            e.printStackTrace();
        }
    }

    public void set(Command command, Short shortValue) {
        try {
            if (!connectionManager.sendPacket(new Packet(command, shortValue)))
                controller.showErrorMessage("Send packet fail",
                        new FailSendPacket("Can't send command " + command + "\n  with value " + shortValue + "\n  to " + this));
        } catch (Exception e) {
            controller.showErrorMessage("Send packet fail", e);
            e.printStackTrace();
        }
    }

    public void set(Command command, Integer integerValue) {
        try {
            if (!connectionManager.sendPacket(new Packet(command, integerValue)))
                controller.showErrorMessage("Send packet fail",
                        new FailSendPacket("Can't send command " + command + "\n  with value " + integerValue + "\n  to " + this));
        } catch (Exception e) {
            controller.showErrorMessage("Send packet fail", e);
            e.printStackTrace();
        }
    }

    public short[] getArray() {
        try {
            return connectionManager.receivePacket().getDataAsShortArray();
        } catch (Exception e) {
            controller.showErrorMessage("Receive packet fail", e);
            e.printStackTrace();
        }
        return new short[0];
    }

    public short getShort() {
        try {
            return connectionManager.receivePacket().getDataAsShort();
        } catch (Exception e) {
            controller.showErrorMessage("Receive packet fail", e);
            e.printStackTrace();
        }
        return 0;
    }

    public int getInteger() {
        try {
            return connectionManager.receivePacket().getDataAsInt();
        } catch (Exception e) {
            controller.showErrorMessage("Receive packet fail", e);
            e.printStackTrace();
        }
        return 0;
    }

    public String getString() {
        try {
            return connectionManager.receivePacket().getDataAsString();
        } catch (Exception e) {
            controller.showErrorMessage("Receive packet fail", e);
            e.printStackTrace();
        }
        return "";
    }




}
