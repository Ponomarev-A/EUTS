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

    public StandInfoPanel(Controller controller) {
        this.controller = controller;
    }

    JPanel create(int width, int height) {

        JPanel jPanel = new JPanel(new GridLayout(3, 1));
        jPanel.setMaximumSize(new Dimension(width, height));
        jPanel.setBackground(Color.LIGHT_GRAY);

        jPanel.setBorder(new TitledBorder(
                View.TITLE_BORDER,
                "Stand info",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                View.DEFAULT_FONT
        ));

        jPanel.add(new JLabel("Firmware:"));    jPanel.add(jlFirmware);
        jPanel.add(new JLabel("Scheme:"));      jPanel.add(jlScheme);
        jPanel.add(new JLabel("Status:"));      jPanel.add(jlStatus);

        return jPanel;
    }

    public void updateInfo() {

        if (controller.isStandConnected()) {

            String[] fields = controller.getStandInfo().split(" ");
            if (fields.length == 2) {
                jlFirmware.setText(fields[0]);
                jlScheme.setText(fields[1]);
            }
            jlStatus.setText("Connected");
            jlStatus.setForeground(Color.GREEN);

        } else {
            jlFirmware.setText("-");
            jlScheme.setText("-");

            jlStatus.setText("Disconnected");
            jlStatus.setForeground(Color.RED);
        }

    }
}