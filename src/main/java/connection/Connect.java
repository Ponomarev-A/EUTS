package connection;

/**
 * Interface for connect realization
 */
public interface Connect {

    boolean init();

    byte[] read() throws Exception;

    void write(byte[] data) throws Exception;
}
