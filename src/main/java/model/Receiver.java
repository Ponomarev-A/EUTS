package model;

import connections.ConnectionManager;
import packet.Command;

/**
 * Receiver class: all information about connected receiver
 */
public class Receiver extends Device {

    public final static int MAX_LEVEL = 1024;
    public final static int MIN_LEVEL = 0;

    private String model;
    private String firmware;
    private String scheme;
    private String ID;

    public Receiver(ConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    public boolean readInfo() {
        boolean result = false;

        // Request for device model name
        if (set(Command.GET_INFO_DEVICE)) {

            // Wait while answer will be received from device
            String info = getString();

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

    public enum Modes {
        MODE_INT_COMPASS,
        MODE_INT_GRAPH,
        MODE_INT_GRAPH_PLUS,
        MODE_INT_GRAPH_2,
        MODE_INT_IFF,
        MODE_EXT_COMPASS,
        MODE_EXT_MULTIMETER,
        MODE_TESTLEVELS,
        MODE_TESTCOMPASS
    }
}
