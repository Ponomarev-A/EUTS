package view;

import controller.Controller;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.AttributeSet;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * Base view class for GUI
 */
public class View extends JFrame {

    // Color constants
    static final Color DARK_GREEN = new Color(0, 130, 0);
    static final Color BLACK = Color.black;
    static final Color RED = Color.RED;

    static final Font TITLE_FONT = new Font(null, Font.BOLD, 20);
    static final Border TITLE_BORDER = BorderFactory.createLineBorder(Color.BLACK, 3);
    private static final Font DEFAULT_FONT = new Font(null, Font.PLAIN, 18);

    private static final int FRAME_HEIGHT = 800;
    private static final int FRAME_WIDTH = 1200;
    private static final FileFilter DB_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || (f.getName().endsWith(".db"));
        }

        @Override
        public String getDescription() {
            return "Database file (*.db)";
        }
    };
    private Controller controller;
    private MenuBar menuBar;
    private InfoPanel jpInfo;
    private LogPanel jpLog;
    private TestsPanel jpTests;
    private JToolBar toolBar;


    public View(Controller controller) {
        this.controller = controller;

        menuBar = new MenuBar(controller);
        toolBar = new ToolBar(controller);
    }

    public void init() {

        createMainFrame();
        createMainPanel();
        createToolBar();
        createMenuBar();

        pack();
        revalidate();
        setVisible(true);
    }

    private void createMainFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        setMinimumSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setTitle("Electronic Units Test Stand (EUTS) application");

        // Set new default font to labels
        UIManager.put("Label.font", DEFAULT_FONT);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.windowClosing();
                super.windowClosing(e);
            }
        });
    }

    private void createMainPanel() {
        JSplitPane panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);

        JPanel jpLeft = new JPanel();
        jpLeft.setPreferredSize(new Dimension(FRAME_WIDTH / 2, FRAME_HEIGHT));
        jpLeft.setLayout(new BoxLayout(jpLeft, BoxLayout.Y_AXIS));

        jpTests = new TestsPanel(controller);
        jpInfo = new InfoPanel(controller);

        jpLeft.add(jpInfo);
        jpLeft.add(jpTests);

        jpLog = new LogPanel(controller);

        panel.setLeftComponent(jpLeft);
        panel.setRightComponent(jpLog);
        panel.setDividerLocation((int) (FRAME_WIDTH * 0.45f));

        add(panel);
    }

    private void createToolBar() {
        add(toolBar, BorderLayout.NORTH);
    }

    private void createMenuBar() {
        setJMenuBar(menuBar);
    }

    public void updateDeviceInfo() {
        jpInfo.updateInfo();
    }

    public void updateMenuStates() {
        menuBar.updateMenuStates();
    }

    public void showErrorMessage(String title, String text) {
        JOptionPane.showMessageDialog(
                getFocusOwner(),
                text,
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }

    public void showMessage(String title, String text) {
        JOptionPane.showMessageDialog(
                getFocusOwner(),
                text,
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void updateLog(String text, AttributeSet... attributeSet) {
        jpLog.updateLog(text, attributeSet);
    }

    public void fillTestList() {
        jpTests.fillTestList();
    }

    public void clearTestList() {
        jpTests.clearTestList();
    }

    public void updateTestList() {
        jpTests.updateTestList();
    }

    public void updateTestControls() {
        jpTests.updateTestControls();
    }

    public String getPathToDatabase() {
        int result = JOptionPane.showOptionDialog(
                this,
                "Database file (*.mv.db) not found.\nWhat do you want to do with database file?",
                "Load database",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[]{"Open/create database file", "Do not use database"},
                null);


        if (result == JOptionPane.YES_OPTION) {
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setDialogTitle("Open/create database file");
            jFileChooser.setFileFilter(DB_FILE_FILTER);

            if (jFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                return jFileChooser.getSelectedFile().getPath().split("\\.")[0];
            }
        }

        return "";
    }

    public void openHistoryWindow() {
        new HistoryFrame(controller);
    }

    public boolean askInsertResultToDB() {
        return controller.isDBExist() &&
                JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                this,
                "Insert test results to database?",
                "Database commit",
                JOptionPane.YES_NO_OPTION
        );
    }
}
