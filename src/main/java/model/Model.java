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
import java.util.Collections;
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
            controller.updateLog("Create " + connectionManager);

            receiver = new Receiver(connectionManager, controller);
            stand = new Stand(connectionManager, controller);
            testManager = new TestManager(controller, receiver, stand);
        } else {
            controller.updateLog(connectionManager + " don't created.");
        }
    }

    private Connection createConnection(String portName) {
        Connection newConnection = UART.getInstance(portName);

        // Before create new connection we need close old connection
        if (connectionManager != null && !connectionManager.getConnection().equals(newConnection)) {
            try {
                connectionManager.getConnection().close();
            } catch (Exception e) {
                controller.showErrorMessage(
                        "Close connection",
                        "Can't close connection " + connectionManager.getConnection(),
                        e);
            }
        }
        controller.updateLog("Create new connection " + newConnection);

        return newConnection;
    }

    public void destroyConnectionManager() {
        if (connectionManager != null) {
            controller.updateLog("Destroy " + connectionManager);

            disconnectFromDevice();
            connectionManager = null;
            receiver = null;
            stand = null;
        }
    }

    public void disconnectFromDevice() {
        try {
            controller.updateLog("Close connection " + connectionManager.getConnection() + " is " +
                    (connectionManager.getConnection().close() ? "success" : "failed"));
        } catch (Exception e) {
            controller.showErrorMessage(
                    "Close connection",
                    "Can't close connection " + connectionManager.getConnection(),
                    e);
        }
        receiver.checkConnectionStatus();
        stand.checkConnectionStatus();
    }

    public void connectToDevice() {
        try {
            controller.updateLog("Open connection " + connectionManager.getConnection() + " is " +
                    (connectionManager.getConnection().open() ? "success" : "failed"));
        } catch (Exception e) {
            controller.showErrorMessage(
                    "Open connection",
                    "Can't open connection " + connectionManager.getConnection(),
                    e);
        }
        receiver.checkConnectionStatus();
        stand.checkConnectionStatus();
    }

    public boolean isCOMPortSelected(String portName) {
        return connectionManager != null && ((UART) connectionManager.getConnection()).getSerialPort().getPortName().equals(portName);
    }

    public boolean isTestRunning() {
        return testRunning;
    }

    public void startTesting() {
        if (testRunning)
            return;

        controller.updateLog("\n===\tSTART TESTING\t===", LogPanel.BOLD);

        testRunning = true;
        testManager.startTests();
        stopTesting();
    }

    public void stopTesting() {
        if (!testRunning)
            return;

        controller.updateLog("\n===\tSTOP TESTING\t===", LogPanel.BOLD);

        testRunning = false;
    }

    public List<BaseTestCase> getTestsList() {
        return isReceiverConnected() && isStandConnected() ?
                testManager.getTestsList() :
                Collections.<BaseTestCase>emptyList();
    }

    public boolean isReceiverConnected() {
        return receiver != null && receiver.getConnectionStatus() == ConnectionStatus.CONNECTED;
    }

    public boolean isStandConnected() {
        return stand != null && stand.getConnectionStatus() == ConnectionStatus.CONNECTED;
    }
}
