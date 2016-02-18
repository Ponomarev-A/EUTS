package model;

import connections.*;
import controller.EventListener;

/**
 * Base model class for data processing
 */
public class Model {

    private final Protocol protocol = new ModBus();
    private EventListener eventListener;
    private ConnectionManager connectionManager;
    private Connection connection;
    private Receiver receiver;


    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public String[] getAvailableCOMPorts() {
        return UART.getPortNames();
    }

    public void createConnectionManager() {
        if (connectionManager == null && connection != null) {
            connectionManager = new ConnectionManager(connection, protocol);
            receiver = new Receiver(connectionManager);
        }

        if (connectionManager != null) {
            connectionManager.close();
        }
    }

    public void connectToDevice() {
        if (connectionManager != null)
            connectionManager.open();
    }

    public void disconnectFromDevice() {
        if (connectionManager != null)
            connectionManager.close();
    }

    public void createUARTConnection(String portName) {
        if (connection != null) {
            connection.close();
        }
        connection = new UART(portName);
    }

    public boolean isCOMPortSelected(String portName) {
        return connection instanceof UART && ((UART) connection).getSerialPort().getPortName().equals(portName);
    }

    public ConnectionStatus getConnectionStatus() {
        return receiver.getConnectionStatus();
    }
}
