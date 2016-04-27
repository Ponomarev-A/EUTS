package view;

import com.github.lgooddatepicker.datepicker.DatePicker;
import controller.Controller;
import model.Receiver;
import view.widget.AutoComboBox;
import view.widget.GridBagHelper;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * History frame class
 */
class HistoryFrame extends JFrame {

    private static final int FRAME_WIDTH = 600;
    private static final int FRAME_HEIGHT = 600;

    private final Controller controller;
    private ResultTableModel resultTableModel;

    private AutoComboBox jacbID;
    private JComboBox<String> jcbModel;
    private JComboBox<String> jcbScheme;
    private JComboBox<String> jcbFirmware;
    private DatePicker jdpFromDate;
    private DatePicker jdpToDate;
    private JTable jtResults;

    HistoryFrame(Controller controller) {
        this.controller = controller;
        create();
        setVisible(true);
    }

    private void create() {
        initFrame();

        JPanel jpSearchFields = createSearchFields();
        JPanel jpResults = createResultTable();

        add(jpSearchFields, BorderLayout.NORTH);
        add(jpResults, BorderLayout.CENTER);
    }

    private void initFrame() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        setMinimumSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        setLocationRelativeTo(null);
        setTitle("History (" + controller.getDatabaseURL() + ")");

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
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
        columnModel.getColumn(1).setMinWidth(80);
        columnModel.getColumn(2).setMinWidth(150);
        columnModel.getColumn(3).setMinWidth(60);
        columnModel.getColumn(4).setMinWidth(120);

        jtResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        jtResults.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    JTable table = (JTable) me.getSource();
                    int row = table.rowAtPoint(me.getPoint());
                    ArrayList<Object> rowData = resultTableModel.data.get(row);

                    Integer ID = (Integer) rowData.get(resultTableModel.findColumn("ID"));
                    String model = (String) rowData.get(resultTableModel.findColumn("Model"));
                    String scheme = (String) rowData.get(resultTableModel.findColumn("Scheme"));
                    String firmware = (String) rowData.get(resultTableModel.findColumn("Firmware"));

                    new HistoryDetailsDialog(controller, new Receiver(ID, model, scheme, firmware));
                }
            }
        });

        panel.add(new JScrollPane(jtResults));
        return panel;
    }

    private JPanel createSelectParameters() {

        jacbID = new AutoComboBox();
        try {
            jcbModel = new JComboBox<>(controller.getReceiverModelsFromDB());
            jcbScheme = new JComboBox<>(controller.getReceiverSchemesFromDB());
            jcbFirmware = new JComboBox<>(controller.getReceiverFirmwaresFromDB());

            jacbID.setKeyWord(controller.getReceiverIDsFromDB());
        } catch (SQLException e) {
            controller.showErrorMessage(
                    "Read receiver parameters",
                    "Read receiver parameters from database failed",
                    e
            );
        }

        jacbID.setSelectedIndex(-1);
        jcbModel.setSelectedIndex(-1);
        jcbScheme.setSelectedIndex(-1);
        jcbFirmware.setSelectedIndex(-1);

        jdpFromDate = new DatePicker();
        jdpToDate = new DatePicker();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagHelper helper = new GridBagHelper();

        panel.add(new JLabel("Receiver ID"), helper.nextCell().fillHorizontally().gap(20).get());
        panel.add(jacbID, helper.nextCell().fillHorizontally().gap(10).setWeights(0.3f, 0).get());
        panel.add(new JLabel("From date"), helper.nextCell().fillHorizontally().setWeights(1.0f, 0).get());

        panel.add(new JLabel("Model"), helper.nextRow().nextCell().fillHorizontally().gap(20).get());
        panel.add(jcbModel, helper.nextCell().fillHorizontally().gap(10).get());
        panel.add(jdpFromDate, helper.nextCell().fillHorizontally().get());

        panel.add(new JLabel("Scheme"), helper.nextRow().nextCell().fillHorizontally().gap(20).get());
        panel.add(jcbScheme, helper.nextCell().fillHorizontally().gap(10).get());
        panel.add(new JLabel("Before date"), helper.nextCell().fillHorizontally().get());

        panel.add(new JLabel("Firmware"), helper.nextRow().nextCell().fillHorizontally().gap(20).get());
        panel.add(jcbFirmware, helper.nextCell().fillHorizontally().gap(10).get());
        panel.add(jdpToDate, helper.nextCell().fillHorizontally().get());

        return panel;
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
            public void actionPerformed(ActionEvent event) {
                Integer ID = jacbID.getSelectedItem() != null ? Integer.parseInt((String) jacbID.getSelectedItem()) : null;
                String model = (String) jcbModel.getSelectedItem();
                String scheme = (String) jcbScheme.getSelectedItem();
                String firmware = (String) jcbFirmware.getSelectedItem();

                String beforeDate = jdpToDate.getDateStringOrSuppliedString(null);
                String afterDate = jdpFromDate.getDateStringOrSuppliedString(null);

                Receiver receiver = new Receiver(ID, model, scheme, firmware);

                try {
                    ResultSet rs = controller.selectTestSessions(receiver, afterDate, beforeDate);
                    resultTableModel.setDataSource(rs);
                } catch (SQLException e) {
                    controller.showErrorMessage(
                            "Find test sessions",
                            "Find test sessions information failed",
                            e
                    );
                }
            }
        });

        jpControls.add(jbClear);
        jpControls.add(jbFind);
        return jpControls;
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

        void setDataSource(ResultSet rs) throws SQLException {
            data.clear();

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
            fireTableDataChanged();
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

                case "Date":
                    Timestamp timestamp = (Timestamp) data.get(rowIndex).get(columnIndex);
                    return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(timestamp.getTime()));

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

}
