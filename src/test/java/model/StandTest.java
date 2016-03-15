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
 * Stand test class
 * <p/>
 * !!! TESTING ON REAL STAND ONLY !!!
 */
public class StandTest {

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
        Stand stand = new Stand(connectionManager, controller);
        assertTrue(stand.readInfo());
    }

    @Test
    public void youGetStatusCONNECTED() throws Exception {
        Stand stand = new Stand(connectionManager, controller);

        stand.checkConnectionStatus();
        assertTrue(stand.getConnectionStatus() == ConnectionStatus.CONNECTED);
    }
}