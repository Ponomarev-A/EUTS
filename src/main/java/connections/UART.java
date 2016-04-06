package connections;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import exception.InvalidPacketSize;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.apache.commons.lang3.ArrayUtils;
import packet.Packet;

import java.util.concurrent.*;

/**
 * UART connections class
 */
public class UART implements Connection {

    private static final int BAUDRATE = SerialPort.BAUDRATE_115200;
    private static final int DATABITS = SerialPort.DATABITS_8;
    private static final int STOPBITS = SerialPort.STOPBITS_1;
    private static final int PARITY = SerialPort.PARITY_NONE;

    private static final int READ_REPEAT_DELAY_MS = 50;
    private static final int READ_STEP_DELAY_MS = 10;
    private static final int READ_ATTEMPTS = 10;
    private static final int READ_WAIT_TIMEOUT_MS;
    private static final int WRITE_WAIT_TIMEOUT_MS = 200;
    private static final ThreadFactory THREAD_FACTORY_WRITER = new ThreadFactoryBuilder().setNameFormat("Writer-%d").setDaemon(true).build();
    private static final ThreadFactory THREAD_FACTORY_READER = new ThreadFactoryBuilder().setNameFormat("Reader-%d").setDaemon(true).build();
    private static final int MIN_READ_LENGTH = ModBus.OPEN_CODE_SEQ.length + 2 * (Packet.MIN_FRAME_LENGTH + Packet.DATA_COUNT_LENGTH) + ModBus.CLOSE_CODE_SEQ.length;
    private static final int MAX_READ_LENGTH = ModBus.OPEN_CODE_SEQ.length + 2 * (Packet.MAX_FRAME_LENGTH + Packet.DATA_COUNT_LENGTH) + ModBus.CLOSE_CODE_SEQ.length;
    private static UART instance = null;

    static {
        int sum = 0;
        for (int i = READ_ATTEMPTS; i > 0; i--) {
            sum += i * READ_STEP_DELAY_MS;
        }
        READ_WAIT_TIMEOUT_MS = READ_STEP_DELAY_MS + sum + READ_ATTEMPTS * READ_REPEAT_DELAY_MS + 100;
    }

    private final SerialPort serialPort;
    private final Object lock = new Object();

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
    public String toString() {
        return "UART { Port name = " + serialPort.getPortName() + " }";
    }

    @Override
    public boolean open() throws SerialPortException {
        if (!isOpened()) {
            serialPort.openPort();
            serialPort.setParams(BAUDRATE, DATABITS, STOPBITS, PARITY, false, false);
        }

        return isOpened();
    }

    @Override
    public byte[] read() throws Exception {

        final ScheduledExecutorService readExecutor = Executors.newSingleThreadScheduledExecutor(THREAD_FACTORY_READER);
        byte[] readBuffer = new byte[0];

        try {
            readBuffer = readExecutor.schedule(new Callable<byte[]>() {

                private byte[] buffer = new byte[0];
                private int attemptsToRead = READ_ATTEMPTS;     // Количество попыток чтения буфера входных данных

                @Override
                public byte[] call() throws Exception {

                    while (attemptsToRead > 0) {

                        byte[] readBytes;

                        synchronized (lock) {
                            readBytes = serialPort.readBytes();
                        }

                        if (readBytes != null && readBytes.length > 0) {
                            buffer = ArrayUtils.addAll(buffer, readBytes);

                            // if new data read form port, insert delay to repeat read
                            TimeUnit.MILLISECONDS.sleep(READ_REPEAT_DELAY_MS);
                        }

                        // Read timing: from 560 Ms to
                        TimeUnit.MILLISECONDS.sleep(attemptsToRead-- * READ_STEP_DELAY_MS);
                    }

                    return buffer;
                }
            }, READ_STEP_DELAY_MS, TimeUnit.MILLISECONDS).get(READ_WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } finally {
            // Already shutdown executor and kill all "read" threads
            readExecutor.shutdown();
        }

        if (readBuffer.length < MIN_READ_LENGTH || readBuffer.length > MAX_READ_LENGTH)
            throw new InvalidPacketSize(String.format("Read %d bytes (expected from %d to %d bytes) data from port %s",
                    readBuffer.length, MIN_READ_LENGTH, MAX_READ_LENGTH, this));

        return readBuffer;
    }

    @Override
    public boolean write(final byte[] buffer) throws Exception {

        Integer leftOutputBufferBytesCount = -1;
        ExecutorService writeExecutor = Executors.newSingleThreadExecutor(THREAD_FACTORY_WRITER);

        try {
            leftOutputBufferBytesCount = writeExecutor.submit(new Callable<Integer>() {

                @Override
                public Integer call() throws Exception {
                    int outputBufferBytesCount = -1;

                    synchronized (lock) {
                        if (serialPort.writeBytes(buffer)) {
                            outputBufferBytesCount = serialPort.getOutputBufferBytesCount();
                        }
                    }
                    return outputBufferBytesCount;
                }
            }).get(WRITE_WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } finally {
            writeExecutor.shutdownNow();
        }

        return leftOutputBufferBytesCount == 0;
    }

    @Override
    public boolean close() throws SerialPortException {
        if (isOpened()) {
            serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
            serialPort.closePort();
        }
        return !isOpened();
    }

    @Override
    public boolean isOpened() {
        return serialPort.isOpened();
    }
}

