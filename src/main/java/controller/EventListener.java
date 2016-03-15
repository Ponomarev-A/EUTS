package controller;

import model.Device;
import model.tests.BaseTestCase;

import javax.swing.text.SimpleAttributeSet;
import java.util.List;

/**
 * EventListener interface contains user defined actions
 */
public interface EventListener {

    String[] getCOMPortList();

    void connect();

    void disconnect();

    boolean isCOMPortSelected(String portName);

    boolean isReceiverConnected();

    boolean isStandConnected();

    Device getReceiver();

    Device getStand();

    void showErrorMessage(String title, Exception e);

    void updateLog(String text, SimpleAttributeSet attributeSet);

    void createConnectionManager(String portName);

    void destroyConnectionManager();

    boolean isConnectionManagerExist();

    void startTesting();

    void stopTesting();

    List<BaseTestCase> getTestsList();
}
