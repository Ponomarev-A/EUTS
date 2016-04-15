package model;

import connections.Connection;
import connections.ConnectionManager;
import connections.ModBus;
import connections.UART;
import controller.Controller;
import model.tests.TestManager;
import model.tests.TestManager.State;
import packet.Command;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;


/**
 * Base model class for data processing
 */
public class Model {

    private final Controller controller;

    private ConnectionManager connectionManager;
    private Receiver receiver;
    private Stand stand;
    private TestManager testManager;
    private ManagerDB managerDB;

    public Model(Controller controller) {
        this.controller = controller;
        this.managerDB = new ManagerDB(controller);
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

    public TestManager getTestManager() {
        return testManager;
    }

    public ManagerDB getManagerDB() {
        return managerDB;
    }

    public void init() {
        createConnectionManagerWithFirstCOMPort();
        managerDB.connect();
    }

    private void createConnectionManagerWithFirstCOMPort() {
        List<String> portList = Arrays.asList(getAvailableCOMPorts());
        if (portList.size() > 0) {
            createConnectionManager(portList.get(0));
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

    public void deinit() {
        destroyConnectionManager();
        managerDB.disconnect();
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
        return testManager != null && testManager.isTestRunning();
    }

    public void startTesting() {
        testManager.startTests();
    }

    public void stopTesting() {
        testManager.stopTesting();
    }

    public boolean isReceiverConnected() {
        return receiver != null && receiver.getConnectionStatus() == ConnectionStatus.CONNECTED;
    }

    public boolean isStandConnected() {
        return stand != null && stand.getConnectionStatus() == ConnectionStatus.CONNECTED;
    }

    public ResultSet selectFromHistoryDB(Receiver receiver, String afterDate, String beforeDate) {
        return managerDB.select(receiver, afterDate, beforeDate);
    }

    public String[] getReceiverModelsFromDB() {
        return managerDB.getModels();
    }

    public String[] getReceiverSchemesFromDB() {
        return managerDB.getSchemes();
    }

    public String[] getReceiverFirmwaresFromDB() {
        return managerDB.getFirmwares();
    }

    public String[] getReceiverIDsFromDB() {
        return managerDB.getIDs();
    }

    public boolean insertResultToDB() {

        Integer newID = managerDB.getNextUniqueID();

        try {
            if (managerDB.insert(new Receiver(newID, receiver.getModel(), receiver.getScheme(), receiver.getFirmware())) &&
                    managerDB.insert(newID, testManager.getTestIDs(State.PASS), testManager.getTestIDs(State.FAIL), testManager.getTestIDs(State.SKIP))) {


                receiver.set(Command.WRITE_PCB_ID_DEVICE, newID);
                receiver.setID(newID);

                controller.showMessage(
                        "Database insert entry",
                        "New entry " + receiver + "\nsuccessfully inserted to database!"
                );
                return true;
            }
        } catch (Exception e) {
            controller.showErrorMessage(
                    "Database insert entry",
                    "New entry " + receiver + "\ninsert to database FAILED!",
                    e
            );
        }
        return false;
    }
}
