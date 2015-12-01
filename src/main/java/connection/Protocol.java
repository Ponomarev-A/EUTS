package connection;

import exception.FailedProtocolException;
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
        result = ArrayUtils.addAll(result, data);
        result = ArrayUtils.addAll(result, CLOSE_CODE_SEQ);

        return result;
    }

    public static byte[] unwrap(byte[] data) throws FailedProtocolException {

        if (data[0] != OPEN_CODE_SEQ[0] ||
            data[data.length - 1] != CLOSE_CODE_SEQ[1] || data[data.length - 2] != CLOSE_CODE_SEQ[0])
            throw new FailedProtocolException();

        return ArrayUtils.subarray(data, Protocol.OPEN_CODE_SEQ.length, data.length - Protocol.CLOSE_CODE_SEQ.length);
    }
}
