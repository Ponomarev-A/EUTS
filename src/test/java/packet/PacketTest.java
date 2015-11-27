package packet;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * Test class
 */
public class PacketTest {

    public static final byte[] DATA_6789 = new byte[]{0x06, 0x07, 0x08, 0x09};
    public static final byte[] DATA_1234 = new byte[]{0x01, 0x02, 0x03, 0x04};

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
        Packet packet = new Packet();
        packet.setCommand(Command.CHECK_KEYBOARD_DEVICE);
        packet.setData(DATA_1234);
        packet.updateCRC();

        // Manual calculating by http://depa.usst.edu.cn/chenjq/www2/software/crc/CRC_Javascript/CRCcalculation.htm
        // CRC polynom      (hex)   0x8005
        // Initial value    (hex)   0
        // Final XOR value  (hex)   0

        assertEquals((short) 0x9D8B, packet.getCRC());
    }

    @Test
    public void youGetDataFromPacket() throws Exception {
        byte[] data = DATA_1234;
        packet.setData(data);
        assertEquals(data, packet.getData());
    }

    @Test
    public void youPackPacket() throws Exception {

        Command command = Command.FREQUENCY_DEVICE;
        byte[] data = DATA_6789;

        Packet packet = new Packet(command, data);
        packet.setCommand(command);
        packet.setData(data);
        packet.updateCRC();

        byte[] cmd  = packet.getByteArrayFromShort((short) command.ordinal());
        byte[] CRC  = ByteBuffer.allocate(Short.SIZE/Byte.SIZE).putShort(packet.getCRC()).array();

        byte[] result = new byte[CRC.length + cmd.length + data.length];
        System.arraycopy(CRC,   0, result, 0,                       CRC.length  );
        System.arraycopy(cmd,   0, result, CRC.length,              cmd.length  );
        System.arraycopy(data,  0, result, CRC.length + cmd.length, data.length );

        assertArrayEquals(result, packet.pack());
    }

    @Test
    public void youUnpackValidPacket() throws Exception {

        Packet sendPacket = new Packet();
        sendPacket.setCommand(Command.GAIN_DEVICE);
        sendPacket.setData(DATA_6789);

        byte[] sentData = sendPacket.pack();

        Packet receivedPacket = new Packet();
        receivedPacket.unpack(sentData);

        assertEquals(sendPacket.getCRC(),       receivedPacket.getCRC());
        assertEquals(sendPacket.getCommand(),   receivedPacket.getCommand());
        assertArrayEquals(sendPacket.getData(), receivedPacket.getData());
    }

    @Test
    public void checkCRC16Calculation() throws Exception {
        assertEquals((short) 0xAF7E, packet.calculateCRC16(new byte[] {0x31, 0x32, 0x33}));
        assertEquals((short) 0xFEE8, packet.calculateCRC16("123456789".getBytes()));
    }

    @Test
    public void concatCommandAndData() throws Exception {
        Packet packet = new Packet();
        packet.setCommand(Command.BACKLIGHT_DEVICE);
        packet.setData(DATA_6789);

        byte[] cmd = packet.getByteArrayFromShort((short) packet.getCommand().ordinal());
        byte[] result = new byte[cmd.length + DATA_6789.length];
        System.arraycopy(cmd,       0, result, 0,           cmd.length);
        System.arraycopy(DATA_6789, 0, result, cmd.length,  DATA_6789.length);

        assertArrayEquals(result, packet.concatCommandAndData(
                packet.getByteArrayFromShort((short) packet.getCommand().ordinal()),
                packet.getData()));
    }
}