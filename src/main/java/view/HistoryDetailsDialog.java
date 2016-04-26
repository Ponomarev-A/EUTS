package view;

import controller.Controller;
import model.Device;
import model.Receiver;
import model.tests.BaseTestCase;
import model.tests.TestManager;
import org.apache.commons.lang.ArrayUtils;
import packet.Command;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

class HistoryDetailsDialog extends JDialog {

    private final int FRAME_WIDTH = 800;
    private final int FRAME_HEIGHT = 600;

    private Controller controller;

    private Receiver receiver;
    private Timestamp timestamp;
    private Map<Integer, TestManager.State> testResults;
    private ArrayList<Float[]> calibrationCoeffs;

    private JTable calibrCoeffsTable;
    private boolean isCoeffsChanged = false;
    private boolean isCoeffsCanBeChanged;


    HistoryDetailsDialog(Controller controller, Receiver receiver) {
        this.controller = controller;

        // Init by default values
        timestamp = new Timestamp(0);
        testResults = new TreeMap<>();
        calibrationCoeffs = new ArrayList<>();
        calibrationCoeffs.add(new Float[0]);
        calibrationCoeffs.add(new Float[0]);
        isCoeffsCanBeChanged = controller.isConnected() && controller.getReceiver().equals(receiver);

        // if selected receiver "really" connected to stand, then using it, otherwise using "virtual" receiver
        this.receiver = isCoeffsCanBeChanged ? controller.getReceiver() : receiver;

        fillDataSource();

        create();
        setVisible(true);
    }

    private void fillDataSource() {

        try (ResultSet rs = controller.selectTestSessions(receiver, null, null)) {
            rs.next();
            timestamp = rs.getTimestamp(5);

            for (Object testCaseID : (Object[]) rs.getObject(6))
                testResults.put((Integer) testCaseID, TestManager.State.PASS);

            for (Object testCaseID : (Object[]) rs.getObject(7))
                testResults.put((Integer) testCaseID, TestManager.State.FAIL);

            for (Object testCaseID : (Object[]) rs.getObject(8))
                testResults.put((Integer) testCaseID, TestManager.State.SKIP);
        } catch (SQLException e) {
            controller.showErrorMessage(
                    "Read receiver info",
                    "Read receiver info failed",
                    e
            );
        }

        try (ResultSet rs = controller.selectCalibrationCoeffs(receiver)) {
            rs.next();
            for (int i = 1; i <= 2; i++) {
                Object[] objects = (Object[]) rs.getObject(i);
                Float[] coeffs = new Float[Receiver.FREQUENCY_HZ.size()];
                for (int j = 0; j < objects.length; j++) {
                    coeffs[j] = (Float) objects[j];
                }
                calibrationCoeffs.set(i - 1, coeffs);
            }
        } catch (SQLException e) {
            controller.showErrorMessage(
                    "Read calibration coefficients",
                    "Read calibration coefficients failed",
                    e
            );
        }
    }

    private void create() {
        initFrame();

        JPanel jpTop = createTopPanel();
        JPanel jpBottom = createBottomPanel();

        add(jpTop, BorderLayout.NORTH);
        add(jpBottom, BorderLayout.CENTER);

        pack();
    }

