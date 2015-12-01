package packet;

import exception.InvalidCRCException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class
 */
public class PacketTest {

    public static final byte[] DATA_1234 = new byte[]{0x01, 0x02, 0x03, 0x04};
    public static final byte[] DATA_6789 = new byte[]{0x06, 0x07, 0x08, 0x09};

    Packet packet = new Packet();

    @Test
    public void youCreateNewEmptyPacket() throws Exception {
        assertNotNull(packet);
        assertNull(packet.getCommand());
        assertNull(packet.getData());
        assertEquals(0, packet.getCRC());
    }

    @Test
    public void youCreatePacketWithParameters() throws Exception {
        Packet somePacket = new Packet(Command.BACKLIGHT_DEVICE, DATA_1234);

        assertNotNull(somePacket);
        assertEquals(Command.BACKLIGHT_DEVICE, somePacket.getCommand());
        assertArrayEquals(DATA_1234, somePacket.getData());
    }

    @Test
    public void youCreatePacketFromExistingPacket() throws Exception {
        Packet somePacket = new Packet(packet);

        assertNotNull(somePacket);
        assertTrue(somePacket.equals(packet));
    }

    @Test
    public void youGetCommandFromPacket() throws Exception {
        packet.setCommand(Command.FREQUENCY_DEVICE);
        assertEquals(Command.FREQUENCY_DEVICE, packet.getCommand());
    }

    @Test
    public void youGetCRCFromPacket() throws Exception {
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
    public void youWantToUpdateCRC() throws Exception {
        packet.setCommand(Command.ERROR_DEVICE);
        packet.setData(DATA_6789);

        assertEquals((short) 0x4B6A, packet.updateCRC());
    }

    @Test
    public void checkCRC16Calculation() throws Exception {
        assertEquals((short) 0xAF7E, packet.calculateCRC16(new byte[] {0x31, 0x32, 0x33}));
        assertEquals((short) 0xFEE8, packet.calculateCRC16("123456789".getBytes()));
    }

    @Test
    public void youPackPacket() throws Exception {

        // Test parameters
        Command command = Command.FREQUENCY_DEVICE;
        byte[] data = DATA_6789;
        short CRC = (short)0x485A;

        // Create new packet
        packet.setCommand(command);
        packet.setData(data);
        packet.updateCRC();

        //  Create byte arrays
        byte[] aCmd  = packet.getByteArrayFromShort((short) command.ordinal());
        byte[] aCRC  = packet.getByteArrayFromShort(CRC);

        // Create byte array packed packet
        byte[] result = new byte[aCRC.length + aCmd.length + data.length];
        System.arraycopy(aCRC,   0, result, 0,                          aCRC.length  );
        System.arraycopy(aCmd,   0, result, aCRC.length,                aCmd.length  );
        System.arraycopy(data,  0, result, aCRC.length + aCmd.length,   data.length );

        assertArrayEquals(result, packet.pack());
    }

    @Test
    public void youUnpackValidPacket() throws Exception {

        // Create send packet
        Packet sendPacket = new Packet(Command.GAIN_DEVICE, DATA_6789);

        // Pack send packet
        byte[] sentData = sendPacket.pack();

        // Create empty received packet
        Packet receivedPacket = new Packet();

        // Unpack sent data to received packet
        receivedPacket.unpack(sentData);

        assertEquals(sendPacket.getCRC(),       receivedPacket.getCRC());
        assertEquals(sendPacket.getCommand(),   receivedPacket.getCommand());
        assertArrayEquals(sendPacket.getData(), receivedPacket.getData());
    }

    @Test(expected = InvalidCRCException.class)
    public void youUnpackInvalidPacket() throws Exception {

        // Create send packet
        Packet sendPacket = new Packet(Command.GAIN_DEVICE, DATA_6789);

        // Pack send packet
        byte[] sentData = sendPacket.pack();

        // Spoil sent packet
        sentData[sentData.length - 1] = (byte) ~sentData[sentData.length - 1];

        // Create empty received packet
        Packet receivedPacket = new Packet();

        // Unpack sent data to received packet
        receivedPacket.unpack(sentData);

        assertEquals(sendPacket.getCRC(),       receivedPacket.getCRC());
        assertEquals(sendPacket.getCommand(),   receivedPacket.getCommand());
        assertArrayEquals(sendPacket.getData(), receivedPacket.getData());
    }

    @Test
    public void getByteArrayFromShort() throws Exception {
        assertArrayEquals(new byte[] {0x04, (byte) 0xD2}, packet.getByteArrayFromShort((short) 1234));
    }

    @Test
    public void getShortFromByteArray() throws Exception {
        assertEquals((short) 4660, packet.getShortFromByteArray(new byte[] {0x12, 0x34}));
    }


    @Test
    public void concatCommandAndData() throws Exception {

        // Push data to packet
        packet.setCommand(Command.BACKLIGHT_DEVICE);
        packet.setData(DATA_6789);

        // Create byte arrays and sum them
        byte[] aCmd = packet.getByteArrayFromShort((short) packet.getCommand().ordinal());
        byte[] aData = packet.getData();
        byte[] result = new byte[aCmd.length + aData.length];
        System.arraycopy(aCmd,  0, result, 0,           aCmd.length);
        System.arraycopy(aData, 0, result, aCmd.length, aData.length);

        assertArrayEquals(result, packet.concatCommandAndData(
                packet.getByteArrayFromShort((short) packet.getCommand().ordinal()),
                packet.getData()));
    }
}