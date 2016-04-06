package connections;

import exception.InvalidProtocol;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * Class ModBus used for wrapping transmitted data by control symbols.
 */
public class ModBus implements Protocol {

    // TODO: Don't forgive revert CLOSE_CODE_SEQ values!
//    public static final byte[] CLOSE_CODE_SEQ = new byte[]{0x0D, 0x0A}; ]
    static final byte[] CLOSE_CODE_SEQ = new byte[]{0x2E, 0x2F};
    static final byte[] OPEN_CODE_SEQ = new byte[]{0x3A};

    @Override
    public String toString() {
        return "ModBus{}";
    }

    @Override
    public byte[] wrap(byte[] data) {

        byte[] result = new byte[0];

        result = ArrayUtils.addAll(result, OPEN_CODE_SEQ);
        result = ArrayUtils.addAll(result, byteArrayToASCIICodeArray(data));
        result = ArrayUtils.addAll(result, CLOSE_CODE_SEQ);

        return result;
    }

    @Override
    public byte[] unwrap(byte[] code) throws InvalidProtocol {

        byte[] SOF = new byte[]{code[0]};
        byte[] EOF = new byte[]{code[code.length - 2], code[code.length - 1]};

        if (!Arrays.equals(SOF, OPEN_CODE_SEQ) || !Arrays.equals(EOF, CLOSE_CODE_SEQ))
            throw new InvalidProtocol("The ModBus protocol is broken: " +
                    "expected " + Arrays.toString(OPEN_CODE_SEQ) + ", " + Arrays.toString(CLOSE_CODE_SEQ) + ", " +
                    "actual " + Arrays.toString(SOF) + ", " + Arrays.toString(EOF));

        byte[] subarray = ArrayUtils.subarray(code, OPEN_CODE_SEQ.length, code.length - CLOSE_CODE_SEQ.length);
        return ASCIICodeArrayToByteArray(subarray);
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
