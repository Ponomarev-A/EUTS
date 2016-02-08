package connections;

import jssc.*;

import java.util.concurrent.*;

/**
 * UART connections class
 */
public class UART implements Connection {

    public static final int BAUDRATE = SerialPort.BAUDRATE_115200;
    public static final int DATABITS = SerialPort.DATABITS_8;
    public static final int STOPBITS = SerialPort.STOPBITS_1;
    public static final int PARITY = SerialPort.PARITY_NONE;

    private SerialPort serialPort;
    private PortReader portReader = new PortReader();

    public UART(String portName) {
        serialPort = new SerialPort(portName);
    }

    public static String[] getPortNames() {
        return SerialPortList.getPortNames();
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
                serialPort.addEventListener(portReader, SerialPort.MASK_RXCHAR);
            }
        } catch (SerialPortException e) {
            e.printStackTrace();
        }

        return isOpened();
    }

    @Override
    public byte[] read() throws SerialPortException {

        byte[] result = null;

        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            result = executor.submit(portReader).get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.err.println(getClass().getSimpleName() + " READ: Timeout exception!");
        }

        return result;
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

    private class PortReader implements Callable<byte[]>, SerialPortEventListener {

        private byte[] data = null;

        @Override
        public void serialEvent(SerialPortEvent event) {

            if (event.isRXCHAR() && event.getEventValue() > 0) {
                try {
                    data = serialPort.readBytes(event.getEventValue());
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        public byte[] call() throws Exception {
            if (data == null) {
                Thread.sleep(200);
            }

            return data;
        }
    }
}
