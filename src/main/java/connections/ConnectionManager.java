package connections;

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

    @Override
    public String toString() {
        return "ConnectionManager{ " +
                "connection=" + connection +
                ", protocol=" + protocol +
                '}';
    }

    public Connection getConnection() {
        return connection;
    }

    public Packet receivePacket() throws Exception {

        Packet receivedPacket = new Packet();

        byte[] readData = connection.read();
        byte[] unwrappedData = protocol.unwrap(readData);
        receivedPacket.unpack(unwrappedData);

        return receivedPacket;
    }

    public boolean sendPacket(Packet packet) throws Exception {

        boolean result = false;

        if (packet != null) {
            byte[] packedData = packet.pack();
            byte[] wrappedData = protocol.wrap(packedData);
            result = connection.write(wrappedData);
        }

        return result;
    }
}
