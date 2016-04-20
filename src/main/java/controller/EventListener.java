package controller;

import model.Device;
import model.Receiver;
import model.tests.TestManager;

import javax.swing.text.AttributeSet;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * EventListener interface contains user defined actions
 */
interface EventListener {

    String[] getCOMPortList();

    void connect();

    void disconnect();

    boolean isCOMPortSelected(String portName);

    boolean isReceiverConnected();

    boolean isStandConnected();

    boolean isConnected();

    Device getReceiver();

    Device getStand();

    void showMessage(String title, String text);

    void showErrorMessage(String title, String text, Exception e);

    void updateLog(String text, AttributeSet... attributeSet);

    void updateLog(String text, AttributeSet attributeSet);

    void updateLog(String text);

    void createConnectionManager(String portName);

    void destroyConnectionManager();

    boolean isConnectionManagerExist();

    void startTesting();

    void stopTesting();

    void updateTestList();

    void history();

    boolean isTestRunning();

    String getPathToDatabase();

    ResultSet selectTestSessions(Receiver receiver, String afterDate, String beforeDate) throws SQLException;

    ResultSet selectCalibrationCoeffs(Receiver receiver) throws SQLException;

    boolean isDBExist();

    String[] getReceiverModelsFromDB() throws SQLException;

    String[] getReceiverSchemesFromDB() throws SQLException;

    String[] getReceiverFirmwaresFromDB() throws SQLException;

    String[] getReceiverIDsFromDB() throws SQLException;

    boolean insertResultToDB();

    int updateCalibrationCoeffsInDB(Float[] depthCoeffs, Float[] currentCoeffs);

    TestManager getTestManager();
}
