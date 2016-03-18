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
    }

    JPanel create() {

        JPanel jPanel = new JPanel(new GridLayout(5, 2));
        jPanel.setBackground(Color.LIGHT_GRAY);


        jPanel.setBorder(new TitledBorder(
                View.TITLE_BORDER,
                "Receiver info",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                View.TITLE_FONT
        ));

        jPanel.add(new JLabel("Model:"));       jPanel.add(jlModel);
        jPanel.add(new JLabel("Firmware:"));    jPanel.add(jlFirmware);
        jPanel.add(new JLabel("Scheme:"));      jPanel.add(jlScheme);
        jPanel.add(new JLabel("Receiver ID:")); jPanel.add(jlReceiverID);
        jPanel.add(new JLabel("Status:"));      jPanel.add(jlStatus);

        return jPanel;
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