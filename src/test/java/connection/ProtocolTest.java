package connection;

import org.junit.Test;
import packet.Packet;

import static org.junit.Assert.assertEquals;

/**
 * Test Protocol class
 */
public class ProtocolTest {

    @Test
    public void youCreateProtocolObjectFromPacket() throws Exception {
        Packet packet = new Packet();
        Protocol protocol = new Protocol(packet);

        assertEquals(packet, protocol.getPacket());
    }

    @Test
    public void youWrapPacketByProtocolRules() throws Exception {
        Packet packet = new Packet();
        packet.setData(new byte[] {1,2,3,4});


    }
}