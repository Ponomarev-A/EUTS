package model;

import connections.ConnectionManager;
import packet.Command;
import packet.Packet;

/**
 * Stand class: all information about current used fot tests stand
 */
public class Stand extends Device {

    private ConnectionManager connectionManager;

    private String firmware;
    private String scheme;
    private String ID;

    public Stand(ConnectionManager connectionManager) {
        super(connectionManager);
        this.connectionManager = connectionManager;
    }

    @Override
    public boolean readInfo() {
        boolean result = false;

        // Request for device model name
        if (connectionManager.sendPacket(new Packet(Command.GET_INFO_STAND))) {

            // Wait while answer will be received from device
            String info = connectionManager.receivePacket().getDataAsString();

            String[] infoDetails = info.trim().split(" ");
            if (infoDetails.length == 3) {
                firmware = infoDetails[0];
                scheme = infoDetails[1];
                ID = infoDetails[2];

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
        return "";
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
        return "Stand { " +
                "firmware = " + firmware +
                ", scheme = " + scheme +
                ", ID= " + ID + " }";
    }
}
