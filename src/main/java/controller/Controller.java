package controller;

import model.Device;
import model.Model;
import model.tests.BaseTestCase;
import view.LogPanel;
import view.View;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Base controller class for user events handling, model managing and viewing.
 */
public class Controller implements EventListener {

    private View view;
    private Model model;

    public Controller() {

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    view = new View(Controller.this);
                    view.init();
                }
            });
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }

        model = new Model(this);
        model.init();

        view.updateMenuStates();
        view.updateDeviceInfo();
    }


    @Override
    public String[] getCOMPortList() {
        return model.getAvailableCOMPorts();
    }

    @Override
    public void connect() {
        updateLog("\nConnect...", LogPanel.BOLD);
        model.connectToDevice();

        view.updateMenuStates();
        view.updateDeviceInfo();
        view.updateTestList();

        updateLog(model.getStand() + " is " + (isStandConnected() ? "connected" : "disconnected"), LogPanel.NORMAL);
        updateLog(model.getReceiver() + " is " + (isReceiverConnected() ? "connected" : "disconnected"), LogPanel.NORMAL);
    }

    @Override
    public void disconnect() {
        updateLog("\nDisconnect...", LogPanel.BOLD);
        model.disconnectFromDevice();

        view.updateMenuStates();
        view.updateDeviceInfo();
        view.updateTestList();

        updateLog(model.getStand() + " is " + (isStandConnected() ? "connected" : "disconnected"), LogPanel.NORMAL);
        updateLog(model.getReceiver() + " is " + (isReceiverConnected() ? "connected" : "disconnected"), LogPanel.NORMAL);
    }


    @Override
    public boolean isCOMPortSelected(String portName) {
        return model.isCOMPortSelected(portName);
    }

    @Override
    public boolean isReceiverConnected() {
        return model.isReceiverConnected();
    }

    @Override
    public boolean isStandConnected() {
        return model.isStandConnected();
    }

    @Override
    public Device getReceiver() {
        return model.getReceiver();
    }

    @Override
    public Device getStand() {
        return model.getStand();
    }

    @Override
    public void showErrorMessage(String title, Exception e) {
        view.showErrorMessage(title, e);
        updateLog(e.getLocalizedMessage(), LogPanel.NORMAL);
    }

    @Override
    public void updateLog(String text, SimpleAttributeSet attributeSet) {
        view.updateLog(text, attributeSet);
    }

    @Override
    public void createConnectionManager(String portName) {
        updateLog("\nCreate connection manager...", LogPanel.BOLD);
        model.createConnectionManager(portName);

        view.updateMenuStates();
    }

    @Override
    public void destroyConnectionManager() {
        updateLog("\nDestroy connection manager...", LogPanel.BOLD);
        model.stopTesting();
        model.destroyConnectionManager();

        view.updateMenuStates();
        view.updateDeviceInfo();
    }

    @Override
    public boolean isConnectionManagerExist() {
        return model.getConnectionManager() != null;
    }

    @Override
    public void startTesting() {
        updateLog("\n   START TESTING", LogPanel.BOLD);
        model.startTesting();

        view.updateMenuStates();
    }

    @Override
    public void stopTesting() {
        updateLog("\n   STOP TESTING", LogPanel.BOLD);
        model.stopTesting();

        view.updateMenuStates();
    }

    @Override
    public List<BaseTestCase> getTestsList() {
        return model.getTestsList();
    }

    public boolean isTestRunning() {
        return model.isTestRunning();
    }
}
