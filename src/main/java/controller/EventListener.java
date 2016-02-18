package controller;

import model.ConnectionStatus;

/**
 * EventListener interface contains user defined actions
 */
public interface EventListener {

    String[] getAvailableCOMPorts();

    void connect();

    void disconnect();

    void createConnection(String portName);

    boolean isCOMPortSelected(String portName);

    ConnectionStatus getConnectionStatus();
}
