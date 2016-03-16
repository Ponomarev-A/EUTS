package model;

import connections.ConnectionManager;
import controller.Controller;
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

    public Receiver(ConnectionManager connectionManager, Controller controller) {
        super(connectionManager, controller);
    }


    @Override
    public boolean readInfo() {

        set(Command.GET_INFO_DEVICE);

        try {
            String info = getString();
            String[] infoDetails = info.trim().split(" ");

            if (infoDetails.length == 4) {
                model = infoDetails[0];
                firmware = infoDetails[1];
                scheme = infoDetails[2];
                ID = infoDetails[3];

                return true;
            }
        } catch (Exception e) {
            controller.showErrorMessage(
                    "Read receiver info",
                    "Read " + this + " info is failed.\nTry again connect to device!",
                    e);
        }

        return false;
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
