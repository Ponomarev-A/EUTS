package model;

import packet.Command;
import packet.Packet;

public enum ConnectionStatus {

    CONNECTED,
    DISCONNECTED;

    private static ConnectionStatus ConnectionStatus;

    public static ConnectionStatus checkStatus(Device device) {

        int request = (int) (System.currentTimeMillis() / 1000);

        Command command = null;
        if (device instanceof Receiver)
            command = Command.CHECK_CONNECTION_DEVICE;
        else if (device instanceof Stand)
            command = Command.CHECK_CONNECTION_STAND;

        if (command != null && device.getConnectionManager().sendPacket(new Packet(command, request))) {

            int response = device.getConnectionManager().receivePacket().getDataAsInt();
            ConnectionStatus = (response == request) ? CONNECTED : DISCONNECTED;
        } else {
            ConnectionStatus = DISCONNECTED;
        }

        return ConnectionStatus;
    }
}