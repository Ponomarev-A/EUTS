package connections;

import exception.FailedProtocolException;
import exception.InvalidCRCException;
import exception.InvalidPacketSize;
import packet.Packet;

/**
 * Class for management connections, protocols and packets.
 */
public class ConnectionManager {

    private Connection connection;
    private Protocol protocol;

    public ConnectionManager(Connection connection, Protocol protocol) {
        this.connection = connection;
        this.protocol = protocol;
    }

    public void open() {
        connection.open();
    }

    public void close() {
        connection.close();
    }

    public Packet receivePacket() {

        try {
            Packet receivedPacket = new Packet();

            byte[] readData = connection.read();
            byte[] unwrappedData = protocol.unwrap(readData);
            receivedPacket.unpack(unwrappedData);

            return receivedPacket;

        } catch (FailedProtocolException | InvalidCRCException e) {
            // TODO: handle receivePacket() exceptions
        } catch (Exception e) {
            // TODO: handle receivePacket() other exceptions
        }

        return null;
    }

    public boolean sendPacket(Packet packet) {

        if (packet != null) {
            try {
                byte[] packedData = packet.pack();
                byte[] wrappedData = protocol.wrap(packedData);
                connection.write(wrappedData);
                return true;

            } catch (InvalidPacketSize e) {
                // TODO: handle invalidPacketSize exception
            } catch (Exception e) {
                // TODO: handle sendPacket() exception
            }
        }

        return false;
    }
}
