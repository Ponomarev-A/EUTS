package view;

import controller.Controller;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Created by Ponomarev on 18.04.2016.
 */
class InfoPanel extends JPanel {
    private final Controller controller;

    private JLabel jlR_Model = new JLabel();
    private JLabel jlR_Firmware = new JLabel();
    private JLabel jlR_Scheme = new JLabel();
    private JLabel jlR_ID = new JLabel();
    private JLabel jlR_Status = new JLabel();

    private JLabel jlS_Model = new JLabel();
    private JLabel jlS_Firmware = new JLabel();
    private JLabel jlS_Scheme = new JLabel();
    private JLabel jlS_ID = new JLabel();
    private JLabel jlS_Status = new JLabel();

    InfoPanel(Controller controller) {
        this.controller = controller;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        setBorder(new TitledBorder(
                View.TITLE_BORDER,
                "Info",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                View.TITLE_FONT
        ));

        JPanel jpLabels = new JPanel();
        jpLabels.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        jpLabels.setLayout(new BoxLayout(jpLabels, BoxLayout.Y_AXIS));
        jpLabels.add(new JLabel("Device:"));
        jpLabels.add(new JLabel("Model:"));
        jpLabels.add(new JLabel("Firmware:"));
        jpLabels.add(new JLabel("Scheme:"));
        jpLabels.add(new JLabel("Device ID:"));
        jpLabels.add(new JLabel("Status:"));


        JPanel jpValues = new JPanel(new GridLayout(6, 2));

        jpValues.add(new JLabel("Receiver"));
        jpValues.add(new JLabel("Stand"));
        jpValues.add(jlR_Model);
        jpValues.add(jlS_Model);
        jpValues.add(jlR_Firmware);
        jpValues.add(jlS_Firmware);
        jpValues.add(jlR_Scheme);
        jpValues.add(jlS_Scheme);
        jpValues.add(jlR_ID);
        jpValues.add(jlS_ID);
        jpValues.add(jlR_Status);
        jpValues.add(jlS_Status);

        add(jpLabels);
        add(jpValues);

        revalidate();
    }

    void updateInfo() {
        updateReceiverInfo();
        updateStandInfo();
    }

    private void updateReceiverInfo() {
        boolean isConnected = controller.isReceiverConnected();

        if (isConnected)
            controller.getReceiver().readInfo();

        String model = controller.getReceiver().getModel();
        String firmware = controller.getReceiver().getFirmware();
        String scheme = controller.getReceiver().getScheme();
        String ID = controller.getReceiver().getID() != null ? controller.getReceiver().getID().toString() : "";

        jlR_Model.setText(isConnected ? model : "-");
        jlR_Firmware.setText(isConnected ? firmware : "-");
        jlR_Scheme.setText(isConnected ? scheme : "-");
        jlR_ID.setText(isConnected ? ID : "-");

        jlR_Status.setText(isConnected ? "Connected" : "Disconnected");
        jlR_Status.setForeground(isConnected ? View.DARK_GREEN : View.RED);
    }

    private void updateStandInfo() {
        boolean isConnected = controller.isStandConnected();

        if (isConnected)
            controller.getStand().readInfo();

        String model = controller.getStand().getModel();
        String firmware = controller.getStand().getFirmware();
        String scheme = controller.getStand().getScheme();
        String ID = controller.getStand().getID() != null ? controller.getStand().getID().toString() : "";

        jlS_Model.setText(isConnected ? model : "-");
        jlS_Firmware.setText(isConnected ? firmware : "-");
        jlS_Scheme.setText(isConnected ? scheme : "-");
        jlS_ID.setText(isConnected ? ID : "-");

        jlS_Status.setText(isConnected ? "Connected" : "Disconnected");
        jlS_Status.setForeground(isConnected ? View.DARK_GREEN : View.RED);
    }
}
