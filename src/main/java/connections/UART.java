package connections;

import exception.InvalidPacketSize;
import jssc.*;
import org.apache.commons.lang3.ArrayUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * UART connections class
 */
public class UART implements Connection {

    public static final int BAUDRATE = SerialPort.BAUDRATE_115200;
    public static final int DATABITS = SerialPort.DATABITS_8;
    public static final int STOPBITS = SerialPort.STOPBITS_1;
    public static final int PARITY = SerialPort.PARITY_NONE;
    public static final int MAX_BUFFER_LENGTH = 2048;

    public static final int READ_WAIT_TIMEOUT_MS = 2000;
    public static final int READ_EXECUTE_TIMEOUT_MS = 300;
    public static final int READ_PERIOD_MS = 50;
    private static UART instance = null;
    private final SerialPort serialPort;
    private final PortReader portReader = new PortReader(MAX_BUFFER_LENGTH);
    private ScheduledExecutorService executor;

    private UART(String portName) {
        serialPort = new SerialPort(portName);
    }

    public static UART getInstance(String portName) {
        if (instance == null) {
            instance = new UART(portName);
        }

        return (instance.getSerialPort().getPortName().equals(portName)) ?
                instance :
                new UART(portName);
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public static String[] getPortNames() {
        String[] portNames = SerialPortList.getPortNames();
        return (portNames == null) ? new String[0] : portNames;
    }

    @Override
    public boolean open() throws SerialPortException {
        if (!isOpened()) {
            serialPort.openPort();
            serialPort.setParams(BAUDRATE, DATABITS, STOPBITS, PARITY, false, false);
            serialPort.addEventListener(portReader, SerialPort.MASK_RXCHAR);
        }

        return isOpened();
    }

    private boolean isOpened() {
        return serialPort.isOpened();
    }

    @Override
    public byte[] read() throws SerialPortException {

        // Clear port data before new read operation
        portReader.reset();

        executor = Executors.newScheduledThreadPool(1);
        executor.schedule(portReader, READ_PERIOD_MS, TimeUnit.MILLISECONDS);

        // Wait while read operation is cancelled
        try {
            executor.awaitTermination(READ_WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return portReader.buffer;
    }

    @Override
    public boolean write(byte[] buffer) throws SerialPortException {
        return serialPort.isOpened() && serialPort.writeBytes(buffer);
    }

    @Override
    public boolean close() throws SerialPortException {
        if (isOpened()) {
            serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
            serialPort.closePort();
        }
        return isOpened();
    }

    private class PortReader implements Runnable, SerialPortEventListener {

        private final int maxBufferLength;
        public byte[] buffer;
        private int bufferLength;
        private int attemptsToRead;     // Количество попыток чтения буфера входных данных

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

                    buffer = ArrayUtils.addAll(buffer, receivedBuffer);
                    bufferLength = buffer.length;

                } catch (SerialPortException | InvalidPacketSize e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {

            if (bufferLength > 0 && attemptsToRead++ > READ_EXECUTE_TIMEOUT_MS / READ_PERIOD_MS) {
                executor.shutdown();
            } else {
                executor.schedule(this, READ_PERIOD_MS, TimeUnit.MILLISECONDS);
            }
        }

        public void reset() {
            buffer = new byte[0];
            bufferLength = attemptsToRead = 0;
        }
    }
}
