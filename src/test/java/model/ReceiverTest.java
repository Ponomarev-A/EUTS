package model;

import connections.*;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
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
        connectionManager.open();
    }

    @Test
    public void youGetInfo() throws Exception {
        assertEquals("AP-019.1 3.05 AP019.01.020izm11 000000", receiver.getInfo());
    }

    @Test
    public void youGetStatusCONNECTED() throws Exception {
        receiver.checkConnectionStatus();
        assertTrue(receiver.getConnectionStatus() == ConnectionStatus.CONNECTED);
    }
}