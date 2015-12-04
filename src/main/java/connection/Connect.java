package connection;

/**
 * Interface for connect realization
 */
public interface Connect {

    boolean init();

    byte[] read();

    void write(byte[] data);
}
