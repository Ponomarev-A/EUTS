package connection;

import packet.Packet;

/**
 * Class Protocol used for wrapping transmitted data by control symbols.
 */
public class Protocol {
    private Packet packet;

    public Protocol(Packet packet) {
        this.packet = packet;
    }

    public Packet getPacket() {
        return packet;
    }
}
