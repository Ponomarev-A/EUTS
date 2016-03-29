package model.tests;

import model.Device;
import model.Receiver;
import model.Stand;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static model.Receiver.MAX_LEVEL;
import static packet.Command.*;

/**
 * Base parent class for AF child classes
 */
abstract class AnalogFilterTest extends BaseTestCase {

    static final int WAIT_CHANGE_VOLTAGE_MS = 300;
    static final double INIT_LEVEL_PRT = 93.0;
    static final double MIN_LEVEL_PRT = 95.0;
    static final double MAX_LEVEL_PRT = 99.99;

    private static final int SET_VOLTAGE_ATTEMPTS_COUNT = 15;

    final int receiverFrequency_Hz;
    final int receiverGain_dB;

    AnalogFilterTest(String name, Receiver receiver, Stand stand, final int receiverGain_dB, final int receiverFrequency_Hz) {
        super(name, receiver, stand);
        this.receiverGain_dB = receiverGain_dB;
        this.receiverFrequency_Hz = receiverFrequency_Hz;
    }

    @Override
    public abstract void runTest() throws Exception, Error;

    short[] autoSetVoltageStand(int initGain, double minLevelPrt, double maxLevelPrt, double initLevelPrt) throws Exception {

        int voltage_mcV = stand.calcVoltage(initLevelPrt, initGain, receiverFrequency_Hz);
        int attempts = SET_VOLTAGE_ATTEMPTS_COUNT;

        double maxLevel_prt = 0;
        while (attempts-- > 0) {

            // Set up new voltage and wait completion transient process on receiver
            stand.set(VOLTAGE_STAND, voltage_mcV);
            TimeUnit.MILLISECONDS.sleep(WAIT_CHANGE_VOLTAGE_MS);

            short[] levels = receiver.getArray(GET_LEVELS_DEVICE);
            maxLevel_prt = findMaxLevel(Arrays.copyOfRange(levels, 0, 4)) * 100.0 / MAX_LEVEL;

            if (maxLevel_prt < minLevelPrt || maxLevel_prt > maxLevelPrt) {

                int voltage_step_mcV = stand.calcVoltage((maxLevelPrt + minLevelPrt) / 2 - maxLevel_prt, initGain, receiverFrequency_Hz);
                voltage_mcV += (voltage_step_mcV < 0) ? voltage_step_mcV * attempts : voltage_step_mcV;

            } else {
                return levels;
            }
        }

        throw new Exception(String.format(
                "Impossible to set level of signal %.2f%% to range (%.2f%%, %.2f%%)",
                maxLevel_prt, minLevelPrt, maxLevelPrt));
    }

    int findMaxLevel(short[] levels) {
        short result = Short.MIN_VALUE;
        for (short level : levels) {
            result = (short) Math.max(level, result);
        }

        return result;
    }

    void setUpReceiver() throws Exception {
        receiver.set(MODE_DEVICE, Receiver.Modes.MODE_TESTLEVELS.ordinal());
        receiver.set(TYPE_OF_SIGNAL_DEVICE, Device.SignalType.SOLID.ordinal());
        receiver.set(BOTTOM_SENSOR_DEVICE, Receiver.BSType.FILTER.ordinal());
        receiver.set(FREQUENCY_DEVICE, receiverFrequency_Hz);
        receiver.set(GAIN_DEVICE, receiverGain_dB);
    }

    void setUpStand() throws Exception {
        stand.set(EXT_SENSOR_STAND, Device.ExtSensors.INT.ordinal());
//        stand.set(TYPE_OF_SIGNAL_STAND,     SignalType.SOLID.ordinal());
        stand.set(FREQUENCY_STAND, receiverFrequency_Hz);
    }

    int findMinLevel(short[] levels) {
        short result = Short.MAX_VALUE;
        for (short level : levels) {
            result = (short) Math.min(level, result);
        }

        return result;
    }
}
