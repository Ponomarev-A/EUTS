package model.tests;

import model.Receiver;
import model.Stand;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static packet.Command.*;

/**
 * Sample test case class
 */
public class AnalogFilterTest extends BaseTestCase {

    public static final double INIT_LEVEL_PRT = 95.0;
    public static final double STEP_LEVEL_PRT = 0.5;
    public static final double DIFF_LEVEL_PRT = 0.0015;
    public static final double MIN_LEVEL_PRT = 0.950;
    public static final double MAX_LEVEL_PRT = 0.999;

    private final int freq;
    private final int gain;

    public AnalogFilterTest(final int freq, final int gain, Receiver receiver, Stand stand) {
        super(String.format("Analog filter (%d Hz, %d dB)", freq, gain), receiver, stand);
        this.freq = freq;
        this.gain = gain;
    }


    @Override
    public void runTest() throws Exception, Error {

        int voltage_mcV = stand.calcVoltage(INIT_LEVEL_PRT, gain);
        int frequency_Hz = freq;
        int voltage_step_mcV = stand.calcVoltage(STEP_LEVEL_PRT, gain);

        stand.set(FREQUENCY_STAND, frequency_Hz);

        receiver.set(MODE_DEVICE, Receiver.Modes.MODE_TESTLEVELS.ordinal());
        receiver.set(FREQUENCY_DEVICE, frequency_Hz);
        receiver.set(GAIN_DEVICE, gain);

        while (true) {
            stand.set(VOLTAGE_STAND, voltage_mcV);
            TimeUnit.MILLISECONDS.sleep(100);

            receiver.set(GET_LEVELS_DEVICE);
            short[] levels = receiver.getArray();

            int maxLevel = findMaxLevel(Arrays.copyOfRange(levels, 0, 4));
            if (maxLevel < MIN_LEVEL_PRT * Receiver.MAX_LEVEL) voltage_mcV += voltage_step_mcV;
            else if (maxLevel > MAX_LEVEL_PRT * Receiver.MAX_LEVEL) voltage_mcV -= voltage_step_mcV;
            else break;
        }

        receiver.set(GET_LEVELS_DEVICE);
        short[] levels = receiver.getArray();
        int maxLevel = findMaxLevel(Arrays.copyOfRange(levels, 0, 4));
        int minLevel = findMinLevel(Arrays.copyOfRange(levels, 0, 4));

        double diffLevelsExpected = DIFF_LEVEL_PRT;
        double diffLevelsActual = (double) (maxLevel - minLevel) / Receiver.MAX_LEVEL;

        assertTrue(
                "The difference between max and min levels of signal is exceeded." +
                        "\nExpected: " + String.format("%.2f%%", diffLevelsExpected * 100) +
                        "\nActual: " + String.format("%.2f%%", diffLevelsActual * 100),
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
