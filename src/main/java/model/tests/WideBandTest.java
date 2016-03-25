package model.tests;

import model.Device;
import model.Receiver;
import model.Stand;

import java.util.concurrent.TimeUnit;

import static model.Receiver.MAX_LEVEL;
import static packet.Command.*;

/**
 * Base parent class for AF child classes
 */
class WideBandTest extends AnalogFilterTest {

    private static final double INIT_LEVEL_PRT = 70.0;
    private static final double MIN_LEVEL_PRT = 60.0;
    private static final double MAX_LEVEL_PRT = 80.0;
    private static final double DELTA_LEVEL_PRT = 10.0;

    private static final int START_FREQUENCY = 512;
    private static final int END_FREQUENCY = 8192;
    private static final int GAIN = 20;

    private static final double REJECTION_ACTIVE_FREQ = 10;

    WideBandTest(Receiver receiver, Stand stand) {
        super(String.format("Check wide band (from %d Hz to %d Hz, %d dB)", START_FREQUENCY, END_FREQUENCY, GAIN),
                receiver,
                stand,
                GAIN,
                START_FREQUENCY);
    }

    @Override
    public void runTest() throws Exception, Error {

        setUpStand();
        setUpReceiver();
        short[] beforeLevels = autoSetVoltageStand(receiverGain_dB, MIN_LEVEL_PRT, MAX_LEVEL_PRT, INIT_LEVEL_PRT);

        // Set up new frequency and wait completion transient process on receiver
        stand.set(FREQUENCY_STAND, END_FREQUENCY);
        TimeUnit.MILLISECONDS.sleep(WAIT_CHANGE_VOLTAGE_MS);

        short[] afterLevels = receiver.getArray(GET_LEVELS_DEVICE);

        double before3_prt = beforeLevels[2] * 100.0 / MAX_LEVEL;
        double before4_prt = beforeLevels[3] * 100.0 / MAX_LEVEL;
        double after3_prt = afterLevels[2] * 100.0 / MAX_LEVEL;
        double after4_prt = afterLevels[3] * 100.0 / MAX_LEVEL;

        before3_prt /= REJECTION_ACTIVE_FREQ;

        assertTrue(String.format(
                "The level of signal on channel #3 is exceeded.%-12s: <%5.2f%% %-12s:  %5.2f%%",
                "\nExpected: ", before3_prt,
                "\nActual:", after3_prt),
                after3_prt <= before3_prt
        );

        double lowBound4_prt = before4_prt * (100.0 - DELTA_LEVEL_PRT) / 100;
        double highBound4_prt = before4_prt * (100.0 + DELTA_LEVEL_PRT) / 100;

        assertTrue(String.format(
                "The level of signal on channel #4 is out of range.%-12s: %5.2f%%...%5.2f%% %-12s: %5.2f%%",
                "\nExpected: ", lowBound4_prt, highBound4_prt,
                "\nActual:", after4_prt),
                after4_prt >= lowBound4_prt && after4_prt <= highBound4_prt
        );
    }

    @Override
    void setUpReceiver() throws Exception {
        receiver.set(MODE_DEVICE, Receiver.Modes.MODE_TESTLEVELS.ordinal());
        receiver.set(TYPE_OF_SIGNAL_DEVICE, Device.SignalType.SOLID.ordinal());
        receiver.set(BOTTOM_SENSOR_DEVICE, Receiver.BSType.WB.ordinal());
        receiver.set(FREQUENCY_DEVICE, receiverFrequency_Hz);
        receiver.set(GAIN_DEVICE, receiverGain_dB);
    }
}
