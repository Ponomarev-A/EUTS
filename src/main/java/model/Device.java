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

    private ConnectionManager CM;
    private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;

    public Device(Controller controller, ConnectionManager CM) {
        this.CM = CM;
        this.controller = controller;
    }

    public Device() {
        this.CM = null;
        this.controller = null;
    }

    public abstract boolean readInfo();

    public abstract Integer getID();

    public abstract String getModel();

    public abstract String getScheme();

    public abstract String getFirmware();

    void checkConnectionStatus() {
        try {
            connectionStatus = (CM != null && CM.getConnection().isOpened()) ?
                    ConnectionStatus.checkStatus(this) :
                    ConnectionStatus.DISCONNECTED;
        } catch (Exception e) {
            connectionStatus = ConnectionStatus.DISCONNECTED;
            if (controller != null) {
                controller.showErrorMessage(
                        "Check " + (this.toString()).split(" ")[0] + " connection status",
                        "Read " + this + " connection status is failed.\nTry again connect to device!",
                        e);
            }
        } finally {
            if (controller != null) {
                controller.updateLog(this + " is " + connectionStatus);
            }
        }
    }

    ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    int getByte(Command command) throws Exception {
        set(command);
        return get().getDataAsByte();
    }

    private void set(Command command) throws Exception {
        set(new Packet(command));
    }

    private Packet get() throws Exception {
        Packet packet = new Packet();
        try {
            packet = CM != null ? CM.receivePacket() : packet;
            checkPacketContainsErrorInfo(packet);
        } catch (InterruptedException e) {
            throw new Exception("Execution operation was interrupted.");
        } catch (Exception e) {
            FailReceivePacket failReceivePacket = new FailReceivePacket(String.format("Receive packet %s\n with value %s\n from device %s",
                    packet.getCommand(), packet.getDataAsInt(), this.toString()));
            failReceivePacket.initCause(e);

            e.printStackTrace();

            throw failReceivePacket;
        }

        return packet;
    }

    private void set(Packet packet) throws Exception {
        try {
            if (CM == null)
                return;

            CM.sendPacket(packet);
            if (!isConfirmationReceived(packet.getCommand()))
                throw new FailSendPacket("No confirmation has been received.");

        } catch (InterruptedException e) {
            throw new InterruptedException("Execution operation was interrupted.");
        } catch (Exception e) {
            FailSendPacket failSendPacket = new FailSendPacket(String.format("Can't send command %s\n with value %s\n to device %s",
                    packet.getCommand(), packet.getDataAsInt(), this.toString()));
            failSendPacket.initCause(e);

            e.printStackTrace();

            throw failSendPacket;
        }
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

    private boolean isConfirmationReceived(Command command) throws Exception {
        if (isConfirmationRequired(command) && CM != null) {
            Packet confirmationPacket = CM.receivePacket();
            if (!confirmationPacket.getCommand().equals(command) ||
                    confirmationPacket.getDataAsByte() == Confirmation.FAIL.ordinal())
                return false;
        }
        return true;
    }

    private boolean isConfirmationRequired(Command command) {
        int id = command.getId();
        return (id >= FREQUENCY_DEVICE.getId() && id <= BACKLIGHT_DEVICE.getId() || id == WRITE_PCB_ID_DEVICE.getId()) ||
                (id >= FREQUENCY_STAND.getId() && id <= EXT_SENSOR_STAND.getId());
    }

    int getInteger(Command command, Integer integerValue) throws Exception {
        set(command, integerValue);
        return get().getDataAsInt();
    }

    public void set(Command command, Integer integerValue) throws Exception {
        set(new Packet(command, integerValue));
    }

    public void set(Command command, float[] floatArray) throws Exception {
        set(new Packet(command, floatArray));
    }

    public short[] getArray(Command command) throws Exception {
        set(command);
        return get().getDataAsShortArray();
    }

    public float[] getFloatArray(Command command) throws Exception {
        set(command);
        return get().getDataAsFloatArray();
    }

    String getString(Command command) throws Exception {
        set(command);
        return get().getDataAsString();
    }


    private enum Confirmation {SUCCESS, FAIL}

    public enum SignalType {SOLID, PULSE}

    public enum ExtSensors {INT, KI, DODK, DKI}

}
