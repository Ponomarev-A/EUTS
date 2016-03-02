package model;

import connections.ConnectionManager;
import packet.Command;
import packet.Packet;

/**
 * Receiver class: all information about connected receiver
 */
public class Receiver extends Device {

    private ConnectionManager connectionManager;

    private String model;
    private String firmware;
    private String scheme;
    private String ID;

    public Receiver(ConnectionManager connectionManager) {
        super(connectionManager);
        this.connectionManager = connectionManager;
    }

    @Override
    public boolean readInfo() {
        boolean result = false;

        // Request for device model name
        if (connectionManager.sendPacket(new Packet(Command.GET_INFO_DEVICE))) {

            // Wait while answer will be received from device
            String info = connectionManager.receivePacket().getDataAsString();

            String[] infoDetails = info.trim().split(" ");
            if (infoDetails.length == 4) {
                model = infoDetails[0];
                firmware = infoDetails[1];
                scheme = infoDetails[2];
                ID = infoDetails[3];

                result = true;
            }
        }

        return result;
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public String getFirmware() {
        return firmware;
    }

    @Override
    public String toString() {
        return "Receiver { " +
                "model = " + model +
                ", firmware = " + firmware +
                ", scheme = " + scheme +
                ", ID= " + ID + " }";
    }
}
