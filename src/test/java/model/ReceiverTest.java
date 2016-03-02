package model;

import connections.*;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Device test class
 * <p/>
 * !!! TESTING ON REAL DEVICE ONLY !!!
 */
public class ReceiverTest {

    private final static Connection uart = UART.getInstance(UART.getPortNames()[0]);
    private final static Protocol modbus = new ModBus();
    private final static ConnectionManager connectionManager = new ConnectionManager(uart, modbus);

    private Receiver receiver = new Receiver(connectionManager);

    @BeforeClass
    public static void setUp() throws Exception {
        connectionManager.getConnection().open();
    }

    @Test
    public void youReadInfo() throws Exception {
        assertTrue(receiver.readInfo());
    }

    @Test
    public void youGetStatusCONNECTED() throws Exception {
        receiver.checkConnectionStatus();
        assertTrue(receiver.getConnectionStatus() == ConnectionStatus.CONNECTED);
    }
}