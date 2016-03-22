package controller;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import model.Device;
import model.Model;
import model.tests.BaseTestCase;
import view.LogPanel;
import view.View;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Base controller class for user events handling, model managing and viewing.
 */
public class Controller implements EventListener {

    private static final ThreadFactory THREAD_FACTORY_CONTROLLER = new ThreadFactoryBuilder().setNameFormat("Controller-%d").setDaemon(true).build();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(THREAD_FACTORY_CONTROLLER);
    private View view;
    private Model model;

    private SwingWorker<Void, Void> testWorker;

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
            }
        });
    }

    @Override
    public String[] getCOMPortList() {
        return model.getAvailableCOMPorts();
    }

    @Override
    public void connect() {

        executor.submit(new SwingWorker<Void, Void>() {
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
                view.loadTestList();
                view.updateTestList();

                updateLog(model.getStand() + " is " + (isStandConnected() ? "connected" : "disconnected"));
                updateLog(model.getReceiver() + " is " + (isReceiverConnected() ? "connected" : "disconnected"));
            }
        });
    }

    @Override
    public void disconnect() {
        executor.submit(new SwingWorker<Void, Void>() {
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
                view.loadTestList();

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
    public void showErrorMessage(final String title, final String text, final Exception e) {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        view.showErrorMessage(title, text + "\n\nError: " + e.getLocalizedMessage() + "\nCause: " + e.getCause().getLocalizedMessage());
                    }
                });
            } catch (InterruptedException | InvocationTargetException error) {
                error.printStackTrace();
            }
        } else {
            view.showErrorMessage(title, text + "\n\nError: " + e.getLocalizedMessage() + "\nCause: " + e.getCause().getLocalizedMessage());
        }

        updateLog("\n\n" + title, LogPanel.BOLD);
        updateLog(text + "\nError: " + e.getLocalizedMessage() + "\nCause: " + e.getCause().getLocalizedMessage(), LogPanel.BOLD, LogPanel.RED);
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
        executor.submit(new SwingWorker<Void, Void>() {
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
        executor.submit(new SwingWorker<Void, Void>() {
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

        testWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                updateLog("\n===  START TESTING  ===", LogPanel.BOLD);

                publish();
                model.startTesting();
                return null;
            }

            @Override
            protected void process(List<Void> chunks) {
                view.updateMenuStates();
            }

            @Override
            protected void done() {
                stopTesting();
            }
        };
        testWorker.execute();
    }

    @Override
    public void stopTesting() {

        if (!model.isTestRunning())
            return;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (!testWorker.isDone()) {
                    testWorker.cancel(true);
                }

                model.stopTesting();
                return null;
            }

            @Override
            protected void done() {
                view.updateMenuStates();
                updateLog("===  STOP TESTING  ===", LogPanel.BOLD);
                updateLog(model.getTestManager().toString(), LogPanel.BOLD);
            }
        }.execute();
    }

    @Override
    public List<BaseTestCase> getTestsList() {
        return model.getTestsList();
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

    public boolean isTestRunning() {
        return model.isTestRunning();
    }

    public void windowClosing() {
        destroyConnectionManager();

        executor.shutdown();
        try {
            executor.awaitTermination(200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
