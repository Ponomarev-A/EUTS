package connections;

import exception.FailedProtocolException;
import exception.InvalidCRCException;
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

    public void init() {
        connection.open();
    }


    public Connection getConnection() {
        return connection;
    }

    public Protocol getProtocol() {
        return protocol;
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

            byte[] packedData = packet.pack();
            byte[] wrappedData = protocol.wrap(packedData);

            try {
                connection.write(wrappedData);
                return true;

            } catch (Exception e) {
                // TODO: handle sendPacket() exception
            }
        }

        return false;
    }
}
