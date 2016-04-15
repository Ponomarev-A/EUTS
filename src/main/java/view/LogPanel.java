package view;

import controller.Controller;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Log panel class show all application and user actions in the multiline textfield window
 */
public class LogPanel extends JPanel {

    // Style attributes
    public static final SimpleAttributeSet BOLD = new SimpleAttributeSet();
    public static final SimpleAttributeSet NORMAL = new SimpleAttributeSet();

    // Color attributes
    public static final SimpleAttributeSet RED = new SimpleAttributeSet();
    public static final SimpleAttributeSet GREEN = new SimpleAttributeSet();
    public static final SimpleAttributeSet BLACK = new SimpleAttributeSet();

    static {
        StyleConstants.setBold(BOLD, true);
        StyleConstants.setBold(NORMAL, false);
        StyleConstants.setForeground(RED, View.RED);
        StyleConstants.setForeground(GREEN, View.DARK_GREEN);
        StyleConstants.setForeground(BLACK, View.BLACK);
    }

    private final SimpleAttributeSet set = new SimpleAttributeSet();
    private Controller controller;
    private JEditorPane jepLog = new JEditorPane();

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

        jepLog.setContentType("text/html");
        jepLog.setEditable(false);

        StyleConstants.setFontSize(set, 15);
        StyleConstants.setFontFamily(set, "Courier New");

        updateLog("### Here will be displayed all information about the testing process ###", BOLD);

        add(new JScrollPane(jepLog));
    }

    void updateLog(String text, AttributeSet... attributeSet) {

        for (AttributeSet attr : attributeSet) {
            set.addAttributes(attr);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String time = Arrays.asList(attributeSet).contains(NORMAL) ? sdf.format(new Date()) + ": " : "";

        appendText(time + text, set);
    }

    private void appendText(String text, SimpleAttributeSet set) {
        try {
            Document doc = jepLog.getDocument();
            int length = doc.getLength();

            // Move the insertion point to the end
            jepLog.setCaretPosition(length);

            // Insert the text
            doc.insertString(length, "\n" + text, set);

            // Convert the new end location
            // to view co-ordinates
            Rectangle r = jepLog.modelToView(length);

            // Finally, scroll so that the new text is visible
            if (r != null) {
                jepLog.scrollRectToVisible(r);
            }
        } catch (BadLocationException e) {
            System.out.println("Failed to append text: " + e);
        }
    }
}
