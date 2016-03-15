package view;

import controller.Controller;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MenuBar implements ActionListener {

    private final JMenu jmPorts = new JMenu("Choose COM-port");
    private final JMenuItem jmiConnect = new JMenuItem("Connect");
    private final JMenuItem jmiDisconnect = new JMenuItem("Disconnect");
    private final JMenuItem jmiStartTesting = new JMenuItem("Start");
    private final JMenuItem jmiStopTesting = new JMenuItem("Stop");
    private Controller controller;
    private JMenu connectionMenu;
    private JMenu testingMenu;


    public MenuBar(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String command = e.getActionCommand();
        switch (command) {
            case "Connect":
                controller.connect();
                break;
            case "Disconnect":
                controller.disconnect();
                break;

            case "Start":
                controller.startTesting();
                break;

            case "Stop":
                controller.stopTesting();
                break;

            // Clicking on COM-port
            default:
                // Check port name correctness
                String portName = command.split(" ")[0].trim();
                if (portName.matches("^COM\\d{1,2}$")) {
                    controller.destroyConnectionManager();
                    controller.createConnectionManager(portName);
                }
                break;
        }
    }

    JMenuBar createMainMenu() {
        JMenuBar menuBar = new JMenuBar();

        connectionMenu = createConnectionMenu();
        testingMenu = createTestingMenu();

        menuBar.add(connectionMenu);
        menuBar.add(testingMenu);

        return menuBar;
    }

    private JMenu createConnectionMenu() {

        JMenu menu = new JMenu("Connection");

        menu.add(jmPorts);
        menu.addSeparator();
        menu.add(jmiConnect);
        menu.add(jmiDisconnect);

        menu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                updateCOMPortList(jmPorts);
            }
        });
        jmiConnect.addActionListener(this);
        jmiDisconnect.addActionListener(this);

        return menu;
    }


    private JMenu createTestingMenu() {
        JMenu menu = new JMenu("Testing");

        menu.add(jmiStartTesting);
        menu.add(jmiStopTesting);

        jmiStartTesting.addActionListener(this);
        jmiStopTesting.addActionListener(this);

        return menu;
    }

    private void updateCOMPortList(JMenu menu) {
        String[] ports = controller.getCOMPortList();

        menu.removeAll();

        if (ports.length == 0) {
            JMenuItem jmiNonePorts = new JMenuItem("None");
            jmiNonePorts.setEnabled(false);
            menu.add(jmiNonePorts);

            controller.destroyConnectionManager();
        }

        for (String port : ports) {
            boolean isCOM1PortExist = port.equals("COM1");

            JCheckBoxMenuItem jmiPort = new JCheckBoxMenuItem(isCOM1PortExist ? port + " (default)" : port);
            jmiPort.addActionListener(MenuBar.this);
            jmiPort.setSelected(controller.isCOMPortSelected(port));

            menu.add(jmiPort);
        }
    }

    public void updateMenuStates() {

        if (!controller.isConnectionManagerExist()) {
            jmiConnect.setEnabled(false);
            jmiDisconnect.setEnabled(false);
            jmPorts.setEnabled(true);

            jmiStartTesting.setEnabled(false);
            jmiStopTesting.setEnabled(false);
            return;
        }

        boolean isConnected = controller.isStandConnected() & controller.isReceiverConnected();

        jmiConnect.setEnabled(!isConnected);
        jmiDisconnect.setEnabled(isConnected);
        jmPorts.setEnabled(!isConnected);


        boolean isTestRunning = controller.isTestRunning();

        jmiStartTesting.setEnabled(isConnected && !isTestRunning);
        jmiStopTesting.setEnabled(isConnected && isTestRunning);
    }
}