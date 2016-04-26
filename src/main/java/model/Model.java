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
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;


/**
 * Base model class for data processing
 */
public class Model {

    private final Controller controller;

    private ConnectionManager CM;
    private Receiver receiver;
    private Stand stand;
    private TestManager testManager;
    private ManagerDB managerDB;

    public Model(Controller controller) {
        this.controller = controller;
        this.managerDB = new ManagerDB(controller);
        this.testManager = new TestManager(controller);
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
        managerDB.connect();
    }

    public List<String> getAvailableCOMPorts() {
        return Arrays.asList(UART.getPortNames());
    }

    public void deinit() {
        disconnectFromDevice();
        managerDB.disconnect();
    }

    public void disconnectFromDevice() {
        if (CM == null)
            return;

        Connection connection = CM.getConnection();
        try {
            // Trying close connection
            connection.close();
            controller.updateLog("Connection " + connection + " successfully CLOSED");

            receiver.checkConnectionStatus();
            stand.checkConnectionStatus();
        } catch (Exception e) {
            controller.showErrorMessage(
                    "Close connection",
                    "Can't close connection " + connection,
                    e);
        } finally {
            CM = null;
            receiver = null;
            stand = null;
        }
    }

    public void connectToDevice(String port) {
        Connection connection = UART.getInstance(port);

        try {
            if (CM != null && CM.getConnection().isOpened()) {
                disconnectFromDevice();
            }

            // Trying open connection
            connection.open();
            controller.updateLog("Connection " + connection + " successfully OPENED");

            CM = new ConnectionManager(connection, new ModBus());
            receiver = new Receiver(controller, CM);
            stand = new Stand(controller, CM);

            receiver.checkConnectionStatus();
            stand.checkConnectionStatus();

        } catch (Exception e) {
            controller.showErrorMessage(
                    "Open connection",
                    "Can't open connection " + connection,
                    e);

            disconnectFromDevice();
        }
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

    public ResultSet selectTestSessions(Receiver receiver, String afterDate, String beforeDate) throws SQLException {
        return managerDB.select(receiver, afterDate, beforeDate);
    }

    public ResultSet selectCalibrationCoeffs(Receiver receiver) throws SQLException {
        return managerDB.select(receiver);
    }

    public String[] getReceiverModelsFromDB() throws SQLException {
        return managerDB.getModels();
    }

    public String[] getReceiverSchemesFromDB() throws SQLException {
        return managerDB.getSchemes();
    }

    public String[] getReceiverFirmwaresFromDB() throws SQLException {
        return managerDB.getFirmwares();
    }

    public boolean insertResultToDB() {
        try {
            Integer newID = managerDB.getNextUniqueID();

            if (managerDB.insert(new Receiver(newID, receiver.getModel(), receiver.getScheme(), receiver.getFirmware()), null, null) > 0 &&
                    managerDB.insert(newID, testManager.getTestIDs(State.PASS), testManager.getTestIDs(State.FAIL), testManager.getTestIDs(State.SKIP)) > 0) {


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

    public int updateCalibrationCoeffsInDB(Float[] depthCoeffs, Float[] currentCoeffs) {
        try {
            return managerDB.update(receiver, depthCoeffs, currentCoeffs);
        } catch (SQLException e) {
            controller.showErrorMessage(
                    "Database update entry",
                    String.format("New entry:\nDepth coefficients %s\nCurrent coefficients %s\nfor %s update database FAILED!",
                            Arrays.toString(depthCoeffs), Arrays.toString(currentCoeffs), receiver
                    ),
                    e
            );
        }
        return 0;
    }

    public void checkDeviceInDB() {

        Integer receiverID = receiver.getID();

        // If receiver has ID = 0, then it's new
        if (receiverID == 0)
            return;

        try {
            List<String> listIDs = Arrays.asList(getReceiverIDsFromDB());

            if (listIDs.contains(String.valueOf(receiverID))) {
                controller.askUserShowStoredInDBReceiver(receiver);
            }
        } catch (SQLException e) {
            controller.showErrorMessage(
                    "Check receiver in database",
                    "Can't get information about existed receivers in database",
                    e);
        }

    }

    public String[] getReceiverIDsFromDB() throws SQLException {
        return managerDB.getIDs();
    }
}
