package packet;

import exception.InvalidCRCException;
import exception.InvalidPacketSize;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;

/*
 * Packet class.
 *
 * Packet structure:
 *  | SOF | LENGTH | COMMAND |     DATA     | CRC16 | EOF |
 *
 *  1) SOF (const value with const size 1 byte) - start of frame (0x3a)
 *
 *  2) Length (var value with const size 2 bytes) - length of command (2 bytes), data array (N bytes) and CRC16 (2 bytes)
 *     LENGTH = COMMAND + DATA + CRC16
 *
 *  3) Command (var value with const size 2 bytes) - one of enum
 *
 *  4) Data (var value with var size N bytes) - data array (may be missing, i.e. have 0 size)
 *
 *  5) CRC16 (var value with const size 2 bytes) - CRC of data array
 *
 *  6) EOF (const value with const size 2 bytes) - end of frame (0x0d, 0x0a)
 */
public class Packet {

    private static final int FRAME_SIZE = Short.SIZE / Byte.SIZE;   // 2 bytes
    private static final int CRC_SIZE = Short.SIZE / Byte.SIZE;   // 2 bytes
    private static final int COMMAND_SIZE = Short.SIZE / Byte.SIZE;   // 2 bytes

    private static final short CRC_POLYNOMIAL = (short)0x8005;

    private Command command;
    private byte[] data;
    private short CRC;


    public Packet(Packet packet) {
        this.command = packet.command;
        this.data    = packet.data;
        this.CRC     = packet.CRC;
    }

    public Packet() {
        this.command = null;
        this.data = new byte[0];
        this.CRC     = 0;
    }

    public Packet(Command command) {
        this.command = command;
        this.data = new byte[0];
        this.CRC = 0;
    }

    public Packet(Command command, byte[] data) {
        this.command = command;
        this.data    = data;
        this.CRC     = 0;
    }

    public Packet(Command command, short data) {
        this.command = command;
        this.data = getByteArrayFromShort(data);
        this.CRC = 0;
    }

    public Packet(Command command, int data) {
        this.command = command;
        this.data = getByteArrayFromInt(data);
        this.CRC = 0;
    }


    @Override
    public int hashCode() {
        int result = getCommand() != null ? getCommand().hashCode() : 0;
        result = 31 * result + getCRC();
        result = 31 * result + Arrays.hashCode(getData());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Packet packet = (Packet) o;

        return getCRC() == packet.getCRC() &&
                getCommand() == packet.getCommand() &&
                Arrays.equals(getData(), packet.getData());
    }

    @Override
    public String toString() {
        return "Packet{" +
                "command=" + command +
                ", data=" + Arrays.toString(data) +
                '}';
    }

    public Command getCommand() {
        return command;
    }

    public short getCRC() {
        return CRC;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setCRC(short CRC) {
        this.CRC = CRC;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    /**
     * Packed packet format:
     * <p> Command (2 byte) </p>
     * <p> Data (N byte(s)) </p>
     * <p> CRC16 (2 byte) </p>
     * @return packed packet array
     */
    public byte[] pack() {

        int length = COMMAND_SIZE + data.length + CRC_SIZE;
        byte[] packedData = new byte[0];

        packedData = ArrayUtils.addAll(packedData, getByteArrayFromShort((short) length));              // length
        packedData = ArrayUtils.addAll(packedData, getByteArrayFromShort((short) command.getId()));     // command
        packedData = ArrayUtils.addAll(packedData, getData());                                          // data
        packedData = ArrayUtils.addAll(packedData, getByteArrayFromShort(updateCRC()));                 // CRC16

        return packedData;
    }

    /**
     * @return CRC sum of Command+Data byte array
     */
    public short updateCRC() {
        CRC = calculateCRC16(data);
        return CRC;
    }

    /**
     * @param buffer byte array buffer
     * @return CRC16 sum
     */
    private short calculateCRC16(byte[] buffer) {
        short crc_value = 0;

        for (byte value : buffer) {

            for (int i = 0x80; i != 0; i >>= 1) {

                if ((crc_value & 0x8000) != 0) {
                    crc_value = (short) ((crc_value << 1) ^ CRC_POLYNOMIAL);
                } else {
                    crc_value = (short) (crc_value << 1);
                }

                if ((value & i) != 0) {
                    crc_value ^= CRC_POLYNOMIAL;
                }
            }
        }
        return crc_value;
    }

    public void unpack(byte[] sentData) throws InvalidCRCException, InvalidPacketSize {

        // Get first 2 bytes - frameLength (in bytes)
        short frameLength = getShortFromByteArray(ArrayUtils.subarray(sentData, 0, FRAME_SIZE));

        if ((FRAME_SIZE + frameLength) != sentData.length)
            throw new InvalidPacketSize();

        // Parsing packet
        short CRC = getShortFromByteArray(ArrayUtils.subarray(sentData, sentData.length - CRC_SIZE, sentData.length));
        short command = getShortFromByteArray(ArrayUtils.subarray(sentData, FRAME_SIZE, FRAME_SIZE + COMMAND_SIZE));
        byte[] data = ArrayUtils.subarray(sentData, FRAME_SIZE + COMMAND_SIZE, sentData.length - CRC_SIZE);

        // Check CRC
        if (CRC != calculateCRC16(data)) {
            throw new InvalidCRCException();
        }

        // All checks DONE
        setCRC(CRC);
        setCommand(Command.getCommand(command));
        setData(data);
    }

    private short getShortFromByteArray(byte[] array) {
        return ByteBuffer.wrap(array).getShort();
    }

    private int getIntFromByteArray(byte[] array) {
        return ByteBuffer.wrap(array).getInt();
    }

    private byte[] getByteArrayFromShort(short value) {
        return ByteBuffer.allocate(Short.SIZE / Byte.SIZE).putShort(value).array();
    }

    private byte[] getByteArrayFromInt(int value) {
        return ByteBuffer.allocate(Integer.SIZE / Byte.SIZE).putInt(value).array();
    }
}
