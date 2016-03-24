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
        create();
    }

    private void create() {

        setLayout(new GridLayout(4, 2));

        setBorder(new TitledBorder(
                View.TITLE_BORDER,
                "Stand info",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                View.TITLE_FONT
        ));

        add(new JLabel("Firmware:"));
        add(jlFirmware);
        add(new JLabel("Scheme:"));
        add(jlScheme);
        add(new JLabel("Stand ID:"));
        add(jlStandID);
        add(new JLabel("Status:"));
        add(jlStatus);
    }

    void updateInfo() {

        boolean isConnected = controller.isStandConnected();

        if (isConnected)
            controller.getStand().readInfo();

        jlFirmware.setText(isConnected ? controller.getStand().getFirmware() : "-");
        jlScheme.setText(isConnected ? controller.getStand().getScheme() : "-");
        jlStandID.setText(isConnected ? controller.getStand().getID().toString() : "-");

        jlStatus.setText(isConnected ? "Connected" : "Disconnected");
        jlStatus.setForeground(isConnected ? View.DARK_GREEN : View.RED);
    }
}