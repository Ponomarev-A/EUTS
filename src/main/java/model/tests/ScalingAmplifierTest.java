package model.tests;

import model.Receiver;
import model.Stand;

import java.util.concurrent.TimeUnit;

import static model.Receiver.MAX_LEVEL;
import static packet.Command.GAIN_DEVICE;
import static packet.Command.GET_LEVELS_DEVICE;

/**
 * Base parent class for AF child classes
 */
class ScalingAmplifierTest extends AnalogFilterTest {

    private static final double INIT_LEVEL_PRT = 35.0;
    private static final double MIN_LEVEL_PRT = 30.0;
    private static final double MAX_LEVEL_PRT = 45.0;
    private static final double DELTA_LEVEL_PRT = 5.0;

    private static final int FREQUENCY = 512;
    private static final int START_GAIN = 18;
    private static final int END_GAIN = 24;

    ScalingAmplifierTest(Receiver receiver, Stand stand) {
        super(String.format("Check scaling amplifier (%d Hz, from %d dB to %d dB)", FREQUENCY, START_GAIN, END_GAIN),
                receiver,
                stand,
                START_GAIN,
                FREQUENCY);
    }

    @Override
    public void runTest() throws Exception, Error {

        setUpStand();
        setUpReceiver();
        short[] beforeLevels = autoSetVoltageStand(receiverGain_dB, MIN_LEVEL_PRT, MAX_LEVEL_PRT, INIT_LEVEL_PRT);

        // Set up new gain and wait completion transient process on receiver
        receiver.set(GAIN_DEVICE, END_GAIN);
        TimeUnit.MILLISECONDS.sleep(WAIT_CHANGE_VOLTAGE_MS);

        short[] afterLevels = receiver.getArray(GET_LEVELS_DEVICE);

        for (int i = 0; i < 4; i++) {

            double before_prt = beforeLevels[i] * 100.0 / MAX_LEVEL;
            double after_prt = afterLevels[i] * 100.0 / MAX_LEVEL;

            before_prt *= Math.pow(10, (END_GAIN - START_GAIN) / 20.0);

            double lowBound_prt = before_prt * (100.0 - DELTA_LEVEL_PRT) / 100;
            double highBound_prt = before_prt * (100.0 + DELTA_LEVEL_PRT) / 100;

            assertTrue(String.format(
                    "The level of signal on channel #%d is out of range.%-12s: %5.2f%%...%5.2f%% %-12s: %5.2f%%",
                    i + 1,
                    "\nExpected: ", lowBound_prt, highBound_prt,
                    "\nActual:", after_prt),
                    after_prt >= lowBound_prt && after_prt <= highBound_prt
            );
        }
    }
}
