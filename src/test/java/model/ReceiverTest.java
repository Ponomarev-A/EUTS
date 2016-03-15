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

    private ConnectionManager connectionManager;

    @Before
    public void setUp() throws Exception {
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
        Receiver receiver = new Receiver(connectionManager, controller);
        assertTrue(receiver.readInfo());
    }

    @Test
    public void youGetStatusCONNECTED() throws Exception {
        Receiver receiver = new Receiver(connectionManager, controller);

        receiver.checkConnectionStatus();
        assertTrue(receiver.getConnectionStatus() == ConnectionStatus.CONNECTED);
    }
}