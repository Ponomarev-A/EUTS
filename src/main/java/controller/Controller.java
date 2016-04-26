package controller;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import model.Model;
import model.Receiver;
import model.Stand;
import model.tests.TestManager;
import view.LogPanel;
import view.View;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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
                model = new Model(Controller.this);
                view = new View(Controller.this);

                view.init();
                view.updateDeviceInfo();
                view.updateTestControls();

                model.init();

                view.updateToolBarStates();
            }
        });
    }

    @Override
    public List<String> getCOMPortList() {
        return model.getAvailableCOMPorts();
    }

    @Override
    public void connect(final String port) {

        connectionExecutor.submit(new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                updateLog("\nConnect...", LogPanel.BOLD);
                model.connectToDevice(port);
                return null;
            }

            @Override
            protected void done() {
                view.updateToolBarStates();
                view.updateDeviceInfo();
                view.updateTestControls();
                view.fillTestList();
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
                view.updateToolBarStates();
                view.updateDeviceInfo();
                view.updateTestControls();
                view.clearTestList();
            }
        });
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
    public boolean isConnected() {
        return isStandConnected() && isReceiverConnected();
    }

    @Override
    public Receiver getReceiver() {
        return model.getReceiver();
    }

    @Override
    public Stand getStand() {
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
    public void startTesting() {
        if (model.isTestRunning())
            return;

        model.startTesting();

        view.updateToolBarStates();
        view.updateTestControls();
    }

    @Override
    public void stopTesting() {
        if (!model.isTestRunning())
            return;

        model.stopTesting();

        view.updateToolBarStates();
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
    public void openHistory() {
        view.openHistoryWindow();
    }

    @Override
    public boolean isTestRunning() {
        return model.isTestRunning();
    }

    @Override
    public String askPathToDatabase() {
        return view.askPathToDatabase();
    }

    @Override
    public void changeDatabasePath() {
        model.getManagerDB().disconnect();
        model.getManagerDB().connect(null);

        view.updateToolBarStates();
    }

    @Override
    public ResultSet selectTestSessions(Receiver receiver, String afterDate, String beforeDate) throws SQLException {
        return model.selectTestSessions(receiver, afterDate, beforeDate);
    }

    @Override
    public ResultSet selectCalibrationCoeffs(Receiver receiver) throws SQLException {
        return model.selectCalibrationCoeffs(receiver);
    }

    @Override
    public boolean isDBExist() {
        return model.getManagerDB().exist();
    }

    @Override
    public String[] getReceiverModelsFromDB() throws SQLException {
        return model.getReceiverModelsFromDB();
    }

    @Override
    public String[] getReceiverSchemesFromDB() throws SQLException {
        return model.getReceiverSchemesFromDB();
    }

    @Override
    public String[] getReceiverFirmwaresFromDB() throws SQLException {
        return model.getReceiverFirmwaresFromDB();
    }

    @Override
    public String[] getReceiverIDsFromDB() throws SQLException {
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
    public int updateCalibrationCoeffsInDB(Float[] depthCoeffs, Float[] currentCoeffs) {
        return model.updateCalibrationCoeffsInDB(depthCoeffs, currentCoeffs);
    }

    @Override
    public TestManager getTestManager() {
        return model.getTestManager();
    }

    @Override
    public void checkDeviceInDB() {
        model.checkDeviceInDB();
    }

    @Override
    public void askUserShowStoredInDBReceiver(Receiver receiver) {
        view.askUserShowStoredInDBReceiver(receiver);
    }

    @Override
    public boolean askUserWriteCalibrCoeffsToReceiver() {
        return view.askUserWriteCalibrCoeffsToReceiver();
    }

    public void windowClosing() {
        connectionExecutor.shutdown();
        model.deinit();
    }
}
