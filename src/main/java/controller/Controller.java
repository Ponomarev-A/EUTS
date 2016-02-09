package controller;

import model.Model;
import view.View;

/**
 * Base controller class for user events handling, model managing and viewing.
 */
public class Controller {
    private View view;
    private Model model;

    public Controller() {

        view = new View(this);
        model = new Model();

    }
}
