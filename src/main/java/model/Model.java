package model;

import connections.Connection;
import connections.ConnectionManager;
import connections.ModBus;
import connections.UART;
import controller.Controller;
import model.tests.BaseTestCase;
import model.tests.TestManager;
import view.LogPanel;

import java.util.Arrays;
import java.util.List;

/**
 * Base model class for data processing
 */
public class Model {

    public static final String DEFAULT_PORTNAME = "COM1";

    private Controller controller;

    private ConnectionManager connectionManager;

    private Receiver receiver;
    private Stand stand;
    private TestManager testManager;
    private boolean testRunning = false;

    public Model(Controller controller) {
        this.controller = controller;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public Receiver getReceiver() {
        return receiver;
    }

    public Stand getStand() {
        return stand;
    }

    public void init() {
        createConnectionManagerIfExistDefaultPortName();
    }

    private void createConnectionManagerIfExistDefaultPortName() {
        List<String> portList = Arrays.asList(getAvailableCOMPorts());
        if (portList.contains(DEFAULT_PORTNAME)) {
            createConnectionManager(DEFAULT_PORTNAME);
        }
    }

    public String[] getAvailableCOMPorts() {
        return UART.getPortNames();
    }

    public void createConnectionManager(String portName) {
        Connection connection = createConnection(portName);

        if (connection != null && connectionManager == null) {
            connectionManager = new ConnectionManager(connection, new ModBus());
            controller.updateLog("Create " + connectionManager, LogPanel.NORMAL);

            receiver = new Receiver(connectionManager, controller);
            stand = new Stand(connectionManager, controller);
            testManager = new TestManager(receiver, stand);
        } else {
            controller.updateLog(connectionManager + " don't created.", LogPanel.NORMAL);
        }
    }

    private Connection createConnection(String portName) {
        Connection newConnection = UART.getInstance(portName);

        // Before create new connection we need close old connection
        if (connectionManager != null && !connectionManager.getConnection().equals(newConnection)) {
            try {
                connectionManager.getConnection().close();
            } catch (Exception e) {
                controller.showErrorMessage("Close connection " + connectionManager.getConnection(), e);
            }
        }
        controller.updateLog("Create new connection " + newConnection, LogPanel.NORMAL);

        return newConnection;
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
        try {
            controller.updateLog("Close connection " + connectionManager.getConnection() + " is " +
                    (connectionManager.getConnection().close() ? "success" : "failed"), LogPanel.NORMAL);

            receiver.checkConnectionStatus();
            stand.checkConnectionStatus();
        } catch (Exception e) {
            controller.showErrorMessage("Close connection", e);
        }
    }

    public void connectToDevice() {
        try {
            controller.updateLog("Open connection " + connectionManager.getConnection() + " is " +
                    (connectionManager.getConnection().open() ? "success" : "failed"), LogPanel.NORMAL);

            receiver.checkConnectionStatus();
            stand.checkConnectionStatus();
        } catch (Exception e) {
            controller.showErrorMessage("Open connection", e);
        }
    }

    public boolean isCOMPortSelected(String portName) {
        Connection connection = connectionManager.getConnection();
        return connection instanceof UART && ((UART) connection).getSerialPort().getPortName().equals(portName);
    }

    public boolean isReceiverConnected() {
        return receiver != null && receiver.getConnectionStatus() == ConnectionStatus.CONNECTED;
    }

    public boolean isStandConnected() {
        return stand != null && stand.getConnectionStatus() == ConnectionStatus.CONNECTED;
    }

    public boolean isTestRunning() {
        return testRunning;
    }

    public void startTesting() {
        testRunning = true;

        try {
            testManager.startTests();
        } catch (Exception e) {
            controller.showErrorMessage("Test failed!", e);
        }
    }

    public void stopTesting() {
        testRunning = false;

    }

    public List<BaseTestCase> getTestsList() {
        return testManager.getTestsList();
    }
}
