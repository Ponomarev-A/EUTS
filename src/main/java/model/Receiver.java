package model;

import connections.ConnectionManager;
import packet.Command;
import packet.Packet;

/**
 * Receiver class: all information about connected receiver
 */
public class Receiver extends Device {

    private ConnectionManager connectionManager;
    private String info;

    public Receiver(ConnectionManager connectionManager) {
        super(connectionManager);
        this.connectionManager = connectionManager;
    }

    @Override
    public String getInfo() {

        // Request for device model name
        if (connectionManager.sendPacket(new Packet(Command.GET_INFO_DEVICE))) {

            // Wait while answer will be received from device
            info = connectionManager.receivePacket().getDataAsString();
        }

        return info.trim();
    }
}
