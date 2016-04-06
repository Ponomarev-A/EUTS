package model.tests;

import model.Receiver;
import model.Stand;

import java.util.Arrays;

import static model.Receiver.MAX_LEVEL;

/**
 * Sample test case class
 */
class AFEqualSignalLevelsTest extends AnalogFilterTest {

    private static final double DELTA_LEVEL_PRT = 10.0;

    AFEqualSignalLevelsTest(final int frequency_Hz, final int gain_dB) {
        super(String.format("Analog filter: equal signal levels (%d Hz, %d dB)", frequency_Hz, gain_dB),
                gain_dB,
                frequency_Hz);
    }

    @Override
    public void runTest(Receiver receiver, Stand stand) throws Exception, Error {

        setUp(stand);
        setUp(receiver);
        short[] levels = autoSetVoltage(stand, receiver, receiverGain_dB, MIN_LEVEL_PRT, MAX_LEVEL_PRT, INIT_LEVEL_PRT);

        int maxLevel = findMaxLevel(Arrays.copyOfRange(levels, 0, 4));
        int minLevel = findMinLevel(Arrays.copyOfRange(levels, 0, 4));

        double diffLevelsExpected = DELTA_LEVEL_PRT;
        double diffLevelsActual = (double) (maxLevel - minLevel) * 100.0 / MAX_LEVEL;

        assertTrue(String.format(
                "The difference between max and min levels of signal is exceeded.%-12s: <%5.2f%% %-12s:  %5.2f%%",
                "\nExpected: ", diffLevelsExpected,
                "\nActual:", diffLevelsActual),
                diffLevelsActual <= diffLevelsExpected
        );
    }
}
