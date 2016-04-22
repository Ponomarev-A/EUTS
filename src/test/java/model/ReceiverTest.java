package model;

import connections.ConnectionManager;
import connections.ModBus;
import connections.Protocol;
import connections.UARTTest;
import controller.Controller;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Device test class
 * <p/>
 * !!! TESTING ON REAL DEVICE ONLY !!!
 */
public class ReceiverTest {

    private final Protocol protocol = new ModBus();
    private final Controller controller = ModelTest.createMockController();

    private ConnectionManager CM;

    @Before
    public void setUp() throws Exception {
        CM = new ConnectionManager(UARTTest.createUARTConnection(), protocol);
        CM.getConnection().open();
    }

    @After
    public void tearDown() throws Exception {
        if (CM != null)
            CM.getConnection().close();
    }

    @Test
    public void youReadInfo() throws Exception {
        Receiver receiver = new Receiver(controller, CM);
        assertTrue(receiver.readInfo());
    }

    @Test
    public void youGetStatusCONNECTED() throws Exception {
        Receiver receiver = new Receiver(controller, CM);

        receiver.checkConnectionStatus();
        assertTrue(receiver.getConnectionStatus() == ConnectionStatus.CONNECTED);
    }
}