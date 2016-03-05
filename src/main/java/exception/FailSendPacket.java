package exception;

/**
 * Failed send packet exception
 */
public class FailSendPacket extends Exception {
    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public FailSendPacket(String message) {
        super(message);
    }
}
