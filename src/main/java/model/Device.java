package model;

import connections.ConnectionManager;
import packet.Command;
import packet.Packet;

/**
 * Device class: all information about current testing device
 */
public class Device {
    private ConnectionManager connectionManager;
    private String info = null;
    private Status status;

    public Device(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public String getInfo() {

        // Request for device model name
        if (connectionManager.sendPacket(new Packet(Command.GET_INFO_DEVICE))) {

            // Wait while answer will be received from device
            info = connectionManager.receivePacket().getDataAsString();
        }

        return info.trim();
    }

    public boolean isConnected() {
        return checkStatus() == Status.CONNECTED;
    }

    private Status checkStatus() {

        int currentTimeSec = (int) (System.currentTimeMillis() / 1000);

        if (connectionManager.sendPacket(new Packet(Command.CHECK_CONNECTION_DEVICE, currentTimeSec))) {

            int callbackTimeSec = connectionManager.receivePacket().getDataAsInt();
            status = (callbackTimeSec == currentTimeSec) ? Status.CONNECTED : Status.ERROR;

        } else {
            status = Status.DISCONNECTED;
        }

        return status;
    }


    enum Status {
        CONNECTED,
        DISCONNECTED,
        ERROR
    }
}
