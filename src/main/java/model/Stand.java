package model;

import connections.ConnectionManager;
import controller.Controller;
import packet.Command;

/**
 * Stand class: all information about current used fot tests stand
 */
public class Stand extends Device {

    private String firmware;
    private String scheme;
    private String ID;

    public Stand(ConnectionManager connectionManager, Controller controller) {
        super(connectionManager, controller);
    }


    @Override
    public boolean readInfo() {
        boolean result = false;

        set(Command.GET_INFO_STAND);
        String info = getString();

        String[] infoDetails = info.trim().split(" ");
        if (infoDetails.length == 3) {
            firmware = infoDetails[0];
            scheme = infoDetails[1];
            ID = infoDetails[2];

            result = true;
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
