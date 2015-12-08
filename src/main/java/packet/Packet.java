package packet;

import exception.InvalidCRCException;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Packet class.
 *
 * Packet structure:
 *  CRC - const 4 byte length
 *  Command - const 2 byte length
 *  Data - various length
 */
public class Packet {

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
        this.data    = null;
        this.CRC     = 0;
    }

    public Packet(Command command, byte[] data) {
        this.command = command;
        this.data    = data;
        this.CRC     = 0;
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
     * <p> CRC16 (2 byte) </p>
     * <p> Command (2 byte) </p>
     * <p> Data (N byte(s)) </p>
     * @return packed packet array
     */
    public byte[] pack() {

        byte[] packedData;

        // Pack command and data into byte array
        packedData = concatCommandAndData(getByteArrayFromShort((short) command.ordinal()), getData());

        // Calculate CRC packetData byte array
        CRC = updateCRC();

        // Then pack CRC to packedData
        packedData = ArrayUtils.addAll(getByteArrayFromShort(CRC), packedData);

        return packedData;
    }

    private byte[] concatCommandAndData(byte[] command, byte[] data) {

        byte[] result = new byte[0];

        result = ArrayUtils.addAll(result, command);
        result = ArrayUtils.addAll(result, data);

        return result;
    }

    private byte[] getByteArrayFromShort(short value) {
        return ByteBuffer.allocate(Short.SIZE / Byte.SIZE).putShort(value).array();
    }

    /**
     * @return CRC sum of Command+Data byte array
     */
    public short updateCRC() {

        byte[] buffer = concatCommandAndData(getByteArrayFromShort((short) command.ordinal()), data);
        CRC = calculateCRC16(buffer);

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

    public void unpack(byte[] sentData) throws InvalidCRCException {

        // Parse input sentData array into 3 byte arrays: CRC, command and data
        byte[] CRC = ArrayUtils.subarray(sentData, 0, 2);
        byte[] cmd = ArrayUtils.subarray(sentData, 2, 4);
        byte[] data = ArrayUtils.subarray(sentData, 4, sentData.length);

        // Check CRC
        short sentDataCRC = getShortFromByteArray(CRC);
        if (sentDataCRC != calculateCRC16(concatCommandAndData(cmd, data)))
            throw new InvalidCRCException();

        setCRC(sentDataCRC);
        setCommand(Command.values()[getShortFromByteArray(cmd)]);
        setData(data);
    }

    private short getShortFromByteArray(byte[] array) {
        return ByteBuffer.wrap(array).getShort();
    }
}
