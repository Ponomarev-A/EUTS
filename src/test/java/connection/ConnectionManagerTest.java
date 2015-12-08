package connection;

import jssc.SerialPortException;
import org.junit.Before;
import org.junit.Test;
import packet.Command;
import packet.Packet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * ConnectionManager test class
 */
public class ConnectionManagerTest {

    ConnectionManager connectionManager;
    Packet packet = new Packet(Command.CHECK_CONNECTION_STAND, new byte[]{1, 2, 3, 4});


    @Before
    public void chooseSpyOrRealConnectionUse() throws Exception {
        // Choose uart for testing: real or spy
        UART realUART = new UART();
        UART spyUART = new UARTTest().spyUART(realUART);

        boolean isRealConnectionUsed = realUART.askPortName() != null;

        connectionManager = new ConnectionManager(
                isRealConnectionUsed ? realUART : spyUART,
                new ModBus()
        );

        if (!isRealConnectionUsed) {
            when(spyUART.read()).thenReturn(connectionManager.getProtocol().wrap(packet.pack()));
        }
    }


    @Test
    public void youCreateUARTConnectionWithModbusProtocol() throws Exception {
        Connect uart = new UART();
        Protocol modbus = new ModBus();
        ConnectionManager connectionManager = new ConnectionManager(uart, modbus);

        assertNotNull(connectionManager);
        assertEquals(uart, connectionManager.getConnection());
        assertEquals(modbus, connectionManager.getProtocol());
    }

    @Test
    public void youSendPacket() throws Exception {
        assertTrue(connectionManager.sendPacket(packet));
    }


    @Test
    public void youSendPacketFailed() throws Exception {

        doThrow(new SerialPortException(null, null, null)).
                when(connectionManager.getConnection()).
                write(connectionManager.getProtocol().wrap(packet.pack()));

        assertFalse(connectionManager.sendPacket(packet));
    }

    @Test
    public void youReceivePacket() throws Exception {
        connectionManager.sendPacket(packet);

        assertEquals(packet, connectionManager.receivePacket());
    }

    @Test
    public void youReceivePacketFailed() throws Exception {

        // Replace open code sequence to close code sequence
        byte[] readData = connectionManager.getConnection().read();
        readData[0] = connectionManager.getProtocol().getCloseSequence()[0];

        when(connectionManager.getConnection().read()).thenReturn(readData);

        assertNull(connectionManager.receivePacket());
    }
}