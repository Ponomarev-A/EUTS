package model;

import connections.ConnectionManager;
import controller.Controller;
import exception.FailReceivePacket;
import exception.FailSendPacket;
import packet.Command;
import packet.Packet;

import static packet.Command.*;

/**
 * Device class: all information about current testing device
 */
public abstract class Device {

    final Controller controller;
    private final ConnectionManager connectionManager;
    private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;

    Device(ConnectionManager connectionManager, Controller controller) {
        this.connectionManager = connectionManager;
        this.controller = controller;
    }

    public abstract boolean readInfo();

    public abstract String getID();

    public abstract String getModel();

    public abstract String getScheme();

    public abstract String getFirmware();

    void checkConnectionStatus() {
        try {
            connectionStatus = (connectionManager.getConnection().isOpened()) ?
                    ConnectionStatus.checkStatus(this) :
                    ConnectionStatus.DISCONNECTED;
        } catch (Exception e) {
            connectionStatus = ConnectionStatus.DISCONNECTED;
            controller.showErrorMessage(
                    "Check " + this + " connection status",
                    "Read " + this + " connection status is failed.\nTry again connect to device!",
                    e);
        }
    }

    ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public void set(Command command) throws Exception {
        set(new Packet(command));
    }

    private void set(Packet packet) throws Exception {
        try {
            connectionManager.sendPacket(packet);
            if (isConfirmationRequired(packet)) {
                if (connectionManager.receivePacket().getDataAsByte() == 1)
                    throw new FailSendPacket("No confirmation has been received.");
            }
        } catch (Exception e) {
            FailSendPacket failSendPacket = new FailSendPacket(String.format("Can't send command %s\n with value %s\n to device %s",
                    packet.getCommand(), packet.getDataAsInt(), this.toString()));
            failSendPacket.initCause(e);

            e.printStackTrace();

            throw failSendPacket;
        }
    }

    private boolean isConfirmationRequired(Packet packet) {
        int id = packet.getCommand().getId();
        return (id >= FREQUENCY_DEVICE.getId() && id <= BACKLIGHT_DEVICE.getId()) ||
                (id >= FREQUENCY_STAND.getId() && id <= EXT_SENSOR_STAND.getId());
    }

    public void set(Command command, Integer integerValue) throws Exception {
        set(new Packet(command, integerValue));
    }

    int getByte() throws Exception {
        return get().getDataAsByte();
    }

    private Packet get() throws Exception {
        Packet packet = new Packet();
        try {
            packet = connectionManager.receivePacket();
            checkPacketContainsErrorInfo(packet);
        } catch (Exception e) {
            FailReceivePacket failReceivePacket = new FailReceivePacket(String.format("Receive packet %s\n with value %s\n from device %s",
                    packet.getCommand(), packet.getDataAsInt(), this.toString()));
            failReceivePacket.initCause(e);

            e.printStackTrace();

            throw failReceivePacket;
        }

        return packet;
    }

    private void checkPacketContainsErrorInfo(Packet packet) throws FailReceivePacket {
        switch (packet.getCommand()) {
            case INVALID_CMD_DEVICE:
            case INVALID_CMD_STAND:
            case INVALID_CRC_DEVICE:
            case INVALID_CRC_STAND:
            case INVALID_DATA_DEVICE:
            case INVALID_DATA_STAND:
            case ERROR_DEVICE:
            case ERROR_STAND:
                throw new FailReceivePacket("Has been received packet with error information.");
        }
    }

    int getInteger() throws Exception {
        return get().getDataAsInt();
    }

    public short[] getArray() throws Exception {
        return get().getDataAsShortArray();
    }

    String getString() throws Exception {
        return get().getDataAsString();
    }

    public enum SignalType {SOLID, PULSE}

    public enum ExtSensors {INT, KI, DODK, DKI}
}
