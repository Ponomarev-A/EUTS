package connection;

import jssc.*;

/**
 * UART connection class
 */
public class UART implements Connect {

    public static final int BAUDRATE = SerialPort.BAUDRATE_128000;
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
    public boolean init() {
        boolean result = false;
        try {
            if (serialPort != null) {
                result = serialPort.openPort();
                result = serialPort.setParams(BAUDRATE, DATABITS, STOPBITS, PARITY, false, false);

                serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
            }
        } catch (SerialPortException e) {
            e.printStackTrace();
        }

        return result;
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

    public boolean isOpened() {
        return serialPort != null && serialPort.isOpened();
    }

    private class PortReader implements SerialPortEventListener {

        @Override
        public void serialEvent(SerialPortEvent event) {

            if (event.isRXCHAR() && event.getEventValue() > 0) {
                try {
                    readData = serialPort.readBytes();
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
