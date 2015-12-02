package connection;

import exception.FailedProtocolException;
import org.junit.Test;
import packet.Command;
import packet.Packet;

import static org.junit.Assert.*;

/**
 * Test Protocol class
 */
public class ProtocolTest {

    Packet packet = new Packet(Command.BACKLIGHT_DEVICE, new byte[] {1,2,3,4});

    @Test
    public void testProtocol() throws Exception {
        assertNotNull(new Protocol());
    }

    @Test
    public void youWrapPacketByProtocolRules() throws Exception {

        // Pack packet to byte array
        byte[] sentData = packet.pack();

        // Create byte arrays from open and close codes sequences and sent packet data
        byte[] openCodeSeq  = Protocol.OPEN_CODE_SEQ;
        byte[] closeCodeSeq = Protocol.CLOSE_CODE_SEQ;
        byte[] result = new byte[openCodeSeq.length + sentData.length + closeCodeSeq.length];
        System.arraycopy(openCodeSeq,   0, result, 0,                                       openCodeSeq.length);
        System.arraycopy(sentData,      0, result, openCodeSeq.length,                      sentData.length);
        System.arraycopy(closeCodeSeq,  0, result, openCodeSeq.length + sentData.length,    closeCodeSeq.length);

        assertArrayEquals(result, Protocol.wrap(sentData));
    }

    @Test
    public void youUnwrapPacketSuccessfully() throws Exception {

        // Pack packet to byte array
        byte[] sentData = Protocol.wrap(packet.pack());

        // packet1 is the same as packet
        Packet packet1 = new Packet(packet);

        // packet2 is the empty packet, unwrapped form packet
        byte[] unwrappedData = Protocol.unwrap(sentData);

        Packet packet2 = new Packet();
        packet2.unpack(unwrappedData);

        assertEquals(packet1.getCRC(),       packet2.getCRC());
        assertEquals(packet1.getCommand(),   packet2.getCommand());
        assertArrayEquals(packet1.getData(), packet2.getData());
    }

    @Test(expected = FailedProtocolException.class)
    public void youUnwrapPacketFailed() throws Exception {

        // Pack packet to byte array
        byte[] sentData = Protocol.wrap(packet.pack());

        // Spoil sent packet (change CLOSE protocol tag)
        sentData[sentData.length - 1] = (byte) ~sentData[sentData.length - 1];

        // packet1 is the same as packet
        Packet packet1 = new Packet(packet);

        // packet2 is the empty packet, unwrapped form packet
        byte[] unwrappedData = Protocol.unwrap(sentData);

        Packet packet2 = new Packet();
        packet2.unpack(unwrappedData);

        assertEquals(packet1.getCRC(),       packet2.getCRC());
        assertEquals(packet1.getCommand(),   packet2.getCommand());
        assertArrayEquals(packet1.getData(), packet2.getData());
    }
}