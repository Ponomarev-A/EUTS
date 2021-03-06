package packet;

import exception.InvalidCRC;
import exception.InvalidPacketSize;
import org.apache.commons.lang.ArrayUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Packet class.
 * <p>
 * <p> Packet structure:
 * <p>  | SOF | LENGTH | COMMAND |     DATA     | CRC16 | EOF |
 * <p>
 * <p>  1) SOF (const value with const size 1 byte) - start of frame (0x3a)
 * <p>
 * <p>  2) Length (var value with const size 2 bytes) - length of command (2 bytes), data array (N bytes) and CRC16 (2 bytes)
 * LENGTH = COMMAND + DATA + CRC16
 * <p>
 * <p>  3) Command (var value with const size 2 bytes) - one of enum
 * <p>
 * <p>  4) Data (var value with var size N bytes) - data array (may be missing, i.e. have 0 size)
 * <p>
 * <p>  5) CRC16 (var value with const size 2 bytes) - CRC of data array
 * <p>
 * <p>  6) EOF (const value with const size 2 bytes) - end of frame (0x0d, 0x0a)
 */
public class Packet {

    public static final short DATA_COUNT_LENGTH = 2;
    private static final short CRC_POLYNOMIAL = (short) 0x8005;
    // Length of packet parts (bytes)
    private static final short COMMAND_LENGTH = 2;
    private static final short CRC16_LENGTH = 2;
    public static final short MIN_FRAME_LENGTH = COMMAND_LENGTH + CRC16_LENGTH;
    private static final short DATA_LENGTH = 512;
    public static final short MAX_FRAME_LENGTH = COMMAND_LENGTH + DATA_LENGTH + CRC16_LENGTH;

    private Command command;
    private byte[] data;
    private short CRC;

    public Packet(Packet packet) {
        this.command = packet.command;
        this.data = packet.data;
        this.CRC = packet.CRC;
    }

    public Packet() {
        this.command = Command.NO_COMMAND;
        this.data = new byte[0];
        this.CRC = 0;
    }

    public Packet(Command command) {
        this.command = command;
        this.data = new byte[0];
        this.CRC = 0;
    }

    public Packet(Command command, byte[] data) {
        this.command = command;
        this.data = data;
        this.CRC = 0;
    }

    public Packet(Command command, short data) {
        this.command = command;
        this.data = toByteArray(data);
        this.CRC = 0;
    }

    private byte[] toByteArray(short value) {
        return ByteBuffer.allocate(Short.SIZE / Byte.SIZE).putShort(value).array();
    }

    public Packet(Command command, float[] data) {
        this.command = command;
        this.data = toByteArray(data);
        this.CRC = 0;
    }

    private byte[] toByteArray(float[] data) {
        byte byteArray[] = new byte[data.length * Float.SIZE / Byte.SIZE];
        ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(data);
        return byteArray;
    }

    public Packet(Command command, int data) {
        this.command = command;
        this.data = toByteArray(data);
        this.CRC = 0;
    }

    private byte[] toByteArray(int value) {
        return ByteBuffer.allocate(Integer.SIZE / Byte.SIZE).putInt(value).array();
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

    public void setCommand(Command command) {
        this.command = command;
    }

    public short getCRC() {
        return CRC;
    }

    private void setCRC(short CRC) {
        this.CRC = CRC;
    }

    public byte[] getData() {
        return data;
    }

    void setData(byte[] data) {
        this.data = data;
    }

    public void setData(int data) {
        this.data = toByteArray(data);
    }

    public short[] getDataAsShortArray() {
        short[] result = new short[data.length / 2];
        try {
            ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(result);
        } catch (Exception ignored) {
        }

        return result;
    }

    public float[] getDataAsFloatArray() {
        float[] result = new float[data.length / 4];
        try {
            ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(result);
        } catch (Exception ignored) {
        }

        return result;
    }

    public byte getDataAsByte() {
        return data[0];
    }

    public int getDataAsInt() {
        return getIntFromByteArray(data);
    }

    private int getIntFromByteArray(byte[] array) {
        int result = 0;

        try {
            result = ByteBuffer.wrap(array).getInt();
        } catch (Exception ignored) {
        }

        return result;
    }

    public String getDataAsString() {
        return new String(data, StandardCharsets.US_ASCII);
    }

    /**
     * Packed packet format:
     * <p> Command (2 byte) </p>
     * <p> Data (N byte(s)) </p>
     * <p> CRC16 (2 byte) </p>
     *
     * @return packed packet array
     */
    public byte[] pack() throws InvalidPacketSize {

        int frameLength = COMMAND_LENGTH + data.length + CRC16_LENGTH;
        if (frameLength < MIN_FRAME_LENGTH || frameLength > MAX_FRAME_LENGTH)
            throw new InvalidPacketSize("Length of frame isn't in the range (" + MIN_FRAME_LENGTH + "," + MAX_FRAME_LENGTH + ")");

        byte[] packedData = new byte[0];

        packedData = ArrayUtils.addAll(packedData, toByteArray((short) frameLength));         // length
        packedData = ArrayUtils.addAll(packedData, toByteArray((short) command.getId()));     // command
        packedData = ArrayUtils.addAll(packedData, getData());                                          // data
        packedData = ArrayUtils.addAll(packedData, toByteArray(updateCRC()));                 // CRC16

        return packedData;
    }

    /**
     * @return CRC sum of Command+Data byte array
     */
    short updateCRC() {
        CRC = calculateCRC16(data);
        return CRC;
    }

    /**
     * Calculate CRC16 code.
     * <p> Check CRC calculating: http://depa.usst.edu.cn/chenjq/www2/software/crc/CRC_Javascript/CRCcalculation.htm
     * <p>
     * <p> CRC16 params:
     * <p>  - Polynom           (hex)   0x8005
     * <p>  - Initial value     (hex)   0
     * <p>  - Final XOR value   (hex)   0
     *
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

    public void unpack(byte[] sentData) throws InvalidCRC, InvalidPacketSize {

        // Get first 2 bytes - frameLength (in bytes)
        short frameLength = getShortFromByteArray(ArrayUtils.subarray(sentData, 0, DATA_COUNT_LENGTH));

        if (frameLength < MIN_FRAME_LENGTH || frameLength > MAX_FRAME_LENGTH)
            throw new InvalidPacketSize("Length of frame " + frameLength + " isn't in the range (" + MIN_FRAME_LENGTH + "," + MAX_FRAME_LENGTH + ")");

        // Parsing packet
        short CRC = getShortFromByteArray(ArrayUtils.subarray(sentData, sentData.length - CRC16_LENGTH, sentData.length));
        short command_id = getShortFromByteArray(ArrayUtils.subarray(sentData, DATA_COUNT_LENGTH, DATA_COUNT_LENGTH + COMMAND_LENGTH));
        byte[] data = ArrayUtils.subarray(sentData, DATA_COUNT_LENGTH + COMMAND_LENGTH, sentData.length - CRC16_LENGTH);

        // Check CRC
        short calcCRC = calculateCRC16(data);
        if (CRC != calcCRC)
            throw new InvalidCRC("Incorrect CRC: received CRC value - " + CRC + ", calculated CRC value - " + calcCRC);

        // All checks DONE
        setCRC(CRC);
        setCommand(Command.getCommand(command_id));
        setData(data);
    }

    private short getShortFromByteArray(byte[] array) {
        short result = 0;
        try {
            result = ByteBuffer.wrap(array).getShort();
        } catch (Exception ignored) {
        }

        return result;
    }
}
