package model;

import connections.ConnectionManager;
import controller.Controller;
import packet.Command;

/**
 * Stand class: all information about current used fot tests stand
 */
public class Stand extends Device {

    private static final int MAX_RECEIVER_ADC_VOLTAGE_MCV = 1500000;
    private String firmware;
    private String scheme;
    private Integer ID;

    Stand(ConnectionManager connectionManager, Controller controller) {
        super(connectionManager, controller);
    }


    @Override
    public boolean readInfo() {
        try {
            set(Command.GET_INFO_STAND);
            String info = getString();
            String[] infoDetails = info.trim().split(" ");

            if (infoDetails.length == 3) {
                firmware = infoDetails[0];
                scheme = infoDetails[1];
                ID = Integer.valueOf(infoDetails[2]);

                return true;
            }
        } catch (Exception e) {
            controller.showErrorMessage(
                    "Read stand info",
                    "Read " + this + " info is failed.\nTry again connect to device!",
                    e);
        }

        return false;
    }

    @Override
    public Integer getID() {
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
        return "Stand {" +
                "firmware = " + firmware +
                ", scheme = " + scheme +
                ", ID = " + ID + "}";
    }

    public int calcVoltage(double level_prt, int gain) {
        double voltage_mcV = level_prt / 100 * MAX_RECEIVER_ADC_VOLTAGE_MCV / Math.pow(10, (gain + 22) / 20.0);
        return (int) voltage_mcV;
    }
}
