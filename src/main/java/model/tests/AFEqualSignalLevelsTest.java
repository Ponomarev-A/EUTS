package model.tests;

import model.Receiver;
import model.Stand;

import java.util.Arrays;

import static model.Receiver.MAX_LEVEL;

/**
 * Sample test case class
 */
class AFEqualSignalLevelsTest extends AnalogFilterTest {

    private static final double DIFF_LEVEL_PRT = 15.0;

    AFEqualSignalLevelsTest(final int frequency_Hz, final int gain_dB, Receiver receiver, Stand stand) {
        super(String.format("Analog filter: equal signal levels (%d Hz, %d dB)", frequency_Hz, gain_dB),
                receiver,
                stand,
                gain_dB,
                frequency_Hz);
    }

    @Override
    public void runTest() throws Exception, Error {

        setUpStand();
        setUpReceiver();
        short[] levels = autoSetVoltageStand(receiverGain_dB, MIN_LEVEL_PRT, MAX_LEVEL_PRT, INIT_LEVEL_PRT);

        int maxLevel = findMaxLevel(Arrays.copyOfRange(levels, 0, 4));
        int minLevel = findMinLevel(Arrays.copyOfRange(levels, 0, 4));

        double diffLevelsExpected = DIFF_LEVEL_PRT;
        double diffLevelsActual = (double) (maxLevel - minLevel) * 100.0 / MAX_LEVEL;

        assertTrue(
                "The difference between max and min levels of signal is exceeded." +
                        "\nExpected: " + String.format("%.2f%%", diffLevelsExpected) +
                        "\nActual: " + String.format("%.2f%%", diffLevelsActual),
                diffLevelsActual <= diffLevelsExpected);
    }
}
