package view;

import model.tests.BaseTestCase;
import org.jdatepicker.impl.DateComponentFormatter;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
        numberFormatter.setValueClass(Short.class);
        numberFormatter.setAllowsInvalid(true);
        numberFormatter.setCommitsOnValidEdit(true);
        numberFormatter.setMinimum((short) 1);
        numberFormatter.setMaximum(Short.MAX_VALUE);

        jftfID = new JFormattedTextField(numberFormatter);
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
        JButton jbClearParameters = new JButton("Clear");
        JButton jbFind = new JButton("Find");

        jpControls.add(jbClearParameters);
        jpControls.add(jbFind);


        jpTop.add(jpParameters, BorderLayout.NORTH);
        jpTop.add(jpControls, BorderLayout.SOUTH);


        ResultTableModel resultTableModel = new ResultTableModel();
        JTable jtResults = new JTable(resultTableModel);
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

    private class ResultTableModel extends AbstractTableModel {

        private static final String INDEX = "№";
        private static final String ID = "ID";
        private static final String MODEL = "Model";
        private static final String PCB = "PCB";
        private static final String FIRMWARE = "Firmware";
        private static final String TEST_PASSED = "Passed";
        private static final String TEST_FAILED = "Failed";
        private static final String TEST_SKIPPED = "Skipped";

        private java.util.List<String> columnNames = Arrays.asList(INDEX, ID, MODEL, PCB, FIRMWARE, TEST_PASSED, TEST_FAILED, TEST_SKIPPED);
        private java.util.List<BaseTestCase> data = new ArrayList<>();

        void setData(java.util.List<BaseTestCase> data) {
            this.data = data;
        }

        /**
         * Returns the number of rows in the model. A
         * <code>JTable</code> uses this method to determine how many rows it
         * should display.  This method should be quick, as it
         * is called frequently during rendering.
         *
         * @return the number of rows in the model
         * @see #getColumnCount
         */
        @Override
        public int getRowCount() {
            return data.size();
        }

        /**
         * Returns the number of columns in the model. A
         * <code>JTable</code> uses this method to determine how many columns it
         * should create and display by default.
         *
         * @return the number of columns in the model
         * @see #getRowCount
         */
        @Override
        public int getColumnCount() {
            return columnNames.size();
        }

        /**
         * Returns the value for the cell at <code>columnIndex</code> and
         * <code>rowIndex</code>.
         *
         * @param rowIndex    the row whose value is to be queried
         * @param columnIndex the column whose value is to be queried
         * @return the value Object at the specified cell
         */
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                default:
                    return null;
            }
        }


        /**
         * Returns a default name for the column using spreadsheet conventions:
         * A, B, C, ... Z, AA, AB, etc.  If <code>column</code> cannot be found,
         * returns an empty string.
         *
         * @param column the column being queried
         * @return a string containing the default name of <code>column</code>
         */
        @Override
        public String getColumnName(int column) {
            return columnNames.get(column);
        }

        /**
         * Returns <code>Object.class</code> regardless of <code>columnIndex</code>.
         *
         * @param columnIndex the column being queried
         * @return the Object.class
         */
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                default:
                    return Object.class;
            }
        }

        /**
         * Returns false.  This is the default implementation for all cells.
         *
         * @param rowIndex    the row being queried
         * @param columnIndex the column being queried
         * @return false
         */
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;
        }

        /**
         * This empty implementation is provided so users don't have to implement
         * this method if their data model is not editable.
         *
         * @param aValue      value to assign to cell
         * @param rowIndex    row of cell
         * @param columnIndex column of cell
         */
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}
