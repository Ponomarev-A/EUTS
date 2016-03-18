package view;

import controller.Controller;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

class StandInfoPanel extends JPanel {

    private Controller controller;

    private JLabel jlFirmware = new JLabel();
    private JLabel jlScheme = new JLabel();
    private JLabel jlStatus = new JLabel();
    private JLabel jlStandID = new JLabel();

    StandInfoPanel(Controller controller) {
        this.controller = controller;
    }

    JPanel create() {

        JPanel jPanel = new JPanel(new GridLayout(4, 1));
        jPanel.setBackground(Color.LIGHT_GRAY);

        jPanel.setBorder(new TitledBorder(
                View.TITLE_BORDER,
                "Stand info",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                View.TITLE_FONT
        ));

        jPanel.add(new JLabel("Firmware:"));    jPanel.add(jlFirmware);
        jPanel.add(new JLabel("Scheme:"));      jPanel.add(jlScheme);
        jPanel.add(new JLabel("Stand ID:"));
        jPanel.add(jlStandID);
        jPanel.add(new JLabel("Status:"));      jPanel.add(jlStatus);

        return jPanel;
    }

    void updateInfo() {

        boolean isConnected = controller.isStandConnected();

        if (isConnected)
            controller.getStand().readInfo();

        jlFirmware.setText(isConnected ? controller.getStand().getFirmware() : "-");
        jlScheme.setText(isConnected ? controller.getStand().getScheme() : "-");
        jlStandID.setText(isConnected ? controller.getStand().getID() : "-");

        jlStatus.setText(isConnected ? "Connected" : "Disconnected");
        jlStatus.setForeground(isConnected ? Color.GREEN : Color.RED);
    }
}