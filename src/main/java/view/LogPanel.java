package view;

import controller.Controller;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Log panel class show all application and user actions in the multiline textfield window
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

    LogPanel(Controller controller) {
        this.controller = controller;
        create();
    }

    private void create() {

        setLayout(new BorderLayout());

        setBorder(new TitledBorder(
                View.TITLE_BORDER,
                "Log",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                View.TITLE_FONT
        ));

        jepLog.setEditable(false);
        jepLog.setAutoscrolls(true);

        add(new JScrollPane(jepLog));
    }

    void updateLog(String text, AttributeSet attributeSet) {
        Document doc = jepLog.getDocument();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String time = attributeSet.equals(NORMAL) ? sdf.format(new Date()) + ": " : "";

        try {
            doc.insertString(doc.getLength(), "\n" + time + text, attributeSet);
            jepLog.setCaretPosition(doc.getLength());
        } catch (BadLocationException ignored) {
        }
    }
}
