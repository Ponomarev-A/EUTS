package view;

import org.jdatepicker.impl.DateComponentFormatter;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

/**
 * History frame class
 */
class HistoryFrame extends JFrame {

    private final int FRAME_WIDTH = 600;
    private final int FRAME_HEIGHT = 600;
    private JFormattedTextField jftfID;
    private JComboBox<String> jcbModel = new JComboBox<>();
    private JComboBox<String> jcbScheme = new JComboBox<>();
    private JComboBox<String> jlFirmware = new JComboBox<>();


    {
        NumberFormat format = NumberFormat.getIntegerInstance();

        NumberFormatter numberFormatter = new NumberFormatter(format);
        numberFormatter.setValueClass(Short.class); //optional, ensures you will always get a long value
//        numberFormatter.setAllowsInvalid(false); //this is the key!!
        numberFormatter.setCommitsOnValidEdit(false);
        numberFormatter.setMinimum(0); //Optional
        numberFormatter.setMaximum(Short.MAX_VALUE); //Optional

        MaskFormatter maskFormatter = new MaskFormatter();
        try {
            maskFormatter.setMask("#####");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        jftfID = new JFormattedTextField(maskFormatter);
    }

    HistoryFrame() {
        create();
        setVisible(true);
    }

    private void create() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        setMinimumSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setTitle("History");

        JPanel jpTop = new JPanel(new BorderLayout());
        jpTop.setBorder(new TitledBorder(
                View.TITLE_BORDER,
                "Search",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                View.TITLE_FONT
        ));

        JPanel jpParameters = new JPanel();
        jpParameters.setLayout(new BoxLayout(jpParameters, BoxLayout.X_AXIS));

        JPanel jpReceiverFields = new JPanel(new GridLayout(4, 2));
        jpReceiverFields.add(new JLabel("Receiver ID:"));
        jpReceiverFields.add(jftfID);
        jpReceiverFields.add(new JLabel("Model:"));
        jpReceiverFields.add(jcbModel);
        jpReceiverFields.add(new JLabel("Scheme:"));
        jpReceiverFields.add(jcbScheme);
        jpReceiverFields.add(new JLabel("Firmware:"));
        jpReceiverFields.add(jlFirmware);


        JPanel jpSamplePeriod = new JPanel();
        jpSamplePeriod.setLayout(new BoxLayout(jpSamplePeriod, BoxLayout.Y_AXIS));

        jpSamplePeriod.add(new JLabel("From date:"));
        jpSamplePeriod.add(createDatePicker());
        jpSamplePeriod.add(new JLabel("Before date:"));
        jpSamplePeriod.add(createDatePicker());

        jpParameters.add(jpReceiverFields);
        jpParameters.add(Box.createHorizontalStrut(20));
        jpParameters.add(jpSamplePeriod);

        JPanel jpControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        jpControls.add(new JButton("Clear"));
        jpControls.add(new JButton("Find"));


        jpTop.add(jpParameters, BorderLayout.NORTH);
        jpTop.add(jpControls, BorderLayout.SOUTH);


        JTable jtResults = new JTable();
        add(new JScrollPane(jtResults));


        add(jpTop, BorderLayout.NORTH);
        add(jtResults, BorderLayout.SOUTH);
    }

    private JDatePickerImpl createDatePicker() {

        UtilDateModel model = new UtilDateModel();

        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePickerImpl jDatePicker = new JDatePickerImpl(
                new JDatePanelImpl(model, p),
                new DateComponentFormatter()
        );

        jDatePicker.setAlignmentX(Component.LEFT_ALIGNMENT);

        return jDatePicker;
    }

    private class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {

        private String datePattern = "yyyy-MM-dd";
        private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

        @Override
        public Object stringToValue(String text) throws ParseException {
            return dateFormatter.parseObject(text);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value != null) {
                Calendar cal = (Calendar) value;
                return dateFormatter.format(cal.getTime());
            }

            return "";
        }
    }
}
