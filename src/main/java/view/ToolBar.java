package view;

import controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

/**
 * Toolbar class with action shortcuts
 */
class ToolBar extends JPanel implements ActionListener {
    private static final String PREF_COM_PORT = "COM_PORT";

    private static final Insets NO_MARGIN = new Insets(0, 0, 0, 0);
    private static final int ICON_WIDTH = 30;
    private static final int ICON_HEIGHT = 30;
    private static final ImageIcon ICON_CONNECT = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("icons/connect.png"))
            .getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_DEFAULT));
    private static final ImageIcon ICON_DISCONNECT = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("icons/disconnect.png"))
            .getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_DEFAULT));
    private static final ImageIcon ICON_START = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("icons/test_start.png"))
            .getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_DEFAULT));
    private static final ImageIcon ICON_STOP = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("icons/test_stop.png"))
            .getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_DEFAULT));
    private static final ImageIcon ICON_HISTORY = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("icons/database.png"))
            .getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_DEFAULT));
    private static final ImageIcon ICON_REFRESH = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("icons/refresh.png"))
            .getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_DEFAULT));
    private static final ImageIcon ICON_LOCATION = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("icons/open.png"))
            .getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_DEFAULT));

    private final Controller controller;

    private JComboBox<String> jcbPorts;
    private JButton jbRefresh;
    private JButton jbReconnect;
    private JButton jbStart;
    private JButton jbStop;
    private JButton jbHistory;
    private JButton jbLocation;

    private String currentCOMPort;

    ToolBar(Controller controller) {
        this.controller = controller;
        currentCOMPort = loadCOMPortFromPref(null);
        create();
    }

    private String loadCOMPortFromPref(String defaultPort) {
        Preferences prefs = Preferences.userNodeForPackage(ToolBar.class);
        return prefs.get(PREF_COM_PORT, defaultPort);
    }

    private void create() {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        JToolBar bar = new JToolBar();
        bar.setBorderPainted(false);
        bar.setFloatable(false);

        jcbPorts = new JComboBox<>();
        jcbPorts.setEditable(false);

        jbRefresh = new JButton(ICON_REFRESH);
        jbReconnect = new JButton(ICON_CONNECT);
        jbStart = new JButton(ICON_START);
        jbStop = new JButton(ICON_STOP);
        jbHistory = new JButton(ICON_HISTORY);
        jbLocation = new JButton(ICON_LOCATION);

        jbRefresh.setMargin(NO_MARGIN);
        jbReconnect.setMargin(NO_MARGIN);
        jbStart.setMargin(NO_MARGIN);
        jbStop.setMargin(NO_MARGIN);
        jbHistory.setMargin(NO_MARGIN);
        jbLocation.setMargin(NO_MARGIN);

        jbRefresh.addActionListener(this);
        jbReconnect.addActionListener(this);
        jbStart.addActionListener(this);
        jbStop.addActionListener(this);
        jbHistory.addActionListener(this);
        jbLocation.addActionListener(this);

        bar.add(new JLabel("Port: "));
        bar.add(jcbPorts);
        bar.add(jbRefresh);
        bar.add(jbReconnect);
        bar.addSeparator();
        bar.add(jbStart);
        bar.add(jbStop);
        bar.addSeparator();
        bar.add(jbHistory);
        bar.add(jbLocation);

        add(bar);
    }

    void updateToolBarStates() {
        refreshCOMPortList();

        boolean isConnected = controller.isConnected();
        boolean isTestRunning = controller.isTestRunning();

        jcbPorts.setEnabled(!isConnected);
        jbRefresh.setEnabled(!isConnected);
        jbReconnect.setEnabled(!isTestRunning);
        jbReconnect.setIcon(isConnected ? ICON_DISCONNECT : ICON_CONNECT);

        jbStart.setEnabled(isConnected && !isTestRunning);
        jbStop.setEnabled(isConnected && isTestRunning);

        jbHistory.setEnabled(controller.isDBExist());
    }

    private void refreshCOMPortList() {
        jcbPorts.removeAllItems();

        java.util.List<String> ports = controller.getCOMPortList();

        if (ports.size() == 0) {
            jcbPorts.addItem("None");
            jcbPorts.setEnabled(false);
        } else {

            for (String port : ports) {
                jcbPorts.addItem(port);
            }
            jcbPorts.setEnabled(true);
        }

        jcbPorts.setSelectedItem(currentCOMPort);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source instanceof JButton) {
            JButton btn = (JButton) source;

            if (btn.equals(jbRefresh)) {
                refreshCOMPortList();

            } else {
                if (btn.equals(jbReconnect)) {
                    currentCOMPort = getSelectedPort();

                    if (controller.isConnected()) {
                        controller.disconnect();
                    } else {
                        controller.connect(currentCOMPort);
                    }

                } else if (btn.equals(jbStart)) {
                    controller.startTesting();

                } else if (btn.equals(jbStop)) {
                    controller.stopTesting();

                } else if (btn.equals(jbHistory)) {
                    controller.openHistory();
                } else if (btn.equals(jbLocation)) {
                    controller.askPathToDatabase();
                }
            }
        }
    }

    private String getSelectedPort() {
        String port = (String) jcbPorts.getSelectedItem();
        if (port != null && port.matches("^COM\\d{1,2}$")) {
            saveCOMPortToPref(port);
        }
        return port;
    }

    private void saveCOMPortToPref(String port) {
        Preferences prefs = Preferences.userNodeForPackage(ToolBar.class);
        prefs.put(PREF_COM_PORT, port);
    }
}
