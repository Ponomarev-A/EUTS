package view;

import controller.Controller;
import model.Receiver;
import org.jdatepicker.impl.DateComponentFormatter;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import view.AutoCompleteComboBox.AutoComboBox;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * History frame class
 */
class HistoryFrame extends JFrame {

    private final int FRAME_WIDTH = 800;
    private final int FRAME_HEIGHT = 600;

    private final Controller controller;

    private AutoComboBox jacbID;
    private JComboBox<String> jcbModel;
    private JComboBox<String> jcbScheme;
    private JComboBox<String> jcbFirmware;
    private JDatePickerImpl jdpFromDate;
    private JDatePickerImpl jdpToDate;
    private JTable jtResults;

    private ResultTableModel resultTableModel;

    HistoryFrame(Controller controller) {
        this.controller = controller;
        create();
        setVisible(true);
    }

    private void create() {
        initHistoryFrame();

        JPanel jpSearchFields = createSearchFields();
        JPanel jpResults = createResultTable();

        add(jpSearchFields);
        add(jpResults);
    }

    private void initHistoryFrame() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        setMinimumSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        setLocationRelativeTo(null);
        setTitle("History");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setContentPane(panel);
    }

    private JPanel createSearchFields() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel jpParameters = createSelectParameters();
        JPanel jpControls = createSelectControls();

        panel.add(jpParameters, BorderLayout.NORTH);
        panel.add(jpControls, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createResultTable() {
        JPanel panel = new JPanel(new BorderLayout());

        resultTableModel = new ResultTableModel();
        jtResults = new JTable(resultTableModel);
        jtResults.setAutoCreateColumnsFromModel(true);
        panel.add(new JScrollPane(jtResults));

        return panel;
    }

    private JPanel createSelectParameters() {
        JPanel jpParameters = new JPanel();
        jpParameters.setLayout(new BoxLayout(jpParameters, BoxLayout.X_AXIS));

        JPanel jpReceiverFields = createSelectReceiverFields();
        JPanel jpSamplePeriod = createSelectPeriodFields();

        jpParameters.add(jpReceiverFields);
        jpParameters.add(Box.createHorizontalStrut(20));
        jpParameters.add(jpSamplePeriod);
        return jpParameters;
    }

    private JPanel createSelectControls() {
        JPanel jpControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton jbClear = new JButton("Clear");
        jbClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jacbID.resetSelection();
                jcbModel.setSelectedIndex(-1);
                jcbScheme.setSelectedIndex(-1);
                jcbFirmware.setSelectedIndex(-1);

                jdpToDate.getModel().setValue(null);
                jdpFromDate.getModel().setValue(null);
            }
        });
        JButton jbFind = new JButton("Find");
        jbFind.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer ID = jacbID.getSelectedItem() != null ? Integer.parseInt((String) jacbID.getSelectedItem()) : null;
                String model = (String) jcbModel.getSelectedItem();
                String scheme = (String) jcbScheme.getSelectedItem();
                String firmware = (String) jcbFirmware.getSelectedItem();

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date toDate = (Date) jdpToDate.getModel().getValue();
                Date fromDate = (Date) jdpFromDate.getModel().getValue();

                String beforeDate = toDate != null ? df.format(toDate) : null;
                String afterDate = fromDate != null ? df.format(fromDate) : null;
                Receiver receiver = new Receiver(ID, model, scheme, firmware);

                ResultSet resultSet = controller.selectFromHistoryDB(receiver, afterDate, beforeDate);
                updateResultTable(resultSet);
            }
        });

        jpControls.add(jbClear);
        jpControls.add(jbFind);
        return jpControls;
    }

    private JPanel createSelectReceiverFields() {

        jacbID = new AutoComboBox();
        jcbModel = new JComboBox<>(controller.getReceiverModelsFromDB());
        jcbScheme = new JComboBox<>(controller.getReceiverSchemesFromDB());
        jcbFirmware = new JComboBox<>(controller.getReceiverFirmwaresFromDB());

        jacbID.setKeyWord(controller.getReceiverIDsFromDB());

        jacbID.setSelectedIndex(-1);
        jcbModel.setSelectedIndex(-1);
        jcbScheme.setSelectedIndex(-1);
        jcbFirmware.setSelectedIndex(-1);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JPanel pLeft = new JPanel();
        pLeft.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS));

        JPanel pRight = new JPanel();
        pRight.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        pRight.setLayout(new GridLayout(4, 1));

        pLeft.add(new JLabel("Receiver ID"));
        pRight.add(jacbID);
        pLeft.add(new JLabel("Model"));
        pRight.add(jcbModel);
        pLeft.add(new JLabel("Scheme"));
        pRight.add(jcbScheme);
        pLeft.add(new JLabel("Firmware"));
        pRight.add(jcbFirmware);

        panel.add(pLeft);
        panel.add(pRight);

        return panel;
    }

    private JPanel createSelectPeriodFields() {
        JPanel jpSamplePeriod = new JPanel();
        jpSamplePeriod.setLayout(new BoxLayout(jpSamplePeriod, BoxLayout.Y_AXIS));

        jdpFromDate = createDatePicker();
        jdpToDate = createDatePicker();

        jpSamplePeriod.add(new JLabel("From date"));
        jpSamplePeriod.add(jdpFromDate);
        jpSamplePeriod.add(new JLabel("Before date"));
        jpSamplePeriod.add(jdpToDate);

        return jpSamplePeriod;
    }

    private void updateResultTable(ResultSet resultSet) {
        try {
            resultTableModel.setDataSource(resultSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        private List<String> columnNames = Arrays.asList(
                "ID",
                "Model",
                "Scheme",
                "Firmware",
                "Date",
                "Pass",
                "Fail",
                "Skip"
        );

        private List<Class> columnTypes = new ArrayList<>();
        private ArrayList<ArrayList<Object>> data = new ArrayList<>();

        {
            columnTypes.add(Integer.class);
            columnTypes.add(String.class);
            columnTypes.add(String.class);
            columnTypes.add(String.class);
            columnTypes.add(Timestamp.class);
            columnTypes.add(Integer.class);
            columnTypes.add(Integer.class);
            columnTypes.add(Integer.class);
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {

            switch (getColumnName(columnIndex)) {
                case "Pass":
                case "Fail":
                case "Skip":
                    return ((Object[]) data.get(rowIndex).get(columnIndex)).length;

                default:
                    return (data.get(rowIndex)).get(columnIndex);
            }
        }

        @Override
        public String getColumnName(int column) {
            return columnNames.get(column);
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return columnTypes.get(column);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            (data.get(row)).set(column, value);
        }

        void setDataSource(ResultSet rs) throws Exception {

            // удаляем прежние данные
            data.clear();

            // получаем данные
            int columnCount = getColumnCount();
            while (rs.next()) {
                // здесь будем хранить ячейки одной строки
                ArrayList<Object> row = new ArrayList<>();
                for (int i = 0; i < columnCount; i++) {
                    row.add(rs.getObject(i + 1));
                }
                data.add(row);
            }

            rs.close();

            // сообщаем об изменениях в структуре данных
            fireTableStructureChanged();
        }
    }
}
