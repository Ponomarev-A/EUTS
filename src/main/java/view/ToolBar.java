package view;

import controller.Controller;

import javax.swing.*;
import java.awt.*;

/**
 * Toolbar class with action shortcuts
 */
class ToolBar extends JToolBar {
    private final Controller controller;

    ToolBar(Controller controller) {
        this.controller = controller;
//        create();
    }

    private void create() {
        setFloatable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));

        panel.add(new Button("Connect"));
        panel.add(new Button("Testing"));

        add(panel);
    }
}
