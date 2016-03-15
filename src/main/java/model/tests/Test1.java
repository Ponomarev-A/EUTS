package model.tests;

import model.Receiver;
import model.Stand;

import java.util.concurrent.TimeUnit;

import static packet.Command.*;

/**
 * Sample test case class
 */
public class Test1 extends BaseTestCase {

    public Test1(String name, Receiver receiver, Stand stand) {
        super(name, receiver, stand);
    }

    @Override
    public boolean setUp() throws InterruptedException {

        int voltage_mcV = 10000;
        int frequency_Hz = 1024;

        stand.set(FREQUENCY_STAND, frequency_Hz);
        stand.set(VOLTAGE_STAND, voltage_mcV);

        receiver.set(MODE_DEVICE, Receiver.Modes.MODE_TESTLEVELS.ordinal());
        receiver.set(FREQUENCY_DEVICE, frequency_Hz);
        receiver.set(GAIN_DEVICE, -1);

        TimeUnit.MILLISECONDS.sleep(2000);

        receiver.set(GET_GAIN_DEVICE);
        short[] gain = receiver.getArray();


        if (gain.length == 0)
            return false;

        receiver.set(GAIN_DEVICE, (int) gain[0]);

        while (true) {
            receiver.set(GET_LEVELS_DEVICE);
            short[] levels = receiver.getArray();

            if (levels.length == 0)
                return false;

            if (levels[3] >= 0.900 * Receiver.MAX_LEVEL && levels[3] <= 0.999 * Receiver.MAX_LEVEL)
                break;
            else {
                voltage_mcV += 50;
            }

            stand.set(VOLTAGE_STAND, voltage_mcV);
            TimeUnit.MILLISECONDS.sleep(100);
        }

        return true;
    }

    @Override
    public void runTest() throws Exception {

        receiver.set(GET_LEVELS_DEVICE);
        short[] levels = receiver.getArray();
        int maxLevel = Math.max(levels[0], Math.max(levels[1], Math.max(levels[2], levels[3])));
        int minLevel = Math.min(levels[0], Math.min(levels[1], Math.min(levels[2], levels[3])));

        assertTrue(maxLevel - minLevel < 0.15 * Receiver.MAX_LEVEL);
    }


}
