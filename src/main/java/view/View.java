package view;

import controller.Controller;

import javax.swing.*;

/**
 * Base view class for GUI
 */
public class View extends JFrame {

    private Controller controller;

    public View(Controller controller) {
        this.controller = controller;
    }
}
