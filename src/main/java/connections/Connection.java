package connections;

/**
 * Interface for connect realization
 */
public interface Connection {

    boolean open() throws Exception;

    byte[] read() throws Exception;

    boolean write(byte[] data) throws Exception;

    boolean close() throws Exception;
}
