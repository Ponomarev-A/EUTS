package controller;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import model.Device;
import model.Model;
import model.Receiver;
import model.tests.TestManager;
import view.LogPanel;
import view.View;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Base controller class for user events handling, model managing and viewing.
 */
public class Controller implements EventListener {

    private static final ThreadFactory CONNECTION_EXECUTOR = new ThreadFactoryBuilder().setNameFormat("Controller-ConnectionExecutor-%d").setDaemon(true).build();
    private final ExecutorService connectionExecutor = Executors.newSingleThreadExecutor(CONNECTION_EXECUTOR);
    private View view;
    private Model model;

    public Controller() {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                view = new View(Controller.this);
                view.init();

                model = new Model(Controller.this);
                model.init();

                view.updateMenuStates();
                view.updateDeviceInfo();
                view.updateTestControls();
            }
        });
    }

    @Override
    public String[] getCOMPortList() {
        return model.getAvailableCOMPorts();
    }

    @Override
    public void connect() {

        connectionExecutor.submit(new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                updateLog("\nConnect...", LogPanel.BOLD);
                model.connectToDevice();
                return null;
            }

            @Override
            protected void done() {
                view.updateMenuStates();
                view.updateDeviceInfo();
                view.updateTestControls();
                view.fillTestList();

                updateLog(model.getStand() + " is " + (isStandConnected() ? "connected" : "disconnected"));
                updateLog(model.getReceiver() + " is " + (isReceiverConnected() ? "connected" : "disconnected"));
            }
        });
    }

    @Override
    public void disconnect() {
        connectionExecutor.submit(new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                updateLog("\nDisconnect...", LogPanel.BOLD);
                model.disconnectFromDevice();
                return null;
            }

            @Override
            protected void done() {
                view.updateMenuStates();
                view.updateDeviceInfo();
                view.updateTestControls();
                view.clearTestList();

                updateLog(model.getStand() + " is " + (isStandConnected() ? "connected" : "disconnected"));
                updateLog(model.getReceiver() + " is " + (isReceiverConnected() ? "connected" : "disconnected"));
            }
        });
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
    public void showMessage(final String title, final String text) {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        view.showMessage(title, text);
                    }
                });
            } catch (InterruptedException | InvocationTargetException error) {
                error.printStackTrace();
            }
        } else {
            view.showMessage(title, text);
        }

        updateLog("\n\n" + title, LogPanel.BOLD);
        updateLog(text);
    }

    @Override
    public void showErrorMessage(final String title, final String text, final Exception e) {

        final String causeMessage = e.getCause() != null ? "\nCause: " + e.getCause().getLocalizedMessage() : "";
        final String errorMessage = "\n\nError: " + e.getLocalizedMessage();

        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        view.showErrorMessage(title, text + errorMessage + causeMessage);
                    }
                });
            } catch (InterruptedException | InvocationTargetException error) {
                error.printStackTrace();
            }
        } else {
            view.showErrorMessage(title, text + errorMessage + causeMessage);
        }

        updateLog("\n\n" + title, LogPanel.BOLD);
        updateLog(text + errorMessage + causeMessage, LogPanel.BOLD, LogPanel.RED);
    }

    @Override
    public void updateLog(final String text, final AttributeSet... attributeSet) {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        view.updateLog(text, attributeSet);
                    }
                });
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            view.updateLog(text, attributeSet);
        }
    }

    @Override
    public void updateLog(String text, AttributeSet attributeSet) {
        updateLog(text, attributeSet, LogPanel.BLACK);
    }

    @Override
    public void updateLog(String text) {
        updateLog(text, LogPanel.NORMAL, LogPanel.BLACK);
    }

    @Override
    public void createConnectionManager(final String portName) {
        connectionExecutor.submit(new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                updateLog("\nCreate connection manager...", LogPanel.BOLD);
                model.createConnectionManager(portName);
                return null;
            }

            @Override
            protected void done() {
                view.updateMenuStates();
            }
        });
    }

    @Override
    public void destroyConnectionManager() {
        connectionExecutor.submit(new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                updateLog("\nDestroy connection manager...", LogPanel.BOLD);
                model.destroyConnectionManager();
                return null;
            }

            @Override
            protected void done() {
                view.updateMenuStates();
                view.updateDeviceInfo();
            }
        });
    }

    @Override
    public boolean isConnectionManagerExist() {
        return model.getConnectionManager() != null;
    }

    @Override
    public void startTesting() {
        if (model.isTestRunning())
            return;

        model.startTesting();

        view.updateMenuStates();
        view.updateTestControls();
    }

    @Override
    public void stopTesting() {

        if (!model.isTestRunning())
            return;

        model.stopTesting();

        view.updateMenuStates();
        view.updateTestControls();
    }


    @Override
    public void updateTestList() {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        view.updateTestList();
                    }
                });
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            view.updateTestList();
        }
    }

    @Override
    public void history() {
        view.openHistoryWindow();
    }

    @Override
    public boolean isTestRunning() {
        return model.isTestRunning();
    }

    @Override
    public String getPathToDatabase() {
        return view.getPathToDatabase();
    }

    @Override
    public ResultSet selectFromHistoryDB(Receiver receiver, String afterDate, String beforeDate) {
        return model.selectFromHistoryDB(receiver, afterDate, beforeDate);
    }

    @Override
    public boolean isDBExist() {
        return model.getManagerDB().exist();
    }

    @Override
    public String[] getReceiverModelsFromDB() {
        return model.getReceiverModelsFromDB();
    }

    @Override
    public String[] getReceiverSchemesFromDB() {
        return model.getReceiverSchemesFromDB();
    }

    @Override
    public String[] getReceiverFirmwaresFromDB() {
        return model.getReceiverFirmwaresFromDB();
    }

    @Override
    public String[] getReceiverIDsFromDB() {
        return model.getReceiverIDsFromDB();
    }

    @Override
    public boolean insertResultToDB() {
        if (view.askInsertResultToDB() && model.insertResultToDB()) {
            view.updateDeviceInfo();
            return true;
        }
        return false;
    }

    @Override
    public TestManager getTestManager() {
        return model.getTestManager();
    }

    public void windowClosing() {
        connectionExecutor.shutdown();
        try {
            connectionExecutor.awaitTermination(200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        model.deinit();
    }
}
