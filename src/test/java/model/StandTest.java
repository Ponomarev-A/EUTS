package model;

import connections.ConnectionManager;
import connections.ModBus;
import connections.Protocol;
import connections.UARTTest;
import controller.Controller;
import model.tests.TestManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import packet.Command;

import static org.junit.Assert.assertTrue;

/**
 * Stand test class
 * <p/>
 * !!! TESTING ON REAL STAND ONLY !!!
 */
public class StandTest {

    private final Protocol protocol = new ModBus();
    private Controller controller;
    private ConnectionManager connectionManager;

    @Before
    public void setUp() throws Exception {
        controller = ModelTest.createMockController();
        connectionManager = new ConnectionManager(UARTTest.createUARTConnection(), protocol);
        connectionManager.getConnection().open();
    }

    @After
    public void tearDown() throws Exception {
        if (connectionManager != null)
            connectionManager.getConnection().close();
    }

    @Test
    public void youReadInfo() throws Exception {
        Stand stand = new Stand(connectionManager, controller);
        assertTrue(stand.readInfo());
    }

    @Test
    public void youGetStatusCONNECTED() throws Exception {
        Stand stand = new Stand(connectionManager, controller);

        stand.checkConnectionStatus();
        assertTrue(stand.getConnectionStatus() == ConnectionStatus.CONNECTED);
    }

    @Test
    @Ignore
    public void testSetAllFrequencies() throws Exception {
        Stand stand = new Stand(connectionManager, controller);
        Receiver receiver = new Receiver(connectionManager, controller);

        new TestManager(controller, receiver, stand);

        for (int i = 40; i < 33000; i += 10) {
            stand.set(Command.FREQUENCY_STAND, i);
        }
    }
}