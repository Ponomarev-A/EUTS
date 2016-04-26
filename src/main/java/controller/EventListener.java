package controller;

import model.Receiver;
import model.Stand;
import model.tests.TestManager;

import javax.swing.text.AttributeSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * EventListener interface contains user defined actions
 */
interface EventListener {

    List<String> getCOMPortList();

    void connect(String port);

    void disconnect();

    boolean isReceiverConnected();

    boolean isStandConnected();

    boolean isConnected();

    Receiver getReceiver();

    Stand getStand();

    void showMessage(String title, String text);

    void showErrorMessage(String title, String text, Exception e);

    void updateLog(String text, AttributeSet... attributeSet);

    void updateLog(String text, AttributeSet attributeSet);

    void updateLog(String text);

    void startTesting();

    void stopTesting();

    void updateTestList();

    void openHistory();

    boolean isTestRunning();

    void askPathToDatabase();

    void changeDatabasePath(String path);

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

    void checkDeviceInDB();

    void askUserShowStoredInDBReceiver(Receiver receiver);

    boolean askUserWriteCalibrCoeffsToReceiver();
}
