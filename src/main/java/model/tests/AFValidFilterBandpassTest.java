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

    AFValidFilterBandpassTest(final int frequency_Hz, final int gain_dB) {
        super(String.format("Analog filter: valid filter bandpass (%d Hz, %d dB)", frequency_Hz, gain_dB),
                gain_dB,
                frequency_Hz);
    }

    @Override
    public void runTest(Receiver receiver, Stand stand) throws Exception, Error {

        setUp(stand);
        setUp(receiver);
        short[] beforeLevels = autoSetVoltage(stand, receiver, receiverGain_dB, MIN_LEVEL_PRT, MAX_LEVEL_PRT, INIT_LEVEL_PRT);

        for (Integer standFrequency_Hz : Receiver.FREQUENCY_HZ) {

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

                assertTrue(String.format(
                        "The difference between levels of signal on channel #%d on stand frequency %d Hz is exceeded.%-12s: <%5.2f%% %-12s:  %5.2f%%",
                        i + 1,
                        standFrequency_Hz,
                        "\nExpected: ", before_prt,
                        "\nActual:", after_prt),
                        after_prt <= before_prt
                );
            }
        }
    }

    private boolean isFrequencyTested(int standFreq_Hz, int receiverFreq_Hz) {

        return (receiverFreq_Hz != standFreq_Hz) &&
                !(receiverFreq_Hz == 60 || receiverFreq_Hz == 120 || standFreq_Hz == 60 || standFreq_Hz == 120) &&
                !(receiverFreq_Hz == 100 && standFreq_Hz == 50);

    }

}
