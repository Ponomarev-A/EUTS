package view;

import controller.Controller;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.SimpleAttributeSet;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
    private LogPanel jpLog;
    private TestsPanel jpTests;


    public View(Controller controller) {
        this.controller = controller;
    }

    public void init() {

        createMainFrame();
        createMainPanel();
        createMainMenu();

        setVisible(true);
    }

    private void createMainFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultLookAndFeelDecorated(true);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setTitle("Electronic Units Test Stand (EUTS) application");

        // Set new default font to labels
        UIManager.put("Label.font", DEFAULT_FONT);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.destroyConnectionManager();
                super.windowClosing(e);
            }
        });
    }

    private void createMainPanel() {
        JPanel jpTop = new JPanel();
        JPanel jpBottom = new JPanel();
        jpTop.setLayout(new BoxLayout(jpTop, BoxLayout.X_AXIS));
        jpBottom.setLayout(new BoxLayout(jpBottom, BoxLayout.X_AXIS));

        jpReceiverInfo = new ReceiverInfoPanel(controller);
        jpStandInfo = new StandInfoPanel(controller);
        jpLog = new LogPanel(controller);
        jpTests = new TestsPanel(controller);

        jpTop.add(jpReceiverInfo.create(FRAME_WIDTH / 2, FRAME_HEIGHT / 5));
        jpTop.add(jpStandInfo.create(FRAME_WIDTH / 2, FRAME_HEIGHT / 5));

        jpBottom.add(jpTests.create(FRAME_WIDTH / 2, FRAME_HEIGHT * 4 / 5));
        jpBottom.add(jpLog.create(FRAME_WIDTH / 2, FRAME_HEIGHT * 4 / 5));


        add(jpTop);
        add(jpBottom);
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

    public void updateLog(String text, SimpleAttributeSet attributeSet) {
        jpLog.updateLog(text, attributeSet);
    }

    public void updateTestList() {
        jpTests.updateList();
    }
}
