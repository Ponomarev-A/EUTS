package connection;

import exception.FailedProtocolException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Class Protocol used for wrapping transmitted data by control symbols.
 */
public class Protocol {
    public static final byte[] CLOSE_CODE_SEQ = new byte[]{0x0D, 0x0A};
    public static final byte[] OPEN_CODE_SEQ = new byte[]{0x3A};

    public static byte[] wrap(byte[] data) {

        byte[] result = new byte[0];

        result = ArrayUtils.addAll(result, OPEN_CODE_SEQ);
        result = ArrayUtils.addAll(result, byteArrayToASCIICodeArray(data));
        result = ArrayUtils.addAll(result, CLOSE_CODE_SEQ);

        return result;
    }

    protected static byte[] byteArrayToASCIICodeArray(byte[] data) {
        return String.valueOf(Hex.encodeHex(data)).getBytes();
    }

    public static byte[] unwrap(byte[] code) throws FailedProtocolException {

        if (code[0] != OPEN_CODE_SEQ[0] ||
                code[code.length - 1] != CLOSE_CODE_SEQ[1] || code[code.length - 2] != CLOSE_CODE_SEQ[0])
            throw new FailedProtocolException();

        byte[] subarray = ArrayUtils.subarray(code, Protocol.OPEN_CODE_SEQ.length, code.length - Protocol.CLOSE_CODE_SEQ.length);
        return ASCIICodeArrayToByteArray(subarray);
    }

    protected static byte[] ASCIICodeArrayToByteArray(byte[] code) {

        byte[] bytes = new byte[code.length / 2];
        for (int i = 0; i < code.length; i += 2) {
            byte b1 = (byte) Character.digit(code[i], 16);
            byte b2 = (byte) Character.digit(code[i + 1], 16);
            bytes[i / 2] |= b1 << 4;
            bytes[i / 2] |= b2;
        }

        return bytes;
    }
}
