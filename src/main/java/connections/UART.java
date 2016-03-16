package connections;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import exception.InvalidPacketSize;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
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

    private static final int READ_PERIOD_MS = 50;
    private static final int READ_EXECUTE_TIMEOUT_MS = READ_PERIOD_MS * 10;
    private static final int READ_WAIT_TIMEOUT_MS = READ_EXECUTE_TIMEOUT_MS + 1500;

    private static final int WRITE_PERIOD_MS = 1000;
    private static final int WRITE_WAIT_TIMEOUT_MS = WRITE_PERIOD_MS + 500;

    private static final ThreadFactory THREAD_FACTORY_WRITER = new ThreadFactoryBuilder().setNameFormat("Writer-%d").setDaemon(true).build();
    private static final ThreadFactory THREAD_FACTORY_READER = new ThreadFactoryBuilder().setNameFormat("Reader-%d").setDaemon(true).build();

    private static UART instance = null;

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
    public byte[] read() throws InvalidPacketSize, SerialPortException {

        ScheduledExecutorService readExecutor = Executors.newScheduledThreadPool(1, THREAD_FACTORY_READER);
        PortReader portReader = new PortReader(readExecutor);

        readExecutor.schedule(portReader, READ_PERIOD_MS, TimeUnit.MILLISECONDS);

        try {
            readExecutor.awaitTermination(READ_WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Already shutdown executor and kill all "read" threads
            readExecutor.shutdown();
        }

        if (portReader.buffer.length == 0)
            throw new InvalidPacketSize("Method \"read\":" +
                    "\n port: " + this + ", " +
                    "\n read bytes count: " + portReader.buffer.length);

        return portReader.buffer;
    }

    @Override
    public boolean write(final byte[] buffer) throws SerialPortException {

        Integer leftOutputBufferBytesCount = -1;
        ScheduledExecutorService writeExecutor = Executors.newScheduledThreadPool(1, THREAD_FACTORY_WRITER);

        try {
            leftOutputBufferBytesCount = writeExecutor.submit(new Callable<Integer>() {

                @Override
                public Integer call() throws Exception {
                    int outputBufferBytesCount = -1;

                    synchronized (lock) {
                        if (serialPort.isOpened() && serialPort.writeBytes(buffer)) {
                            outputBufferBytesCount = serialPort.getOutputBufferBytesCount();
                        }
                    }

                    TimeUnit.MILLISECONDS.sleep(WRITE_PERIOD_MS);
                    return outputBufferBytesCount;
                }
            }).get();

            writeExecutor.shutdown();
            writeExecutor.awaitTermination(WRITE_WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
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

    private class PortReader implements Runnable {

        private final ScheduledExecutorService executor;
        private byte[] buffer = new byte[0];

        private int attemptsToRead = 0;     // Количество попыток чтения буфера входных данных

        public PortReader(ScheduledExecutorService executor) {
            this.executor = executor;
        }

        @Override
        public void run() {

            if (buffer.length > 0 && ++attemptsToRead == READ_EXECUTE_TIMEOUT_MS / READ_PERIOD_MS) {
                executor.shutdown();
            } else {
                synchronized (lock) {

                    try {
                        if (serialPort.isOpened()) {
                            buffer = ArrayUtils.addAll(buffer, serialPort.readBytes());
                        }
                    } catch (SerialPortException e) {
                        e.printStackTrace();
                    }
                }

                executor.schedule(this, READ_PERIOD_MS, TimeUnit.MILLISECONDS);
            }
        }
    }

}

