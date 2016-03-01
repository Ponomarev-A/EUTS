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

    public void open() throws Exception {
        connection.open();
    }

    public void close() throws Exception {
        connection.close();
    }

    public Packet receivePacket() {

        Packet receivedPacket = new Packet();
        try {

            byte[] readData = connection.read();
            byte[] unwrappedData = protocol.unwrap(readData);
            receivedPacket.unpack(unwrappedData);

        } catch (FailedProtocolException | InvalidCRCException e) {
            // TODO: handle receivePacket() exceptions
        } catch (Exception e) {
            // TODO: handle receivePacket() other exceptions
        }

        return receivedPacket;
    }

    public boolean sendPacket(Packet packet) {

        boolean result = false;

        if (packet != null) {
            try {
                byte[] packedData = packet.pack();
                byte[] wrappedData = protocol.wrap(packedData);
                result = connection.write(wrappedData);

            } catch (InvalidPacketSize e) {
                // TODO: handle invalidPacketSize exception
                e.printStackTrace();
            } catch (Exception e) {
                // TODO: handle sendPacket() exception
                e.printStackTrace();
            }
        }

        return result;
    }
}
