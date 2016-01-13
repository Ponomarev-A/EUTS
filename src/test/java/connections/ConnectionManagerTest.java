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
        Connection uart = new UART();
        Protocol modbus = new ModBus();
        ConnectionManager connectionManager = new ConnectionManager(uart, modbus);

        assertNotNull(connectionManager);
        assertEquals(uart, connectionManager.getConnection());
        assertEquals(modbus, connectionManager.getProtocol());
    }

    @Test
    public void testGetConnectionAndProtocol() throws Exception {
        Connection connection = new UART();
        Protocol protocol = new ModBus();
        ConnectionManager cm = new ConnectionManager(connection, protocol);

        assertEquals(connection, cm.getConnection());
        assertEquals(protocol, cm.getProtocol());
    }

    @Test
    public void testReceivePacket() throws Exception {
        Packet packetTestConnection = new Packet(Command.CHECK_CONNECTION_DEVICE, new byte[]{1, 2, 3, 4});
        connectionManager.sendPacket(packetTestConnection);

        // TODO: fix receivePacket() func, add time delay to it.
        assertEquals(packetTestConnection, connectionManager.receivePacket());
    }

    @Test
    public void testSendPacket() throws Exception {
        Packet packet = new Packet(Command.MODE_DEVICE, new byte[]{1, 2, 3, 4, 5});
        assertTrue(connectionManager.sendPacket(packet));
    }
}