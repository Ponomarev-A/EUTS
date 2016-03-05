package connections;

import exception.InvalidProtocol;
import org.junit.Test;
import packet.Command;
import packet.Packet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Test ModBus class
 */
public class ModBusTest {

    Packet packet = new Packet(Command.BACKLIGHT_DEVICE, new byte[] {1,2,3,4});
    Protocol modbus = new ModBus();

    @Test
    public void testCreateProtocol() throws Exception {
        assertNotNull(new ModBus());
    }

    @Test
    public void youWrapPacketByProtocolRules() throws Exception {

        // Pack packet to byte array
        byte[] sentData = packet.pack();

        // Create byte arrays from open and close codes sequences and sent packet data
        byte[] openCodeSeq = new byte[]{0x3A};

        // TODO: Don't forgive revert CLOSE_CODE_SEQ values in TEST!
//        byte[] closeCodeSeq = new byte[]{0x0D, 0x0A};
        byte[] closeCodeSeq = new byte[]{0x2E, 0x2F};
        byte[] data = (byte[]) invokePrivateMethod(modbus, "byteArrayToASCIICodeArray", new Class[]{byte[].class}, new Object[]{sentData});
        assert data != null;

        byte[] result = new byte[openCodeSeq.length + data.length + closeCodeSeq.length];
        System.arraycopy(openCodeSeq, 0, result, 0, openCodeSeq.length);
        System.arraycopy(data, 0, result, openCodeSeq.length, data.length);
        System.arraycopy(closeCodeSeq, 0, result, openCodeSeq.length + data.length, closeCodeSeq.length);

        assertArrayEquals(result, modbus.wrap(sentData));
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
    public void youUnwrapPacketSuccessfully() throws Exception {

        // Pack packet to byte array
        byte[] sentData = modbus.wrap(packet.pack());

        // packet1 is the same as packet
        Packet packet1 = new Packet(packet);

        // packet2 is the empty packet, unwrapped form packet
        byte[] unwrappedData = modbus.unwrap(sentData);

        Packet packet2 = new Packet();
        packet2.unpack(unwrappedData);

        assertEquals(packet1.getCRC(),       packet2.getCRC());
        assertEquals(packet1.getCommand(),   packet2.getCommand());
        assertArrayEquals(packet1.getData(), packet2.getData());
    }

    @Test(expected = InvalidProtocol.class)
    public void youUnwrapPacketFailed() throws Exception {

        // Pack packet to byte array
        byte[] sentData = modbus.wrap(packet.pack());

        // Spoil sent packet (change CLOSE protocol tag)
        sentData[sentData.length - 1] = (byte) ~sentData[sentData.length - 1];

        // packet1 is the same as packet
        Packet packet1 = new Packet(packet);

        // packet2 is the empty packet, unwrapped form packet
        byte[] unwrappedData = modbus.unwrap(sentData);

        Packet packet2 = new Packet();
        packet2.unpack(unwrappedData);

        assertEquals(packet1.getCRC(),       packet2.getCRC());
        assertEquals(packet1.getCommand(),   packet2.getCommand());
        assertArrayEquals(packet1.getData(), packet2.getData());
    }

    @Test
    public void testByteArrayToASCIICodeArray() throws Exception {
        byte[] data = new byte[]{1, 0x0a, (byte) 0xb0, (byte) 0xc1, (byte) 0xdf};
        byte[] code = new byte[]{0x30, 0x31, 0x30, 0x61, 0x62, 0x30, 0x63, 0x31, 0x64, 0x66};

        byte[] result = (byte[]) invokePrivateMethod(modbus, "byteArrayToASCIICodeArray", new Class[]{byte[].class}, new Object[]{data});

        assertArrayEquals(code, result);
    }

    @Test
    public void testASCIICodeArrayToByteArray() throws Exception {
        byte[] data = new byte[]{1, 0x0a, (byte) 0xb0, (byte) 0xc1, (byte) 0xdf};
        byte[] code = new byte[]{0x30, 0x31, 0x30, 0x61, 0x62, 0x30, 0x63, 0x31, 0x64, 0x66};

        byte[] result = (byte[]) invokePrivateMethod(modbus, "ASCIICodeArrayToByteArray", new Class[]{byte[].class}, new Object[]{code});

        assertArrayEquals(data, result);
    }
}