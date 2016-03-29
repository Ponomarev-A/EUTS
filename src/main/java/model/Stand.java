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
            String info = getString(Command.GET_INFO_STAND);
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

    public int calcVoltage(double level_prt, int gain_dB, int frequency_Hz) {

        // Fix calculation for 32768 Hz receiver freq
        double cascade_gain_dB = (frequency_Hz == 32768) ? 22.5 : 22.0;

        double voltage_mcV = level_prt / 100.0 * MAX_RECEIVER_ADC_VOLTAGE_MCV / Math.pow(10, (gain_dB + cascade_gain_dB) / 20.0);
        return (int) voltage_mcV;
    }
}
