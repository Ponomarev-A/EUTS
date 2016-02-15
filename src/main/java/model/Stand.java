package model;

import connections.ConnectionManager;
import packet.Command;
import packet.Packet;

/**
 * Stand class: all information about current used fot tests stand
 */
public class Stand {

    private ConnectionManager connectionManager;
    private String info;

    public Stand(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public String getInfo() {
        // Request for device model name
        if (connectionManager.sendPacket(new Packet(Command.GET_INFO_STAND))) {

            // Wait while answer will be received from device
            info = connectionManager.receivePacket().getDataAsString();
        }

        return info.trim();
    }
}
