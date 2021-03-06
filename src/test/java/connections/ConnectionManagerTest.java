package connections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import packet.Command;
import packet.Packet;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * ConnectionManager test class
 */
public class ConnectionManagerTest {

    private ConnectionManager connectionManager;

    @Before
    public void openConnection() throws Exception {
        connectionManager = new ConnectionManager(UARTTest.createUARTConnection(), new ModBus());
        connectionManager.getConnection().open();

        assertTrue(connectionManager.getConnection().isOpened());
    }

    @After
    public void closeConnection() throws Exception {
        assertTrue(connectionManager.getConnection().close());
    }

    @Test
    public void testSendAndReceivePacket() throws Exception {
        Packet packetTestConnection = new Packet(Command.CHECK_CONNECTION_DEVICE, new byte[]{1, 2, 3, 4});

        assertTrue(connectionManager.sendPacket(packetTestConnection));
        assertEquals(packetTestConnection, connectionManager.receivePacket());
    }

    @Test
    public void testMultipleSendingAndReceivingSamePackets() throws Exception {
        Packet packetTestConnection = new Packet(Command.CHECK_CONNECTION_DEVICE, new byte[]{1, 2, 3, 4});

        for (int i = 0; i < 5; i++) {
            assertTrue(connectionManager.sendPacket(packetTestConnection));
            assertEquals("iteration #" + i, packetTestConnection, connectionManager.receivePacket());
        }
    }

    @Test
    public void testMultipleSendingAndReceivingDiffPackets() throws Exception {
        Packet packetTestConnection = new Packet(Command.CHECK_CONNECTION_DEVICE);

        for (int i = 0; i < 5; i++) {
            packetTestConnection.setData(i * 1234);
            assertTrue(connectionManager.sendPacket(packetTestConnection));
            assertEquals("iteration #" + i, packetTestConnection, connectionManager.receivePacket());
        }
    }

    @Test
    public void testSendAndReceiveBigPacket() throws Exception {
        Random random = new Random();
        byte[] bigData = new byte[512];
        for (int i = 0; i < bigData.length; i++) {
            bigData[i] = (byte) ('0' + random.nextInt(10));
        }


        Packet bigPacket = new Packet(Command.CHECK_CONNECTION_DEVICE, bigData);
        assertTrue(connectionManager.sendPacket(bigPacket));
        assertEquals(bigPacket, connectionManager.receivePacket());
    }

    @Test
    public void testSendPacketWithConfirmation() throws Exception {

        // Valid packet
        connectionManager.sendPacket(new Packet(Command.FREQUENCY_DEVICE, 1024));
        assertEquals(0, connectionManager.receivePacket().getDataAsByte());

        // Invalid packet
        connectionManager.sendPacket(new Packet(Command.FREQUENCY_DEVICE, 1000));
        assertEquals(1, connectionManager.receivePacket().getDataAsByte());
    }

    @Test
    public void testMultipleSendDiffPackets() throws Exception {

        Packet packetTestConnection = new Packet(Command.FREQUENCY_DEVICE, 1000);

        for (int i = 0; i < 5; i++) {
            packetTestConnection.setData(i * 5);
            assertTrue("iteration #" + i, connectionManager.sendPacket(packetTestConnection));
            assertEquals("iteration #" + i, 1, connectionManager.receivePacket().getDataAsByte());
        }
    }
}