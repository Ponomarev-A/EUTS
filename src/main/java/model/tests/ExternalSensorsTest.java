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
class ExternalSensorsTest extends AnalogFilterTest {

    private static final double DELTA_LEVEL_PRT = 10.0;
    private static final double REJECTION_EXT_SENSOR = 3.0;


    private static final int FREQUENCY = 512;
    private static final int GAIN = 20;
    private static final long WAIT_CHANGE_EXT_SENSOR_MS = 300;

    ExternalSensorsTest() {
        super(String.format("Check external sensors (%d Hz, %d dB)", FREQUENCY, GAIN),
                GAIN,
                FREQUENCY);
    }

    @Override
    public void runTest(Receiver receiver, Stand stand) throws Exception, Error {

        setUp(stand);
        setUp(receiver);
        short[] beforeLevels = autoSetVoltage(stand, receiver, receiverGain_dB, MIN_LEVEL_PRT, MAX_LEVEL_PRT, INIT_LEVEL_PRT);

        Device.ExtSensors receiverSensor = null;
        for (Device.ExtSensors standSensor : Device.ExtSensors.values()) {

            stand.set(EXT_SENSOR_STAND, standSensor.ordinal());
            TimeUnit.MILLISECONDS.sleep(WAIT_CHANGE_EXT_SENSOR_MS);
            receiverSensor = Stand.ExtSensors.values()[receiver.getArray(GET_EXT_SENSOR_DEVICE)[0]];

            assertEquals(String.format(
                    "The external sensors are different on Stand and on Receiver.%-30s: %s %-30s: %s\n",
                    "\nSensor set by Stand", standSensor,
                    "\nSensor get from Receiver", receiverSensor),
                    standSensor, receiverSensor
            );
        }

        short[] afterLevels = receiver.getArray(GET_LEVELS_DEVICE);

        double before4_prt = beforeLevels[3] / REJECTION_EXT_SENSOR * 100.0 / MAX_LEVEL;
        double after4_prt = afterLevels[3] * 100.0 / MAX_LEVEL;
        double lowBound4_prt = before4_prt * (100.0 - DELTA_LEVEL_PRT) / 1000;
        double highBound4_prt = before4_prt * (100.0 + DELTA_LEVEL_PRT) / 100;

        assertTrue(String.format(
                "The level of signal on channel #4 (with %s connected) is out of range.%-12s: %5.2f%%...%5.2f%% %-12s: %5.2f%%",
                receiverSensor,
                "\nExpected: ", lowBound4_prt, highBound4_prt,
                "\nActual:", after4_prt),
                after4_prt >= lowBound4_prt && after4_prt <= highBound4_prt
        );
    }
}
