package controller;

/**
 * EventListener interface contains user defined actions
 */
public interface EventListener {

    String[] getCOMPortList();

    void connect();

    void disconnect();

    void createConnection(String portName);

    boolean isCOMPortSelected(String portName);

    boolean isReceiverConnected();

    boolean isStandConnected();

    String getStandInfo();

    String getReceiverInfo();

    void showErrorMessage(String title, Exception e);

    void createConnectionManager();

    void destroyConnectionManager();

    boolean isConnectionManagerExist();
}
