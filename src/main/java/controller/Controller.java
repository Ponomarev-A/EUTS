package controller;

import model.Model;
import view.View;

import javax.swing.*;

/**
 * Base controller class for user events handling, model managing and viewing.
 */
public class Controller implements EventListener {
    private View view;
    private Model model;

    public Controller() {

        model = new Model(this);
        model.init();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                view = new View(Controller.this);
                view.init();
            }
        });
    }


    @Override
    public String[] getCOMPortList() {
        return model.getAvailableCOMPorts();
    }

    @Override
    public void connect() {
        model.connectToDevice();

        view.updateMenuStates();
        view.updateDeviceInfo();
    }

    @Override
    public void disconnect() {
        model.disconnectFromDevice();

        view.updateMenuStates();
        view.updateDeviceInfo();
    }


    @Override
    public void createConnection(String portName) {
        model.createConnection(portName);
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
    public String getStandInfo() {
        return model.getStandInfo();
    }

    @Override
    public String getReceiverInfo() {
        return model.getReceiverInfo();
    }

    @Override
    public void showErrorMessage(String title, Exception e) {
        view.showErrorMessage(title, e);
    }

    @Override
    public void createConnectionManager() {
        model.createConnectionManager();

        view.updateMenuStates();
    }

    @Override
    public void destroyConnectionManager() {
        model.destroyConnectionManager();

        view.updateMenuStates();
        view.updateDeviceInfo();
    }

    @Override
    public boolean isConnectionManagerExist() {
        return model.isConnectionManagerExist();
    }
}