    private void initFrame() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension((int) (FRAME_WIDTH * 0.8), FRAME_HEIGHT));
        setMinimumSize(new Dimension((int) (FRAME_WIDTH * 0.8), FRAME_HEIGHT));
        setLocationRelativeTo(null);
        setTitle("History details");
        setModal(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (isCoeffsChanged && controller.askUserWriteCalibrCoeffsToReceiver())
                    writeCoeffsToReceiver();
                super.windowClosing(e);
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setContentPane(panel);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(createInfoDetails());
        leftPanel.add(createControls());

        panel.add(leftPanel);
        panel.add(createCalibrationCoeffs());
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(new JScrollPane(createDetailsTable()));
        return panel;
    }

    private void writeCoeffsToReceiver() {
        float[] coeffs = ArrayUtils.addAll(
                ArrayUtils.toPrimitive(calibrationCoeffs.get(0)),
                ArrayUtils.toPrimitive(calibrationCoeffs.get(1))
        );
        try {
            Device receiver = controller.getReceiver();
            receiver.set(Command.WRITE_CALIBR_COEFFS_DEVICE, coeffs);

            controller.updateCalibrationCoeffsInDB(
                    calibrationCoeffs.get(0),
                    calibrationCoeffs.get(1)
            );

            isCoeffsChanged = false;

            controller.showMessage(
                    "Write calibration coefficients",
                    "New calibration coefficients successfully write to\n" + receiver
            );
        } catch (Exception e) {
            controller.showErrorMessage(
                    "Write calibration coefficients",
                    "Write calibration coefficients FAILED!",
                    e);
        }
    }

    private JPanel createInfoDetails() {
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JPanel jpLabels = new JPanel();
        jpLabels.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        jpLabels.setLayout(new BoxLayout(jpLabels, BoxLayout.Y_AXIS));
        jpLabels.add(new JLabel("Model:"));
        jpLabels.add(new JLabel("Firmware:"));
        jpLabels.add(new JLabel("Scheme:"));
        jpLabels.add(new JLabel("ID:"));
        jpLabels.add(new JLabel("Timestamp:"));

        JPanel jpValues = new JPanel();
        jpValues.setLayout(new BoxLayout(jpValues, BoxLayout.Y_AXIS));

        jpValues.add(new JLabel(receiver.getModel()));
        jpValues.add(new JLabel(receiver.getFirmware()));
        jpValues.add(new JLabel(receiver.getScheme()));
        jpValues.add(new JLabel(String.valueOf(receiver.getID())));
        jpValues.add(new JLabel(new SimpleDateFormat("EE, d MMMM yyyy, HH:mm").format(new Date(timestamp.getTime()))));

        panel.add(jpLabels);
        panel.add(jpValues);
        panel.revalidate();

        panel.setBackground(Color.ORANGE);
        return panel;
    }

    private JPanel createControls() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton jbRead = new JButton("Read coeffs");
        jbRead.setEnabled(isCoeffsCanBeChanged);
        jbRead.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                readCoeffsFromReceiver();
            }
        });


        JButton jbWrite = new JButton("Write coeffs");
        jbWrite.setEnabled(isCoeffsCanBeChanged);
        jbWrite.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                writeCoeffsToReceiver();
            }
        });

        panel.add(jbRead);
        panel.add(jbWrite);

        return panel;
    }

    private JPanel createCalibrationCoeffs() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        panel.setLayout(new BorderLayout());

        calibrCoeffsTable = new JTable(new CalibrCoeffsTableModel());
        calibrCoeffsTable.setEnabled(isCoeffsCanBeChanged);

        // FIX hack: Resize table height
        calibrCoeffsTable.setPreferredScrollableViewportSize(calibrCoeffsTable.getPreferredSize());

        panel.add(new JScrollPane(calibrCoeffsTable));
        return panel;
    }

    private JTable createDetailsTable() {
        JTable table = new JTable(new DetailsTableModel(testResults));
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setMaxWidth(50);
        columnModel.getColumn(0).setResizable(false);
        columnModel.getColumn(2).setMaxWidth(100);
        columnModel.getColumn(2).setResizable(false);

        table.setRowHeight(20);

        return table;
    }

    private void readCoeffsFromReceiver() {
        try {
            float[] coeffs = receiver.getFloatArray(Command.GET_CALIBR_COEFFS_DEVICE);
            calibrationCoeffs.set(0, ArrayUtils.toObject(ArrayUtils.subarray(coeffs, 0, 8)));
            calibrationCoeffs.set(1, ArrayUtils.toObject(ArrayUtils.subarray(coeffs, 8, 16)));

            controller.updateCalibrationCoeffsInDB(
                    calibrationCoeffs.get(0),
                    calibrationCoeffs.get(1)
            );
            controller.showMessage(
                    "Read calibration coefficients",
                    "Calibration coefficients successfully read from\n" + receiver
            );
        } catch (Exception e) {
            controller.showErrorMessage(
                    "Read calibration coefficients",
                    "Read calibration coefficients FAILED!",
                    e);
        } finally {
            ((AbstractTableModel) calibrCoeffsTable.getModel()).fireTableDataChanged();
        }
    }

    private class DetailsTableModel extends AbstractTableModel {

        private final Map<Integer, TestManager.State> testMap;
        private final java.util.List<BaseTestCase> testsList;
        private final String[] columnNames = {"ID", "Description", "State"};

        DetailsTableModel(Map<Integer, TestManager.State> testResults) {
            this.testMap = testResults;

            controller.getTestManager().fillTestList();
            this.testsList = controller.getTestManager().getTestList();
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

    private class CalibrCoeffsTableModel extends AbstractTableModel {

        private String[] columnNames = new String[]{"Frequency, Hz", "Depth", "Current"};

        @Override
        public int getRowCount() {
            return calibrationCoeffs.get(0).length;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Receiver.FREQUENCY_HZ.get(rowIndex);
                case 1:
                case 2:
                    return String.format(Locale.ENGLISH, "%.6f", calibrationCoeffs.get(columnIndex - 1)[rowIndex] != null ?
                            calibrationCoeffs.get(columnIndex - 1)[rowIndex] : 1.0f);
                default:
                    return null;
            }
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1 || columnIndex == 2;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            isCoeffsChanged = true;

            calibrationCoeffs.get(columnIndex - 1)[rowIndex] = Float.valueOf((String) aValue);
        }
    }
}