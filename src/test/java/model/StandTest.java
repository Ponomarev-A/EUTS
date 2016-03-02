package model;

import connections.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Stand test class
 * <p/>
 * !!! TESTING ON REAL STAND ONLY !!!
 */
@Ignore
public class StandTest {

    private final static Connection uart = UART.getInstance(UART.getPortNames()[0]);
    private final static Protocol modbus = new ModBus();
    private final static ConnectionManager connectionManager = new ConnectionManager(uart, modbus);

    Stand stand = new Stand(connectionManager);

    @Before
    public void setUp() throws Exception {
        connectionManager.getConnection().open();
    }

    @Test
    public void youReadInfo() throws Exception {
        assertTrue(stand.readInfo());
    }
}