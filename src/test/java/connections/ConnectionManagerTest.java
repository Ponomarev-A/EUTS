package connections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import packet.Command;
import packet.Packet;

import static org.junit.Assert.*;

/**
 * ConnectionManager test class
 */
public class ConnectionManagerTest {

    ConnectionManager connectionManager;

    @Before
    public void openConnection() throws Exception {
        connectionManager = new ConnectionManager(new UARTTest().createUARTConnection(), new ModBus());
        connectionManager.init();
    }

    @After
    public void closeConnection() throws Exception {
        if (connectionManager != null)
            connectionManager.getConnection().close();

        connectionManager = null;
    }

    @Test
    public void youCreateUARTConnectionWithModbusProtocol() throws Exception {
        Connection uart = new UART(new UARTTest().COM1_PORT_NAME);
        Protocol modbus = new ModBus();
        ConnectionManager connectionManager = new ConnectionManager(uart, modbus);

        assertNotNull(connectionManager);
        assertEquals(uart, connectionManager.getConnection());
        assertEquals(modbus, connectionManager.getProtocol());
    }

    @Test
    public void testGetConnectionAndProtocol() throws Exception {
        Connection connection = new UART(new UARTTest().COM1_PORT_NAME);
        Protocol protocol = new ModBus();
        ConnectionManager cm = new ConnectionManager(connection, protocol);

        assertEquals(connection, cm.getConnection());
        assertEquals(protocol, cm.getProtocol());
    }

    @Test
    public void testSendAndReceivePacket() throws Exception {
        Packet packetTestConnection = new Packet(Command.CHECK_CONNECTION_DEVICE, new byte[]{1, 2, 3, 4});

        assertTrue(connectionManager.sendPacket(packetTestConnection));
        assertEquals(packetTestConnection, connectionManager.receivePacket());
    }

    @Test
    public void testMultipleSendingAndRecevingPackets() throws Exception {
        Packet packetTestConnection = new Packet(Command.CHECK_CONNECTION_DEVICE, new byte[]{1, 2, 3, 4});

        for (int i = 0; i < 10; i++) {
            assertTrue(connectionManager.sendPacket(packetTestConnection));
            assertEquals(packetTestConnection, connectionManager.receivePacket());

        }
    }
}