package connections;

import jssc.SerialPort;
import jssc.SerialPortException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class
 */
public class UARTTest {

    public static final String COM1_PORT_NAME = "COM1";
    private byte[] mockData;
    private UART uart;

    @Before
    public void openUARTConnection() throws Exception {
        uart = createUARTConnection();
        uart.open();
    }

    public UART createUARTConnection() {
        UART realUART = new UART();
        UART mockUART = mockUART();

        boolean isRealUARTUsed = realUART.askPortName() != null;

        return isRealUARTUsed ? realUART : mockUART;
    }

    public UART mockUART() {

        final UART mockUART = mock(UART.class);

        when(mockUART.askPortName()).thenReturn(COM1_PORT_NAME);
        when(mockUART.getPortName()).thenReturn(COM1_PORT_NAME);
        when(mockUART.getSerialPort()).thenReturn(new SerialPort(COM1_PORT_NAME));
        when(mockUART.open()).thenReturn(true);
        when(mockUART.close()).thenReturn(true);

        try {
            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    mockData = (byte[]) invocation.getArguments()[0];
                    return null;
                }
            }).when(mockUART).write(any(byte[].class));

            when(mockUART.read()).thenAnswer(new Answer<byte[]>() {
                @Override
                public byte[] answer(InvocationOnMock invocation) throws Throwable {
                    return mockData;
                }
            });
        } catch (SerialPortException e) {
        }

        return mockUART;
    }

    @After
    public void closeUARTConnection() throws Exception {
        uart.close();
    }

    @Test
    public void youCreateNewUARTConnection() throws Exception {
        assertNotNull(new UART());
    }

    @Test
    public void testGetPortName() throws Exception {
        assertEquals(COM1_PORT_NAME, uart.getPortName());
    }

    @Test
    public void testGetSerialPort() throws Exception {
        assertNotNull(uart.getSerialPort());
    }

    @Test
    public void testAskPortName() throws Exception {
        assertEquals(COM1_PORT_NAME, uart.askPortName());
    }
}