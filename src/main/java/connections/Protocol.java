package connections;

import exception.FailedProtocolException;

/**
 * Protocol interface
 */
public interface Protocol {

    byte[] wrap(byte[] data);

    byte[] unwrap(byte[] data) throws FailedProtocolException;

    byte[] getOpenSequence();

    byte[] getCloseSequence();
}
