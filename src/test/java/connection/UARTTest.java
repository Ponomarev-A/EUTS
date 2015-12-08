package connection;

import jssc.SerialPort;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class
 */
public class UARTTest {

    public static final String COM1_PORT = "COM1";
    UART uart;

    @Before
    public void chooseSpyOrRealUARTUse() throws Exception {
        // Choose uart for testing: real or spy
        UART realUART = new UART();
        UART spyUART = spyUART(realUART);

        boolean isRealUARTUsed = realUART.askPortName() != null;

        uart = isRealUARTUsed ? realUART : spyUART;
    }

    public UART spyUART(UART realUART) {
//        UART spyUART = spy(realUART);
//
//        doReturn(COM1_PORT).when(spyUART).askPortName();
//        doReturn(COM1_PORT).when(spyUART).getPortName();
//        doReturn(new SerialPort(COM1_PORT)).when(spyUART).getSerialPort();
//        doReturn(true).when(spyUART).isOpened();
//        doReturn(true).when(spyUART).init();

//        return spyUART;

        UART mockUART = mock(UART.class);

        when(mockUART.askPortName()).thenReturn(COM1_PORT);
        when(mockUART.getPortName()).thenReturn(COM1_PORT);
        when(mockUART.getSerialPort()).thenReturn(new SerialPort(COM1_PORT));
        when(mockUART.isOpened()).thenReturn(true);
        when(mockUART.init()).thenReturn(true);

        return mockUART;
    }

    @Test
    public void youCreateNewUARTConnection() throws Exception {
        assertNotNull(new UART());
    }

    @Test
    public void testGetPortName() throws Exception {
        assertEquals(COM1_PORT, uart.getPortName());
    }

    @Test
    public void testGetSerialPort() throws Exception {
        assertNotNull(uart.getSerialPort());
    }

    @Test
    public void testInit() throws Exception {
        assertTrue(uart.init());
    }

    @Test
    public void testAskPortName() throws Exception {
        assertEquals(COM1_PORT, uart.askPortName());
    }

    @Test
    public void testIsOpened() throws Exception {
        uart.init();
        assertTrue(uart.isOpened());
    }
}