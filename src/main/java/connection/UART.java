package connection;

import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * UART connection class
 */
public class UART {
    private final int port;
    private SerialPort serialPort;

    public UART(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    public byte[] read() throws SerialPortException {

        return new byte[0];
    }

    public void write(byte[] buffer) {


    }
}
