package view;

import controller.Controller;
import controller.EventListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Base view class for GUI
 */
public class View extends JFrame {

    private static final int FRAME_WIDTH = 1100;
    private static final int FRAME_HEIGHT = 800;

    private final Font titleFont = new Font(null, Font.BOLD, 20);
    private final Border titleBorder = BorderFactory.createLineBorder(Color.BLACK, 3);

    private Controller controller;
    private EventListener eventListener;
    private MainMenu mainMenu;

    public View(Controller controller) {
        this.controller = controller;
        mainMenu = new MainMenu(this, controller);
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void init() {

        createMainFrame(FRAME_WIDTH, FRAME_HEIGHT);
        mainMenu.createMainMenu();
        createMainPanel();

        setVisible(true);
    }

    private void createMainFrame(int width, int height) {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(width, height);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultLookAndFeelDecorated(true);
        setTitle("Electronic Units Test Stand (EUTS) application");
    }

    private void createMainPanel() {
        JPanel jpMain = new JPanel();
        jpMain.setLayout(new BoxLayout(jpMain, BoxLayout.X_AXIS));

        JPanel jpDevice = createDeviceInfoPanel(FRAME_WIDTH * 2 / 3, FRAME_HEIGHT / 5);
        JPanel jpStand = createStandInfoPanel(FRAME_WIDTH * 1 / 3, FRAME_HEIGHT / 5);

        jpMain.add(jpDevice);
        jpMain.add(jpStand);

        add(jpMain);
    }

    private JPanel createDeviceInfoPanel(int width, int height) {
        JPanel jPanel = new JPanel(new GridLayout(4, 1));
        jPanel.setMaximumSize(new Dimension(width, height));
        jPanel.setBackground(Color.LIGHT_GRAY);
        jPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        jPanel.setBorder(new TitledBorder(
                titleBorder,
                "Device info",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                titleFont
        ));
        JLabel jlModel = new JLabel("Model: ");
        JLabel jlFirmware = new JLabel("Firmware: ");
        JLabel jlScheme = new JLabel("Scheme: ");
        JLabel jlStatus = new JLabel("Status: ");

        add(jPanel);
        jPanel.add(jlModel);
        jPanel.add(jlFirmware);
        jPanel.add(jlScheme);
        jPanel.add(jlStatus);

        return jPanel;
    }

    private JPanel createStandInfoPanel(int width, int height) {
        JPanel jPanel = new JPanel(new GridLayout(3, 1));
        jPanel.setMaximumSize(new Dimension(width, height));
        jPanel.setBackground(Color.LIGHT_GRAY);
        jPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        jPanel.setBorder(new TitledBorder(
                titleBorder,
                "Stand info",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                titleFont
        ));
        JLabel jlFirmware = new JLabel("Firmware: ");
        JLabel jlScheme = new JLabel("Scheme: ");
        JLabel jlStatus = new JLabel("Status: ");

        add(jPanel);
        jPanel.add(jlFirmware);
        jPanel.add(jlScheme);
        jPanel.add(jlStatus);

        return jPanel;
    }


    public void updateMenuStates() {
        mainMenu.updateMenuStates();
    }
}
