package model;

import packet.Command;

public enum ConnectionStatus {

    CONNECTED,
    DISCONNECTED;

    private static ConnectionStatus ConnectionStatus;

    public static ConnectionStatus checkStatus(Device device) {

        if (device == null)
            return ConnectionStatus;

        Command command;
        if (device instanceof Receiver)
            command = Command.CHECK_CONNECTION_DEVICE;
        else
            command = Command.CHECK_CONNECTION_STAND;

        int request = (int) (System.currentTimeMillis() / 1000);
        device.set(command, request);
        int response = device.getInteger();

        ConnectionStatus = (response == request) ? CONNECTED : DISCONNECTED;

        return ConnectionStatus;
    }
}