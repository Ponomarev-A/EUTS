package model;

import connections.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Stand test class
 * <p/>
 * !!! TESTING ON REAL STAND ONLY !!!
 */
@Ignore
public class StandTest {

    private final static Connection uart = new UART(UART.getPortNames()[0]);
    private final static Protocol modbus = new ModBus();
    private final static ConnectionManager connectionManager = new ConnectionManager(uart, modbus);

    Stand stand = new Stand(connectionManager);

    @Before
    public void setUp() throws Exception {
        connectionManager.open();
    }

    @Test
    public void youGetInfo() throws Exception {
        assertEquals("1.00 EUTS.00.000izm1", stand.getInfo());

    }
}