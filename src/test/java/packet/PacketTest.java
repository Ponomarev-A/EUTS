package packet;

import exception.InvalidCRC;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * Test class
 */
public class PacketTest {

    private static final byte[] DATA_1234 = new byte[]{0x01, 0x02, 0x03, 0x04};
    private static final byte[] DATA_6789 = new byte[]{0x06, 0x07, 0x08, 0x09};

    private Packet packet = new Packet();

    @Test
    public void youCreateNewEmptyPacket() throws Exception {
        assertNotNull(packet);
        assertEquals(Command.NO_COMMAND, packet.getCommand());
        assertArrayEquals(new byte[0], packet.getData());
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
        packet.setData(DATA_1234);
        packet.updateCRC();

        assertEquals((short) 0x9E33, packet.getCRC());
    }

    @Test
    public void youGetDataFromPacket() throws Exception {
        byte[] data = DATA_1234;
        packet.setData(data);
        assertEquals(data, packet.getData());
    }

    @Test
    public void youGetDataAsString() throws Exception {
        byte[] data = "1234".getBytes(StandardCharsets.US_ASCII);
        packet.setData(data);

        assertEquals("1234", packet.getDataAsString());
    }

    @Test
    public void youGetDataAsInt() throws Exception {
        byte[] data = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE).putInt(1234).array();
        packet.setData(data);

        assertEquals(1234, packet.getDataAsInt());
    }

    @Test
    public void youGetDataAsFloatArray() throws Exception {
        float[] fArr = new float[]{1.0f, 0.1f};
        byte[] bArr = new byte[]{0x00, 0x00, (byte) 0x80, 0x3F, (byte) 0xCD, (byte) 0xCC, (byte) 0xCC, (byte) 0x3D};

        packet.setData(bArr);
        assertArrayEquals(fArr, packet.getDataAsFloatArray(), 0.0001f);
    }

    @Test
    public void youGetByteArrayFromFloatArray() throws Exception {
        float[] fArr = new float[]{1.0f, 0.1f};
        byte[] bArr = new byte[]{0x00, 0x00, (byte) 0x80, 0x3F, (byte) 0xCD, (byte) 0xCC, (byte) 0xCC, (byte) 0x3D};

        assertArrayEquals(bArr, new Packet(Command.NO_COMMAND, fArr).getData());
    }

    @Test
    public void youWantToUpdateCRC() throws Exception {
        packet.setData(DATA_6789);

        assertEquals((short) 0x485A, packet.updateCRC());
    }

    @Test
    public void checkCRC16Calculation() throws Exception {

        assertEquals((short) 0xAF7E, invokePrivateMethod(
                packet,
                "calculateCRC16",
                new Class[]{byte[].class},
                new Object[]{new byte[]{0x31, 0x32, 0x33}}));
        assertEquals((short) 0xFEE8, invokePrivateMethod(
                packet,
                "calculateCRC16",
                new Class[]{byte[].class},
                new Object[]{"123456789".getBytes()}));
    }

    private Object invokePrivateMethod(Object targetObject, String methodName, Class[] argClasses, Object[] argObjects) {
        try {
            Method method = targetObject.getClass().getDeclaredMethod(methodName, argClasses);
            method.setAccessible(true);
            return method.invoke(targetObject, argObjects);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void youPackPacket() throws Exception {

        // Test parameters
        Command command = Command.FREQUENCY_DEVICE;
        byte[] data = DATA_6789;
        short CRC = (short)0x485A;

        int command_length = Short.SIZE / Byte.SIZE;
        int CRC_length = Short.SIZE / Byte.SIZE;
        int frameLength = command_length + data.length + CRC_length;

        // Create new packet
        packet.setCommand(command);
        packet.setData(data);
        packet.updateCRC();

        //  Create byte arrays
        byte[] aCnt = (byte[]) invokePrivateMethod(packet, "toByteArray", new Class[]{short.class}, new Object[]{(short) frameLength});
        byte[] aCmd = (byte[]) invokePrivateMethod(packet, "toByteArray", new Class[]{short.class}, new Object[]{(short) command.getId()});
        byte[] aCRC = (byte[]) invokePrivateMethod(packet, "toByteArray", new Class[]{short.class}, new Object[]{CRC});
        assert aCnt != null;
        assert aCmd != null;
        assert aCRC != null;

        // Create byte array packed packet
        byte[] result = new byte[aCnt.length + aCmd.length + data.length + aCRC.length];
        System.arraycopy(aCnt, 0, result, 0, aCnt.length);
        System.arraycopy(aCmd, 0, result, aCnt.length, aCmd.length);
        System.arraycopy(data, 0, result, aCnt.length + aCmd.length, data.length);
        System.arraycopy(aCRC, 0, result, aCnt.length + aCmd.length + data.length, aCRC.length);

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

    @Test(expected = InvalidCRC.class)
    public void youUnpackInvalidPacket() throws Exception {

        // Create send packet
        Packet sendPacket = new Packet(Command.GAIN_DEVICE, DATA_6789);

        // Pack send packet
        byte[] sentData = sendPacket.pack();

        // Spoil sent packet
        sentData[sentData.length - 1] = (byte) ~sentData[sentData.length - 1];

        // Create empty received packet
        Packet receivedPacket = new Packet();

        // Unpack sent data to received packet, catch InvalidCRC here!
        receivedPacket.unpack(sentData);
    }
}