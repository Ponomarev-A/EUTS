package view;

import controller.Controller;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

class ReceiverInfoPanel extends JPanel {

    private Controller controller;

    private JLabel jlModel = new JLabel();
    private JLabel jlFirmware = new JLabel();
    private JLabel jlScheme = new JLabel();
    private JLabel jlStatus = new JLabel();
    private JLabel jlReceiverID = new JLabel();

    ReceiverInfoPanel(Controller controller) {
        this.controller = controller;
        create();
    }

    private void create() {

        setLayout(new GridLayout(5, 2));

        setBorder(new TitledBorder(
                View.TITLE_BORDER,
                "Receiver info",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                View.TITLE_FONT
        ));

        add(new JLabel("Model:"));
        add(jlModel);
        add(new JLabel("Firmware:"));
        add(jlFirmware);
        add(new JLabel("Scheme:"));
        add(jlScheme);
        add(new JLabel("Receiver ID:"));
        add(jlReceiverID);
        add(new JLabel("Status:"));
        add(jlStatus);
    }

    void updateInfo() {

        boolean isConnected = controller.isReceiverConnected();

        if (isConnected)
            controller.getReceiver().readInfo();

        jlModel.setText(isConnected ? controller.getReceiver().getModel() : "-");
        jlFirmware.setText(isConnected ? controller.getReceiver().getFirmware() : "-");
        jlScheme.setText(isConnected ? controller.getReceiver().getScheme() : "-");
        jlReceiverID.setText(isConnected ? controller.getReceiver().getID() : "-");

        jlStatus.setText(isConnected ? "Connected" : "Disconnected");
        jlStatus.setForeground(isConnected ? Color.GREEN : Color.RED);
    }
}