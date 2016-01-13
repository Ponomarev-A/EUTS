package connections;

/**
 * Interface for connect realization
 */
public interface Connection {

    boolean open();

    byte[] read() throws Exception;

    void write(byte[] data) throws Exception;

    boolean close();
}
