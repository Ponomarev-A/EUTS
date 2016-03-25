package model;

import packet.Command;

enum ConnectionStatus {

    CONNECTED,
    DISCONNECTED;

    private static ConnectionStatus ConnectionStatus;

    public static ConnectionStatus checkStatus(Device device) throws Exception {

        if (device == null)
            return ConnectionStatus;

        Command command;
        if (device instanceof Receiver)
            command = Command.CHECK_CONNECTION_DEVICE;
        else
            command = Command.CHECK_CONNECTION_STAND;

        int request = (int) (System.currentTimeMillis() / 1000);
        int response = device.getInteger(command, request);

        ConnectionStatus = (response == request) ? CONNECTED : DISCONNECTED;

        return ConnectionStatus;
    }
}