package connections;

import jssc.*;

/**
 * UART connections class
 */
public class UART implements Connection {

    public static final int BAUDRATE = SerialPort.BAUDRATE_115200;
    public static final int DATABITS = SerialPort.DATABITS_8;
    public static final int STOPBITS = SerialPort.STOPBITS_1;
    public static final int PARITY = SerialPort.PARITY_NONE;

    private String portName;
    private SerialPort serialPort;
    private byte[] readData;

    public UART() {
        portName = askPortName();
        serialPort = new SerialPort(portName);
    }

    public String askPortName() {
        // TODO: realize askPortName() method
        String[] portNames = SerialPortList.getPortNames();
        if (portNames != null && portNames.length > 0)
            return portNames[0];
        else
            return null;
    }

    public String getPortName() {
        return portName;
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    @Override
    public boolean open() {
        try {
            if (serialPort != null) {
                serialPort.openPort();
                serialPort.setParams(BAUDRATE, DATABITS, STOPBITS, PARITY, false, false);
                serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
            }
        } catch (SerialPortException e) {
            e.printStackTrace();
        }

        return isOpened();
    }

    @Override
    public byte[] read() throws SerialPortException {
        return readData;
    }

    @Override
    public void write(byte[] buffer) throws SerialPortException {
        if (serialPort.isOpened())
            serialPort.writeBytes(buffer);
    }

    @Override
    public boolean close() {
        try {
            return isOpened() && serialPort.closePort();
        } catch (SerialPortException e) {
            return false;
        }
    }

    private boolean isOpened() {
        return serialPort != null && serialPort.isOpened();
    }

    private class PortReader implements SerialPortEventListener {

        @Override
        public void serialEvent(SerialPortEvent event) {

            if (event.isRXCHAR() && event.getEventValue() > 0) {
                try {
                    readData = serialPort.readBytes(event.getEventValue());
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
