package controller;

import model.Device;
import model.tests.BaseTestCase;

import javax.swing.text.AttributeSet;
import java.util.List;

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

    List<BaseTestCase> getTestsList();

    void updateTestList();

    void history();

    boolean isTestRunning();

    String getPathToDatabase();
}
