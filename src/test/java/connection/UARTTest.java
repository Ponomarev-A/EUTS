package connection;

import jssc.SerialPort;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class
 */
public class UARTTest {

    public static final int PORT = 1234;
    public static final int COM_PORT = 5;
    UART connection = new UART(PORT);

    @Test
    public void youCreateNewConnection() throws Exception {
        assertNotNull(connection);
    }

    @Test
    public void youGetPortUsedByUARTConnection() throws Exception {
        assertEquals(PORT, connection.getPort());
    }

    @Test
    public void youGetSerialPortFromConnection() throws Exception {
        String portName = "COM" + COM_PORT;
        SerialPort serialPort = new SerialPort(portName);
        connection.setSerialPort(serialPort);

        assertEquals(portName, serialPort.getPortName());
        assertEquals(serialPort, connection.getSerialPort());
    }

    @Test
    @Ignore
    public void youReadByteArrayFromConnection() throws Exception {
        UART connection = new UART(PORT);
        connection.setSerialPort(new SerialPort("COM10"));

        byte[] buffer = new byte[]{1, 2, 3, 4};

        assertArrayEquals(buffer, connection.read());
    }

    @Ignore
    @Test
    public void youWriteByteArrayToConnection() throws Exception {
        UART connection = new UART(PORT);
        connection.setSerialPort(new SerialPort("COM10"));

        byte[] buffer = new byte[]{1, 2, 3, 4};
        connection.write(buffer);

        assertArrayEquals(buffer, connection.read());
    }
}