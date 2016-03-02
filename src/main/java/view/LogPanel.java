package view;

import controller.Controller;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;

/**
 * Log class show all application and user actions in the multiline textfield window
 */
public class LogPanel extends JPanel {

    public static final SimpleAttributeSet BOLD = new SimpleAttributeSet();
    public static final SimpleAttributeSet NORMAL = new SimpleAttributeSet();

    static {
        StyleConstants.setBold(BOLD, true);
    }

    private Controller controller;
    private JEditorPane jepLog = new JEditorPane(
            "text/html",
            "<b>### Here will be displayed all information about the testing process ###</b>\n"
    );

    public LogPanel(Controller controller) {
        this.controller = controller;
    }

    JPanel create(int width, int height) {

        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.setMaximumSize(new Dimension(width, height));
        jPanel.setBackground(Color.LIGHT_GRAY);

        jPanel.setBorder(new TitledBorder(
                View.TITLE_BORDER,
                "Log",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                View.DEFAULT_FONT
        ));

        jepLog.setEditable(false);
        jepLog.setAutoscrolls(true);

        jPanel.add(new JScrollPane(jepLog), BorderLayout.CENTER);

        return jPanel;
    }

    public void updateLog(String text, AttributeSet attributeSet) {
        Document doc = jepLog.getDocument();

        try {
            doc.insertString(doc.getLength(), "\n" + text, attributeSet);
        } catch (BadLocationException e) {
        }
    }
}
