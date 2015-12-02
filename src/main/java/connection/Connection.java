package connection;

/**
 * Abstract connection class
 */
public abstract class Connection {

    public Connection() {}

    public abstract boolean init();
    public abstract byte[] read();
    public abstract void write(byte[] data);
}
