package model.tests;

import model.Receiver;
import model.Stand;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static model.Device.ExtSensors;
import static model.Device.SignalType;
import static model.Receiver.*;
import static packet.Command.*;

/**
 * Sample test case class
 */
class AnalogFilterTest extends BaseTestCase {

    private static final double INIT_LEVEL_PRT = 93.0;
    private static final double DIFF_LEVEL_PRT = 15.0;
    private static final double MIN_LEVEL_PRT = 95.0;
    private static final double MAX_LEVEL_PRT = 99.99;

    private static final int WAIT_CHANGE_VOLTAGE_MS = 300;
    private static final int SET_VOLTAGE_ATTEMPTS_COUNT = 10;

    private final int freq;
    private final int gain;

    AnalogFilterTest(final int freq, final int gain, Receiver receiver, Stand stand) {
        super(String.format("Analog filter (%d Hz, %d dB)", freq, gain), receiver, stand);
        this.freq = freq;
        this.gain = gain;
    }

    @Override
    public void runTest() throws Exception, Error {

        int voltage_mcV = stand.calcVoltage(INIT_LEVEL_PRT, gain);
        int frequency_Hz = freq;

        stand.set(EXT_SENSOR_STAND, ExtSensors.INT.ordinal());
//        stand.set(TYPE_OF_SIGNAL_STAND,     SignalType.SOLID.ordinal());
        stand.set(FREQUENCY_STAND, frequency_Hz);

        receiver.set(MODE_DEVICE, Modes.MODE_TESTLEVELS.ordinal());
        receiver.set(TYPE_OF_SIGNAL_DEVICE, SignalType.SOLID.ordinal());
        receiver.set(BOTTOM_SENSOR_DEVICE, BSType.FILTER.ordinal());
        receiver.set(FREQUENCY_DEVICE, frequency_Hz);
        receiver.set(GAIN_DEVICE, gain);

        int attempts = SET_VOLTAGE_ATTEMPTS_COUNT;
        while (true) {
            stand.set(VOLTAGE_STAND, voltage_mcV);

            // Delay to normalize receiver levels after stand set new voltage
            TimeUnit.MILLISECONDS.sleep(WAIT_CHANGE_VOLTAGE_MS);

            receiver.set(GET_LEVELS_DEVICE);
            short[] levels = receiver.getArray();

            double maxLevel_prt = findMaxLevel(Arrays.copyOfRange(levels, 0, 4)) * 100 / MAX_LEVEL;

            if (maxLevel_prt < MIN_LEVEL_PRT || maxLevel_prt > MAX_LEVEL_PRT) {
                voltage_mcV += stand.calcVoltage((MAX_LEVEL_PRT + MIN_LEVEL_PRT) / 2 - maxLevel_prt, gain);
                if (attempts-- < 0)
                    throw new Exception(String.format(
                            "Impossible to set level of signal to range (%.2f%%, %.2f%%)",
                            MIN_LEVEL_PRT, MAX_LEVEL_PRT));
            } else
                break;
        }

        receiver.set(GET_LEVELS_DEVICE);
        short[] levels = receiver.getArray();
        int maxLevel = findMaxLevel(Arrays.copyOfRange(levels, 0, 4));
        int minLevel = findMinLevel(Arrays.copyOfRange(levels, 0, 4));

        double diffLevelsExpected = DIFF_LEVEL_PRT;
        double diffLevelsActual = (double) (maxLevel - minLevel) * 100 / MAX_LEVEL;

        assertTrue(
                "The difference between max and min levels of signal is exceeded." +
                        "\nExpected: " + String.format("%.2f%%", diffLevelsExpected) +
                        "\nActual: " + String.format("%.2f%%", diffLevelsActual),
                diffLevelsActual <= diffLevelsExpected);
    }

    private int findMaxLevel(short[] levels) {
        short result = Short.MIN_VALUE;
        for (short level : levels) {
            result = (short) Math.max(level, result);
        }

        return result;
    }

    private int findMinLevel(short[] levels) {
        short result = Short.MAX_VALUE;
        for (short level : levels) {
            result = (short) Math.min(level, result);
        }

        return result;
    }
}
