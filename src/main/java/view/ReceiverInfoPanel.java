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

    public ReceiverInfoPanel(Controller controller) {
        this.controller = controller;
    }

    JPanel create(int width, int height) {

        JPanel jPanel = new JPanel(new GridLayout(5, 2));
        jPanel.setMaximumSize(new Dimension(width, height));
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

    public void updateInfo() {

        if (controller.isReceiverConnected()) {

            String[] info = controller.getReceiverInfo().split(" ");
            if (info.length == 4) {
                jlModel.setText(info[0]);
                jlFirmware.setText(info[1]);
                jlScheme.setText(info[2]);
                jlReceiverID.setText(info[3]);
            }

            jlStatus.setText("Connected");
            jlStatus.setForeground(Color.GREEN);
        } else {
            jlModel.setText("-");
            jlFirmware.setText("-");
            jlScheme.setText("-");
            jlReceiverID.setText("-");

            jlStatus.setText("Disconnected");
            jlStatus.setForeground(Color.RED);
        }
    }
}