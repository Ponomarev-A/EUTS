package controller;

import model.ConnectionStatus;
import model.Model;
import view.View;

/**
 * Base controller class for user events handling, model managing and viewing.
 */
public class Controller implements EventListener {
    private View view;
    private Model model;

    public Controller() {

        model = new Model();
        model.setEventListener(this);

        view = new View(this);
        view.init();
        view.setEventListener(this);
    }


    @Override
    public String[] getAvailableCOMPorts() {
        return model.getAvailableCOMPorts();
    }

    @Override
    public void connect() {
        model.connectToDevice();
        view.updateMenuStates();
    }

    @Override
    public void disconnect() {
        model.disconnectFromDevice();
        view.updateMenuStates();
    }

    @Override
    public void createConnection(String portName) {
        model.createUARTConnection(portName);
        model.createConnectionManager();

        view.updateMenuStates();
    }

    @Override
    public boolean isCOMPortSelected(String portName) {
        return model.isCOMPortSelected(portName);
    }

    @Override
    public ConnectionStatus getConnectionStatus() {
        return model.getConnectionStatus();
    }
}
