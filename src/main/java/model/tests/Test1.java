package model.tests;

import model.Receiver;
import model.Stand;

import static packet.Command.*;

/**
 * Sample test case class
 */
public class Test1 extends BaseTestCase {

    public Test1(String name, Receiver receiver, Stand stand) {
        super(name, receiver, stand);
    }

    @Override
    public void setUp() {

        int voltage_mcV = 10000;
        int frequency_Hz = 1024;

        stand.set(VOLTAGE_STAND, voltage_mcV);
        stand.set(FREQUENCY_STAND, frequency_Hz);

        receiver.set(MODE_DEVICE, Receiver.Modes.MODE_TESTLEVELS.ordinal());
        receiver.set(FREQUENCY_DEVICE, frequency_Hz);
        receiver.set(GAIN_DEVICE, -1);

        while (true) {
            short[] levels = receiver.getArray();
            if (levels[3] >= 0.95 * Receiver.MAX_LEVEL && levels[3] <= 0.99 * Receiver.MAX_LEVEL)
                break;
            else
                stand.set(VOLTAGE_STAND, voltage_mcV++);
        }
    }

    @Override
    public void runTest() throws Exception {

        short[] levels = receiver.getArray();
        int maxLevel = Math.max(levels[0], Math.max(levels[1], Math.max(levels[2], levels[3])));
        int minLevel = Math.min(levels[0], Math.min(levels[1], Math.min(levels[2], levels[3])));

        assertTrue(maxLevel - minLevel < 0.15 * Receiver.MAX_LEVEL);
    }


}
