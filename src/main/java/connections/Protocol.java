package connections;

import exception.InvalidProtocol;

/**
 * Protocol interface
 */
public interface Protocol {

    byte[] wrap(byte[] data);

    byte[] unwrap(byte[] data) throws InvalidProtocol;

    byte[] getOpenSequence();

    byte[] getCloseSequence();
}
