package connections;

import exception.FailedProtocolException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Class ModBus used for wrapping transmitted data by control symbols.
 */
public class ModBus implements Protocol {
//    public static final byte[] CLOSE_CODE_SEQ = new byte[]{0x0D, 0x0A}; ]

    // TODO: Don't forgive revert CLOSE_CODE_SEQ values!
    public static final byte[] CLOSE_CODE_SEQ = new byte[]{0x2E, 0x2F};
    public static final byte[] OPEN_CODE_SEQ = new byte[]{0x3A};


    @Override
    public byte[] wrap(byte[] data) {

        byte[] result = new byte[0];

        result = ArrayUtils.addAll(result, getOpenSequence());
        result = ArrayUtils.addAll(result, byteArrayToASCIICodeArray(data));
        result = ArrayUtils.addAll(result, getCloseSequence());

        return result;
    }

    @Override
    public byte[] unwrap(byte[] code) throws FailedProtocolException {

        if (code[0] != getOpenSequence()[0] ||
                code[code.length - 1] != getCloseSequence()[1] || code[code.length - 2] != getCloseSequence()[0])
            throw new FailedProtocolException();

        byte[] subarray = ArrayUtils.subarray(code, getOpenSequence().length, code.length - getCloseSequence().length);
        return ASCIICodeArrayToByteArray(subarray);
    }

    @Override
    public byte[] getOpenSequence() {
        return OPEN_CODE_SEQ;
    }

    @Override
    public byte[] getCloseSequence() {
        return CLOSE_CODE_SEQ;
    }

    private static byte[] ASCIICodeArrayToByteArray(byte[] code) {

        if (code.length % 2 != 0)
            return null;

        byte[] bytes = new byte[code.length / 2];
        for (int i = 0; i < code.length; i += 2) {
            byte b1 = (byte) Character.digit(code[i], 16);
            byte b2 = (byte) Character.digit(code[i + 1], 16);
            bytes[i / 2] |= b1 << 4;
            bytes[i / 2] |= b2;
        }

        return bytes;
    }

    private static byte[] byteArrayToASCIICodeArray(byte[] data) {
        return String.valueOf(Hex.encodeHex(data)).getBytes();
    }
}
