package model.tests;

import model.Receiver;
import model.Stand;

import java.util.concurrent.TimeUnit;

import static model.Receiver.MAX_LEVEL;
import static packet.Command.*;

/**
 * Base parent class for AF child classes
 */
class TwoFrequencyTest extends AnalogFilterTest {

    private static final double DELTA_LEVEL_PRT = 10.0;

    private static final int FREQUENCY_1024 = 1024;
    private static final int FREQUENCY_8192 = 8192;
    private static final int GAIN = 10;

    private static final double REJECTION_ACTIVE_FREQ = 10;

    TwoFrequencyTest(Receiver receiver, Stand stand) {
        super(String.format("Check 2-frequency mode (%d Hz and %d Hz, %d dB)", FREQUENCY_1024, FREQUENCY_8192, GAIN),
                receiver,
                stand,
                GAIN,
                FREQUENCY_1024);
    }

    @Override
    public void runTest() throws Exception, Error {

        setUpStand();
        setUpReceiver();
        autoSetVoltageStand(receiverGain_dB, MIN_LEVEL_PRT, MAX_LEVEL_PRT, INIT_LEVEL_PRT);

        receiver.set(MODE_DEVICE, Receiver.Modes.MODE_INT_IFF.ordinal());
        short[] magnitudesAt1024Hz = receiver.getArray(GET_MAGNITUDES_DEVICE);

        double magn1K_at1024Hz_prt = magnitudesAt1024Hz[0] * 100.0 / MAX_LEVEL;
        double magn8K_at1024Hz_prt = magnitudesAt1024Hz[1] * 100.0 / MAX_LEVEL;

        assertTrue(String.format(
                "The magnitude level %d Hz on frequency %d Hz is exceeded.%-12s: <%5.2f%% %-12s:  %5.2f%%",
                FREQUENCY_8192, FREQUENCY_1024,
                "\nExpected: ", magn1K_at1024Hz_prt / REJECTION_ACTIVE_FREQ,
                "\nActual:", magn8K_at1024Hz_prt),
                magn8K_at1024Hz_prt <= magn1K_at1024Hz_prt / REJECTION_ACTIVE_FREQ
        );

        // Set up new frequency and wait completion transient process on receiver
        stand.set(FREQUENCY_STAND, FREQUENCY_8192);
        TimeUnit.MILLISECONDS.sleep(WAIT_CHANGE_VOLTAGE_MS);

        short[] magnitudesAt8192Hz = receiver.getArray(GET_MAGNITUDES_DEVICE);

        double magn1K_at8192Hz_prt = magnitudesAt8192Hz[0] * 100.0 / MAX_LEVEL;
        double magn8K_at8192Hz_prt = magnitudesAt8192Hz[1] * 100.0 / MAX_LEVEL;


        assertTrue(String.format(
                "The magnitude level %d Hz on frequency %d Hz is exceeded.%-12s: <%5.2f%% %-12s:  %5.2f%%",
                FREQUENCY_1024, FREQUENCY_8192,
                "\nExpected: ", magn8K_at8192Hz_prt / REJECTION_ACTIVE_FREQ,
                "\nActual:", magn1K_at8192Hz_prt),
                magn1K_at8192Hz_prt <= magn8K_at8192Hz_prt / REJECTION_ACTIVE_FREQ
        );

        double lowBound_prt = magn1K_at1024Hz_prt * (100.0 - DELTA_LEVEL_PRT) / 100;
        double highBound_prt = magn8K_at8192Hz_prt < 100.0 ? magn8K_at8192Hz_prt * (100.0 + DELTA_LEVEL_PRT) / 100 : 100.0;

        assertTrue(String.format(
                "The magnitude levels %d Hz and %d Hz are out of range..%-12s: %5.2f%%...%5.2f%% %-12s: %5.2f%%",
                FREQUENCY_1024, FREQUENCY_8192,
                "\nExpected: ", lowBound_prt, highBound_prt,
                "\nActual:", magn8K_at8192Hz_prt),
                magn8K_at8192Hz_prt >= lowBound_prt && magn8K_at8192Hz_prt <= highBound_prt
        );
    }
}
