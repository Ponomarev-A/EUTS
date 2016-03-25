package model.tests;

import model.Receiver;
import model.Stand;

import java.util.concurrent.TimeUnit;

import static model.Receiver.MAX_LEVEL;
import static packet.Command.FREQUENCY_STAND;
import static packet.Command.GET_LEVELS_DEVICE;

/**
 * Sample test case class
 */
class AFValidFilterBandpassTest extends AnalogFilterTest {

    private static final double REJECTION_PASSIVE_FREQ = 1.5;
    private static final double REJECTION_ACTIVE_FREQ = 10.0;

    AFValidFilterBandpassTest(final int frequency_Hz, final int gain_dB, Receiver receiver, Stand stand) {
        super(String.format("Analog filter: valid filter bandpass (%d Hz, %d dB)", frequency_Hz, gain_dB),
                receiver,
                stand,
                gain_dB,
                frequency_Hz);
    }

    @Override
    public void runTest() throws Exception, Error {

        setUpStand();
        setUpReceiver();
        short[] beforeLevels = autoSetVoltageStand(receiverGain_dB, MIN_LEVEL_PRT, MAX_LEVEL_PRT, INIT_LEVEL_PRT);

        for (Integer standFrequency_Hz : receiver.frequencyHz) {

            // This frequencies have same analog filter, skip it's frequencies
            if (!isFrequencyTested(standFrequency_Hz, receiverFrequency_Hz))
                continue;

            double rejection = standFrequency_Hz == 100 && receiverFrequency_Hz == 50 ?
                    REJECTION_PASSIVE_FREQ :
                    REJECTION_ACTIVE_FREQ;

            // Set up new frequency and wait completion transient process on receiver
            stand.set(FREQUENCY_STAND, standFrequency_Hz);
            TimeUnit.MILLISECONDS.sleep(WAIT_CHANGE_VOLTAGE_MS);

            short[] afterLevels = receiver.getArray(GET_LEVELS_DEVICE);

            for (int i = 0; i < 4; i++) {

                double before_prt = beforeLevels[i] * 100.0 / MAX_LEVEL;
                double after_prt = afterLevels[i] * 100.0 / MAX_LEVEL;

                before_prt /= rejection;

                assertTrue(
                        "The difference between levels of signal on channel #" + (i + 1) + " on stand frequency " + standFrequency_Hz + "Hz is exceeded." +
                                "\nExpected: " + String.format("%.2f%%", before_prt) +
                                "\nActual: " + String.format("%.2f%%", after_prt),
                        after_prt <= before_prt);
            }
        }
    }

    private boolean isFrequencyTested(int standFreq_Hz, int receiverFreq_Hz) {

        return (receiverFreq_Hz != standFreq_Hz) &&
                !(receiverFreq_Hz == 60 || receiverFreq_Hz == 120 || standFreq_Hz == 60 || standFreq_Hz == 120) &&
                !(receiverFreq_Hz == 100 && standFreq_Hz == 50);

    }

}
