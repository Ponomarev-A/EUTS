package view;

import controller.Controller;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Base view class for GUI
 */
public class View extends JFrame {

    public static final Font DEFAULT_FONT = new Font(null, Font.PLAIN, 18);
    public static final Font TITLE_FONT = new Font(null, Font.BOLD, 20);

    public static final Border TITLE_BORDER = BorderFactory.createLineBorder(Color.BLACK, 3);

    private static final int FRAME_WIDTH = 1100;
    private static final int FRAME_HEIGHT = 800;

    private Controller controller;

    private MenuBar menuBar;
    private ReceiverInfoPanel jpReceiverInfo;
    private StandInfoPanel jpStandInfo;


    public View(Controller controller) {
        this.controller = controller;
    }

    public void init() {

        createMainFrame();
        createMainPanel();
        createMainMenu();

        updateDeviceInfo();
        updateMenuStates();

        setVisible(true);
    }

    private void createMainFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultLookAndFeelDecorated(true);
        setTitle("Electronic Units Test Stand (EUTS) application");

        // Set new default font to labels
        UIManager.put("Label.font", DEFAULT_FONT);
    }

    private void createMainPanel() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));

        jpReceiverInfo = new ReceiverInfoPanel(controller);
        jpStandInfo = new StandInfoPanel(controller);

        jPanel.add(jpReceiverInfo.create(FRAME_WIDTH * 2 / 3, FRAME_HEIGHT / 5));
        jPanel.add(jpStandInfo.create(FRAME_WIDTH * 1 / 3, FRAME_HEIGHT / 5));

        add(jPanel);
    }

    private void createMainMenu() {
        menuBar = new MenuBar(controller);
        setJMenuBar(menuBar.createMainMenu());
    }

    public void updateDeviceInfo() {
        jpReceiverInfo.updateInfo();
        jpStandInfo.updateInfo();
    }

    public void updateMenuStates() {
        menuBar.updateMenuStates();
    }

    public void showErrorMessage(String title, Exception e) {
        JOptionPane.showMessageDialog(
                View.this,
                e.getLocalizedMessage(),
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }
}
