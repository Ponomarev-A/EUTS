package packet;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Packet class.
 *
 * Packet structure:
 *  CRC32 - const 4 byte length
 *  Command - const 4 byte length
 *  Data - various length
 */
public class Packet {

    private Command command;
    private int CRC;
    private byte[] data;

    public Packet(Packet packet) {
        this.command = packet.command;
        this.CRC     = packet.CRC;
        this.data    = packet.data;
    }

    public Packet() {
        this.command = null;
        this.CRC     = 0;
        this.data    = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Packet packet = (Packet) o;

        return  getCRC() == packet.getCRC() &&
                getCommand() == packet.getCommand() &&
                Arrays.equals(getData(), packet.getData());
    }

    @Override
    public int hashCode() {
        int result = getCommand() != null ? getCommand().hashCode() : 0;
        result = 31 * result + getCRC();
        result = 31 * result + Arrays.hashCode(getData());
        return result;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public int getCRC() {
        return CRC;
    }

    public void setCRC(int CRC) {
        this.CRC = CRC;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }


    public byte[] pack() {
        byte[] result = new byte[0];

        result = ArrayUtils.addAll(result, ByteBuffer.allocate(Integer.SIZE/Byte.SIZE).putInt(CRC).array());
        result = ArrayUtils.addAll(result, ByteBuffer.allocate(Integer.SIZE/Byte.SIZE).putInt(command.ordinal()).array());
        result = ArrayUtils.addAll(result, data);

        return result;
    }

    public void unpack(byte[] packedPack) {

        byte[] ba_CRC       = ArrayUtils.subarray(packedPack, 0,   4);
        byte[] ba_command   = ArrayUtils.subarray(packedPack, 4,   8);
        byte[] ba_data      = ArrayUtils.subarray(packedPack, 8,   packedPack.length);

        setCRC(ByteBuffer.wrap(ba_CRC).getInt());
        setCommand(Command.values()[ByteBuffer.wrap(ba_command).getInt()]);
        setData(ba_data);
    }
}
