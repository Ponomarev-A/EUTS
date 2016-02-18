package model;

import packet.Command;
import packet.Packet;

public enum ConnectionStatus {

    CONNECTED,
    DISCONNECTED,
    ERROR;

    private static ConnectionStatus status;

    public static ConnectionStatus checkStatus(Device device) {

        int currentTimeSec = (int) (System.currentTimeMillis() / 1000);
        Command command = null;
        if (device instanceof Receiver)
            command = Command.CHECK_CONNECTION_DEVICE;
        else if (device instanceof Stand)
            command = Command.CHECK_CONNECTION_STAND;
        else
            status = ERROR;


        if (command != null && device.getConnectionManager().sendPacket(new Packet(command, currentTimeSec))) {

            int callbackTimeSec = device.getConnectionManager().receivePacket().getDataAsInt();
            status = (callbackTimeSec == currentTimeSec) ? CONNECTED : ERROR;

        } else {
            status = DISCONNECTED;
        }

        return status;
    }
}