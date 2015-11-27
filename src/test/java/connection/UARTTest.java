package connection;

import jssc.SerialPort;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class
 */
public class UARTTest {

    @Test
    public void youCreateNewConnection() throws Exception {
        UART connection = new UART(1234);
        assertNotNull(connection);
    }

    @Test
    public void youGetPortUsedByUARTConnection() throws Exception {
        UART connection = new UART(7777);
        assertEquals(7777, connection.getPort());
    }

    @Test
    public void youCreateSerialPort() throws Exception {
        String portName = "COM5";
        SerialPort serialPort = new SerialPort(portName);

        assertEquals(portName, serialPort.getPortName());
    }

    @Test
    public void youSetSerialPortToConnection() throws Exception {
        UART connection = new UART(5555);
        String portName = "COM" + connection.getPort();

        SerialPort serialPort = new SerialPort("COM" + connection.getPort());
        assertEquals(portName, serialPort.getPortName());

        connection.setSerialPort(serialPort);
        assertEquals(serialPort, connection.getSerialPort());
    }

    @Test
    public void youGetSerialPortNameFromConnection() throws Exception {
        UART connection = new UART(1234);
        connection.setSerialPort(new SerialPort("COM9"));

        assertEquals("COM9", connection.getSerialPort().getPortName());
    }

    @Ignore
    @Test
    public void youReadByteArrayFromConnection() throws Exception {
        UART connection = new UART(1234);
        connection.setSerialPort(new SerialPort("COM10"));

        byte[] buffer = new byte[] {1,2,3,4};

        assertArrayEquals(buffer, connection.read());
    }

    @Ignore
    @Test
    public void youWriteByteArrayToConnection() throws Exception {
        UART connection = new UART(1234);
        connection.setSerialPort(new SerialPort("COM10"));

        byte[] buffer = new byte[] {1,2,3,4};
        connection.write(buffer);

        assertArrayEquals(buffer, connection.read());
    }
}