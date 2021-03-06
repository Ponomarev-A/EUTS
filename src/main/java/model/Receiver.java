package model;

import connections.ConnectionManager;
import controller.Controller;
import packet.Command;

import java.util.Arrays;
import java.util.List;

/**
 * Receiver class: all information about connected receiver
 */
public class Receiver extends Device {

    public final static int MAX_LEVEL = 1023;
    public final static int MIN_LEVEL = 0;

    public static final List<Integer> FREQUENCY_HZ = Arrays.asList(
            50,
            60,
            100,
            120,
            512,
            1024,
            8192,
            32768
    );
    private String model;
    private String firmware;
    private String scheme;

    private Integer ID;

    public Receiver(Controller controller, ConnectionManager CM) {
        super(controller, CM);
    }

    public Receiver() {
        super();
        this.ID = null;
        this.model = null;
        this.scheme = null;
        this.firmware = null;
    }


    public Receiver(Integer ID, String model, String scheme, String firmware) {
        super();
        this.ID = ID;
        this.model = model;
        this.scheme = scheme;
        this.firmware = firmware;
    }


    @Override
    public boolean readInfo() {
        try {
            String info = getString(Command.GET_INFO_DEVICE);
            String[] infoDetails = info.trim().split(" ");

            if (infoDetails.length == 4) {
                model = infoDetails[0];
                firmware = infoDetails[1];
                scheme = infoDetails[2];
                ID = Integer.valueOf(infoDetails[3]);

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
    public Integer getID() {
        return ID;
    }

    void setID(Integer ID) {
        this.ID = ID;
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
    public int hashCode() {
        int result = getModel().hashCode();
        result = 31 * result + getFirmware().hashCode();
        result = 31 * result + getScheme().hashCode();
        result = 31 * result + getID().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Receiver receiver = (Receiver) o;

        if (!getModel().equals(receiver.getModel())) return false;
        if (!getFirmware().equals(receiver.getFirmware())) return false;
        if (!getScheme().equals(receiver.getScheme())) return false;
        return getID().equals(receiver.getID());

    }

    @Override
    public String toString() {
        return "Receiver {" +
                "model = " + model +
                ", firmware = " + firmware +
                ", scheme = " + scheme +
                ", ID = " + ID + "}";
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

    public enum BSType {FILTER, WB, RADIO}

    public enum SintezSound {OFF, ON}

}
