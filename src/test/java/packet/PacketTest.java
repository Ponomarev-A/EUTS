package packet;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * Test class
 */
public class PacketTest {

    Packet packet = new Packet();

    @Test
    public void youCreateNewPacket() throws Exception {
        assertNotNull(new Packet());
    }

    @Test
    public void youCreatePacketFromExistingPacket() throws Exception {
        Packet existingPacket = new Packet();
        Packet somePacket = new Packet(existingPacket);
        assertTrue(existingPacket.equals(somePacket));
    }

    @Test
    public void youGetCommandFromPacket() throws Exception {
        packet.setCommand(Command.FREQUENCY_DEVICE);
        assertEquals(Command.FREQUENCY_DEVICE, packet.getCommand());
    }

    @Test
    public void youGetCRCFromPacket() throws Exception {
        packet.setCRC(12345678);
        assertEquals(12345678, packet.getCRC());
    }

    @Test
    public void youGetDataFromPacket() throws Exception {
        byte[] data = new byte[] {1, 2, 3, 4};
        packet.setData(data);
        assertEquals(data, packet.getData());
    }

    @Test
    public void youPackPacket() throws Exception {
        Packet packet = new Packet();
        packet.setCRC(1234);
        packet.setCommand(Command.FREQUENCY_DEVICE);
        packet.setData(new byte[] {6,7,8,9});

        byte[] result = new byte[0];
        result = ArrayUtils.addAll(result, ByteBuffer.allocate(Integer.SIZE/Byte.SIZE).putInt(1234).array());
        result = ArrayUtils.addAll(result, ByteBuffer.allocate(Integer.SIZE/Byte.SIZE).putInt(Command.FREQUENCY_DEVICE.ordinal()).array());
        result = ArrayUtils.addAll(result, new byte[] {6,7,8,9});

        assertArrayEquals(result, packet.pack());
    }

    @Test
    public void youUnpackPacket() throws Exception {
        Packet packet = new Packet();
        assertNotNull(packet);

        packet.setCRC(9876);
        packet.setCommand(Command.GAIN_DEVICE);
        packet.setData(new byte[] {4,3,2,1});
        byte[] packedPack = packet.pack();


        Packet somePacket = new Packet();
        assertNotNull(somePacket);
        somePacket.unpack(packedPack);

        assertEquals(9876, somePacket.getCRC());
        assertEquals(Command.GAIN_DEVICE, somePacket.getCommand());
        assertArrayEquals(new byte[] {4,3,2,1}, somePacket.getData());
    }
}