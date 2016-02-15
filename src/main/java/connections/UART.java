package connections;

import exception.InvalidPacketSize;
import jssc.*;
import org.apache.commons.lang3.ArrayUtils;

import java.util.concurrent.*;

/**
 * UART connections class
 */
public class UART implements Connection {

    public static final int BAUDRATE = SerialPort.BAUDRATE_115200;
    public static final int DATABITS = SerialPort.DATABITS_8;
    public static final int STOPBITS = SerialPort.STOPBITS_1;
    public static final int PARITY = SerialPort.PARITY_NONE;

    public static final int READ_PORT_TIMEOUT_MS = 1000;
    public static final int READ_PORT_DELAY_MS = 600;
    public static final int MAX_BUFFER_LENGTH = 2048;

    private SerialPort serialPort;
    private PortReader portReader = new PortReader(MAX_BUFFER_LENGTH);

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
            if (!isOpened() && serialPort != null) {
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

        ExecutorService executor = Executors.newScheduledThreadPool(1);

        try {
            result = ((ScheduledExecutorService) executor).
                    schedule(portReader, READ_PORT_DELAY_MS, TimeUnit.MILLISECONDS).
                    get(READ_PORT_DELAY_MS + READ_PORT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
            System.err.println(this.getClass().getSimpleName() + " read(): Timeout exception!");
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

        private final int maxBufferLength;

        private int bufferLength = 0;
        private byte[] buffer;

        public PortReader(int maxBufferLength) {
            this.maxBufferLength = maxBufferLength;
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
            byte[] receivedBuffer;

            if (event.isRXCHAR()) {
                try {
                    receivedBuffer = serialPort.readBytes();
                    int receivedBufferLength = receivedBuffer != null ? receivedBuffer.length : 0;

                    if (receivedBufferLength + bufferLength > maxBufferLength)
                        throw new InvalidPacketSize();

                    buffer = ArrayUtils.addAll(bufferLength > 0 ? buffer : null, receivedBuffer);
                    bufferLength = buffer.length;

                } catch (SerialPortException | InvalidPacketSize e) {
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
            bufferLength = 0;
            return buffer;
        }
    }
}
