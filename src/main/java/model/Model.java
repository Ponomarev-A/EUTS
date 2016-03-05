package model;

import connections.*;
import controller.Controller;
import view.LogPanel;

import java.util.Arrays;
import java.util.List;

/**
 * Base model class for data processing
 */
public class Model {

    private static final String DEFAULT_PORTNAME = "COM1";

    private final Protocol protocol = new ModBus();
    private Connection connection;
    private ConnectionManager connectionManager;

    private Controller controller;

    private Receiver receiver;
    private Stand stand;

    public Model(Controller controller) {
        this.controller = controller;
    }

    public Receiver getReceiver() {
        return receiver;
    }

    public Stand getStand() {
        return stand;
    }

    public void init() {
        createDefaultConnection(DEFAULT_PORTNAME);
        createConnectionManager();
    }

    private void createDefaultConnection(String defaultPortname) {
        List<String> portList = Arrays.asList(getAvailableCOMPorts());
        if (portList.contains(defaultPortname)) {
            createConnection(defaultPortname);
        }
    }

    public void createConnectionManager() {
        if (connection != null && connectionManager == null) {
            connectionManager = new ConnectionManager(connection, protocol);
            controller.updateLog("Create " + connectionManager, LogPanel.NORMAL);

            receiver = new Receiver(connectionManager, controller);
            stand = new Stand(connectionManager, controller);
        } else {
            controller.updateLog(connectionManager + " don't created.", LogPanel.NORMAL);
        }
    }

    public String[] getAvailableCOMPorts() {
        return UART.getPortNames();
    }

    public void createConnection(String portName) {
        Connection newConnection = UART.getInstance(portName);

        // Before create new connection we need close old connection
        if (connection != null && !connection.equals(newConnection)) {
            try {
                connection.close();
            } catch (Exception e) {
                controller.showErrorMessage("Create new connection", e);
            }
        }
        connection = newConnection;
        controller.updateLog("Create new connection " + connection, LogPanel.NORMAL);
    }

    public void destroyConnectionManager() {
        if (connectionManager != null) {
            controller.updateLog("Destroy " + connectionManager, LogPanel.NORMAL);

            disconnectFromDevice();
            connectionManager = null;
            receiver = null;
            stand = null;
        }
    }

    public void disconnectFromDevice() {
        if (connectionManager != null) {
            try {
                controller.updateLog("Close connection " + connection + " is " +
                        (connectionManager.getConnection().close() ? "success" : "failed"), LogPanel.NORMAL);

                receiver.checkConnectionStatus();
                stand.checkConnectionStatus();
            } catch (Exception e) {
                controller.showErrorMessage("Close connection", e);
            }
        }
    }

    public boolean isCOMPortSelected(String portName) {
        return connection instanceof UART && ((UART) connection).getSerialPort().getPortName().equals(portName);
    }

    public void connectToDevice() {
        if (connectionManager != null) {
            try {
                controller.updateLog("Open connection " + connection + " is " +
                        (connectionManager.getConnection().open() ? "success" : "failed"), LogPanel.NORMAL);

                receiver.checkConnectionStatus();
                stand.checkConnectionStatus();
            } catch (Exception e) {
                controller.showErrorMessage("Open connection", e);
            }
        }
    }

    public boolean isReceiverConnected() {
        return receiver != null && receiver.getConnectionStatus() == ConnectionStatus.CONNECTED;
    }

    public boolean isStandConnected() {
        return stand != null && stand.getConnectionStatus() == ConnectionStatus.CONNECTED;
    }

    public boolean isConnectionManagerExist() {
        return connectionManager != null;
    }
}
