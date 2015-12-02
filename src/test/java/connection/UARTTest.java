package connection;

import jssc.SerialPort;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * Test class
 */
public class UARTTest {

    public static final String COM1_PORT = "COM1";
    UART connection;

    @Before
    public void initMockCOMPortList() throws Exception {

        UART uart = new UART();
        UART spyConnection = spy(uart);

        doReturn(COM1_PORT).when(spyConnection).askPortName();
        doReturn(COM1_PORT).when(spyConnection).getPortName();
        doReturn(new SerialPort(COM1_PORT)).when(spyConnection).getSerialPort();
        doReturn(true).when(spyConnection).isOpened();
        doReturn(true).when(spyConnection).init();

        // Choose connection for testing: real or mock
        String portNames = uart.askPortName();
        connection = (portNames != null) ? uart : spyConnection;
    }

    @Test
    public void youCreateNewUARTConnection() throws Exception {
        assertNotNull(new UART());
    }

    @Test
    public void testGetPortName() throws Exception {
        assertEquals(COM1_PORT, connection.getPortName());
    }

    @Test
    public void testGetSerialPort() throws Exception {
        assertNotNull(connection.getSerialPort());
    }

    @Test
    public void testInit() throws Exception {
        assertTrue(connection.init());
    }

    @Test
    public void testAskPortName() throws Exception {
        assertEquals(COM1_PORT, connection.askPortName());
    }

    @Test
    public void testIsOpened() throws Exception {
        connection.init();
        assertTrue(connection.isOpened());
    }
}