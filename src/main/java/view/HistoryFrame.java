package view;

import com.github.lgooddatepicker.datepicker.DatePicker;
import controller.Controller;
import model.Receiver;
import model.tests.BaseTestCase;
import model.tests.TestManager;
import view.AutoCompleteComboBox.AutoComboBox;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.Timestamp;
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
    private DatePicker jdpFromDate;
    private DatePicker jdpToDate;
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
        TableColumnModel columnModel = jtResults.getColumnModel();
        columnModel.getColumn(0).setMinWidth(50);
        columnModel.getColumn(1).setMinWidth(100);
        columnModel.getColumn(2).setMinWidth(160);
        columnModel.getColumn(3).setMinWidth(60);
        columnModel.getColumn(5).setMinWidth(70);
        columnModel.getColumn(6).setMinWidth(70);
        columnModel.getColumn(7).setMinWidth(70);
        jtResults.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        jtResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jtResults.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                JTable table = (JTable) me.getSource();
                Point p = me.getPoint();
                int row = table.rowAtPoint(p);
                if (me.getClickCount() == 2) {
                    // your valueChanged overridden method
                    new HistoryDetails(row).setVisible(true);
                }
            }
        });

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

                jdpToDate.setText("");
                jdpFromDate.setText("");
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

                String beforeDate = jdpToDate.getDateStringOrSuppliedString(null);
                String afterDate = jdpFromDate.getDateStringOrSuppliedString(null);
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

        jdpFromDate = new DatePicker();
        jdpToDate = new DatePicker();

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

    private static class ResultTableModel extends AbstractTableModel {

        private static List<String> columnNames = Arrays.asList(
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
            columnTypes.add(String.class);
            columnTypes.add(Integer.class);
            columnTypes.add(Integer.class);
            columnTypes.add(Integer.class);
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        ArrayList<ArrayList<Object>> getDataSource() {
            return data;
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

                case "Date":
                    Timestamp timestamp = (Timestamp) data.get(rowIndex).get(columnIndex);
                    return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(timestamp.getTime()));

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


    }

    private class HistoryDetails extends JFrame {

        private final Integer receiverID;
        private final String model;
        private final String scheme;
        private final String firmware;
        private final Timestamp timestamp;
        private final Map<Integer, TestManager.State> stateMap;

        HistoryDetails(int row) {

            ArrayList<ArrayList<Object>> dataSource = resultTableModel.getDataSource();

            receiverID = (Integer) dataSource.get(row).get(0);
            model = (String) dataSource.get(row).get(1);
            scheme = (String) dataSource.get(row).get(2);
            firmware = (String) dataSource.get(row).get(3);
            timestamp = (Timestamp) dataSource.get(row).get(4);

            stateMap = new TreeMap<>();
            for (Object ID : (Object[]) dataSource.get(row).get(5))
                stateMap.put((Integer) ID, TestManager.State.PASS);

            for (Object ID : (Object[]) dataSource.get(row).get(6))
                stateMap.put((Integer) ID, TestManager.State.FAIL);

            for (Object ID : (Object[]) dataSource.get(row).get(7))
                stateMap.put((Integer) ID, TestManager.State.SKIP);

            this.create();
            this.pack();
        }

        private void create() {
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            setPreferredSize(new Dimension((int) (FRAME_WIDTH * 0.8), FRAME_HEIGHT));
            setMinimumSize(new Dimension((int) (FRAME_WIDTH * 0.8), FRAME_HEIGHT));
            setLocationRelativeTo(null);
            setTitle("History details");

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            setContentPane(panel);


            JPanel jpReceiverInfo = createReceiverDetails();
            JTable detailsTable = createDetailsTable();

            add(jpReceiverInfo);
            add(new JScrollPane(detailsTable));
        }

        private JPanel createReceiverDetails() {
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(6, 2));

            JLabel jlModel = new JLabel(model);
            JLabel jlFirmware = new JLabel(firmware);
            JLabel jlScheme = new JLabel(scheme);
            JLabel jlReceiverID = new JLabel(String.valueOf(receiverID));
            JLabel jlDate = new JLabel(new SimpleDateFormat("EEEE, d MMMM yyyy").format(new Date(timestamp.getTime())));
            JLabel jlTime = new JLabel(new SimpleDateFormat("HH:mm:ss").format(new Date(timestamp.getTime())));

            panel.add(new JLabel("Model:"));
            panel.add(jlModel);
            panel.add(new JLabel("Firmware:"));
            panel.add(jlFirmware);
            panel.add(new JLabel("Scheme:"));
            panel.add(jlScheme);
            panel.add(new JLabel("Receiver ID:"));
            panel.add(jlReceiverID);
            panel.add(new JLabel("Date:"));
            panel.add(jlDate);
            panel.add(new JLabel("Time:"));
            panel.add(jlTime);

            return panel;
        }

        private JTable createDetailsTable() {
            JTable table = new JTable(new DetailsTableModel(stateMap));
            TableColumnModel columnModel = table.getColumnModel();
            columnModel.getColumn(0).setMaxWidth(50);
            columnModel.getColumn(0).setResizable(false);
            columnModel.getColumn(2).setMaxWidth(100);
            columnModel.getColumn(2).setResizable(false);

            table.setRowHeight(20);

            return table;
        }

        private class DetailsTableModel extends AbstractTableModel {
            private final Map<Integer, TestManager.State> testMap;
            private final List<BaseTestCase> testsList;
            private final String[] columnNames = {"ID", "Description", "State"};

            DetailsTableModel(Map<Integer, TestManager.State> testMap) {
                this.testMap = testMap;
                testsList = controller.getTestManager().getTestList();
            }

            @Override
            public String getColumnName(int column) {
                return columnNames[column];
            }

            @Override
            public int getRowCount() {
                return testMap.size();
            }

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return Number.class;
                    case 1:
                        return String.class;
                    case 2:
                        return String.class;
                    default:
                        return Object.class;
                }
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                BaseTestCase testCase = testsList.get(rowIndex);
                switch (columnIndex) {
                    case 0:
                        return testCase.getId();
                    case 1:
                        return testCase.getName();
                    case 2:
                        return testMap.get(testCase.getId()).toString();

                    default:
                        return null;
                }
            }
        }
    }
}
