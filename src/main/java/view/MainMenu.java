package view;

import controller.Controller;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;

public class MainMenu implements Serializable, ActionListener {
    private final JMenu jMenu = new JMenu("Connection");
    private final JMenu jmPorts = new JMenu("Choose COM-port");
    private final JMenuItem jmiConnect = new JMenuItem("Connect");
    private final JMenuItem jmiDisconnect = new JMenuItem("Disconnect");
    private View view;
    private Controller controller;

    public MainMenu(View view, Controller controller) {
        this.view = view;
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
            // Clicking on COM-port
            default:
                controller.createConnection(command);
                break;
        }
    }

    void createMainMenu() {
        JMenuBar jmbMainMenu = new JMenuBar();

        jmbMainMenu.add(createConnectionMenu());


        view.setJMenuBar(jmbMainMenu);
    }


    private JMenu createConnectionMenu() {

        jMenu.add(jmPorts);
        jMenu.addSeparator();
        jMenu.add(jmiConnect);
        jMenu.add(jmiDisconnect);

        // Add listener to refresh COM port list menu item
        jmPorts.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                readAvailableCOMPorts(jmPorts);
            }
        });

        jmiConnect.addActionListener(this);
        jmiDisconnect.addActionListener(this);

        jmiConnect.setEnabled(false);
        jmiDisconnect.setEnabled(false);

        return jMenu;
    }

    private void readAvailableCOMPorts(JMenu menu) {
        String[] ports = controller.getAvailableCOMPorts();

        // Clear list
        menu.removeAll();

        if (ports.length == 0) {
            JMenuItem jmiNonePorts = new JMenuItem("None");
            jmiNonePorts.setEnabled(false);
            menu.add(jmiNonePorts);
        }

        for (String port : ports) {
            JCheckBoxMenuItem jmiPort = new JCheckBoxMenuItem(port);
            jmiPort.addActionListener(MainMenu.this);
            menu.add(jmiPort);

            jmiPort.setSelected(controller.isCOMPortSelected(port));
        }
    }

    public void updateMenuStates() {
        switch (controller.getConnectionStatus()) {
            case CONNECTED:
                jmiConnect.setEnabled(false);
                jmiDisconnect.setEnabled(true);
                break;

            case DISCONNECTED:
                jmiConnect.setEnabled(true);
                jmiDisconnect.setEnabled(false);
                break;

            case ERROR:
                jmiConnect.setEnabled(false);
                jmiDisconnect.setEnabled(false);
                break;
        }

    }
}