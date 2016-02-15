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
public class DeviceTest {

    private final static Connection uart = new UART(UART.getPortNames()[0]);
    private final static Protocol modbus = new ModBus();
    private final static ConnectionManager connectionManager = new ConnectionManager(uart, modbus);

    private Device device = new Device(connectionManager);

    @BeforeClass
    public static void setUp() throws Exception {
        connectionManager.open();
    }

    @Test
    public void youGetInfo() throws Exception {
        assertEquals("AP-019.1 3.05 AP019.01.020izm11 000000", device.getInfo());
    }

    @Test
    public void youGetStatusCONNECTED() throws Exception {
        assertTrue(device.isConnected());
    }
}