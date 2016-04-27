package view;

import controller.Controller;
import model.tests.BaseTestCase;
import model.tests.TestManager;
import org.apache.commons.lang.StringEscapeUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

import static model.tests.TestManager.State.FAIL;

/**
 * Test panel class contains testList list
 */
class TestsPanel extends JPanel implements ActionListener {

    private static final String STRING_CHECK_ALL = "Check all";
    private static final String STRING_UNCHECK_ALL = "Uncheck all";
    private static final String STRING_CHECK_FAILED = "Check failed";

    private Controller controller;
    private TestTableModel testTableModel;
    private JTable jtTests;
    private JButton jbCheckAll;
    private JButton jbUncheckAll;
    private JButton jbCheckFailed;

    TestsPanel(Controller controller) {
        this.controller = controller;

        setBorder(new TitledBorder(
                View.TITLE_BORDER,
                "Tests",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                View.TITLE_FONT
        ));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel jpControls = createTestControls();
        JPanel jpTable = createTestTable();

        add(jpControls);
        add(jpTable);
    }

    private JPanel createTestControls() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        jbCheckAll = new JButton(STRING_CHECK_ALL);
        jbUncheckAll = new JButton(STRING_UNCHECK_ALL);
        jbCheckFailed = new JButton(STRING_CHECK_FAILED);

        jbCheckAll.addActionListener(this);
        jbUncheckAll.addActionListener(this);
        jbCheckFailed.addActionListener(this);

        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.add(jbCheckAll);
        panel.add(jbUncheckAll);
        panel.add(jbCheckFailed);

        return panel;
    }

    private JPanel createTestTable() {

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        testTableModel = new TestTableModel();
        jtTests = new JTable(testTableModel);
        jtTests.setDefaultRenderer(Object.class, new TestStateRenderer());

        TableColumnModel columnModel = jtTests.getColumnModel();
        columnModel.setColumnSelectionAllowed(false);

        TableColumn columnCheck = columnModel.getColumn(TestTableModel.COL_CHECK_INX);
        TableColumn columnID = columnModel.getColumn(TestTableModel.COL_ID_INX);
        TableColumn columnState = columnModel.getColumn(TestTableModel.COL_STATE_INX);

        columnID.setMaxWidth(50);
        columnID.setResizable(false);
        columnCheck.setMaxWidth(30);
        columnCheck.setResizable(false);
        columnState.setMaxWidth(80);
        columnState.setResizable(false);

        jtTests.setColumnModel(columnModel);
        jtTests.setRowHeight(20);
        jtTests.setRowSelectionAllowed(false);

        panel.add(new JScrollPane(jtTests));

        return panel;
    }

    void fillTestList() {
        if (controller.isConnected()) {
            controller.getTestManager().fillTestList();
            testTableModel.setDataSources(controller.getTestManager());
            updateTestList();
        }
    }

    void updateTestList() {
        testTableModel.fireTableDataChanged();
    }

    void clearTestList() {
        testTableModel.removeDataSources();
        updateTestList();
    }

    void updateTestControls() {

        boolean isConnected = controller.isConnected();
        boolean isTestRunning = controller.isTestRunning();

        jbCheckAll.setEnabled(isConnected && !isTestRunning);
        jbUncheckAll.setEnabled(isConnected && !isTestRunning);
        jbCheckFailed.setEnabled(isConnected && !isTestRunning);
        jtTests.setEnabled(!isTestRunning);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case STRING_CHECK_ALL:
                setCheckAllTests(true);
                break;

            case STRING_UNCHECK_ALL:
                setCheckAllTests(false);
                break;

            case STRING_CHECK_FAILED:
                setCheckFailedTests();
                break;
        }
        updateTestList();
    }

    private void setCheckAllTests(boolean setChecked) {
        for (int i = 0; i < testTableModel.getRowCount(); i++) {
            testTableModel.setValueAt(setChecked, i, TestTableModel.COL_CHECK_INX);
        }
    }

    private void setCheckFailedTests() {
        for (int i = 0; i < testTableModel.getRowCount(); i++) {
            TestManager.State state = testTableModel.getTestResults().get(testTableModel.getTestList().get(i).getId());
            testTableModel.setValueAt(state != null && state.equals(FAIL), i, TestTableModel.COL_CHECK_INX);
        }
    }

    private class TestTableModel extends AbstractTableModel {

        static final int COL_CHECK_INX = 0;
        static final int COL_ID_INX = 1;
        static final int COL_DESC_INX = 2;
        static final int COL_STATE_INX = 3;

        private List<String> columnNames = Arrays.asList(
                StringEscapeUtils.unescapeJava("\\u2611"),
                "ID",
                "Description",
                "State"
        );

        private List<BaseTestCase> testList = new ArrayList<>();
        private Map<Integer, TestManager.State> testResults = new TreeMap<>();

        void setDataSources(TestManager testManager) {
            this.testList = testManager.getTestList();
            this.testResults = testManager.getTestResults();
        }

        void removeDataSources() {
            testList.clear();
            testResults.clear();
        }

        List<BaseTestCase> getTestList() {
            return testList;
        }

        Map<Integer, TestManager.State> getTestResults() {
            return testResults;
        }

        @Override
        public int getRowCount() {
            return testList.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {

            BaseTestCase testCase = testList.get(rowIndex);
            TestManager.State state = testResults.get(testCase.getId());

            switch (columnIndex) {
                case COL_CHECK_INX:
                    return testCase.isEnabled();
                case COL_ID_INX:
                    return testCase.getId();
                case COL_DESC_INX:
                    return testCase.getName();
                case COL_STATE_INX:
                    return state != null ? state.toString() : "";
                default:
                    return null;
            }
        }

        @Override
        public String getColumnName(int column) {
            return columnNames.get(column);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case COL_CHECK_INX:
                    return Boolean.class;
                case COL_ID_INX:
                    return Number.class;
                case COL_DESC_INX:
                    return String.class;
                case COL_STATE_INX:
                    return String.class;
                default:
                    return Object.class;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == COL_CHECK_INX;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == COL_CHECK_INX) {
                testList.get(rowIndex).setEnabled((Boolean) aValue);
            }
        }
    }

    private class TestStateRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (column == TestTableModel.COL_STATE_INX) {
                String state = (String) value;
                if (!state.isEmpty()) {
                    switch (TestManager.State.valueOf(state)) {
                        case PASS:
                            c.setBackground(Color.GREEN);
                            break;
                        case FAIL:
                            c.setBackground(Color.RED);
                            break;
                        case SKIP:
                            c.setBackground(Color.LIGHT_GRAY);
                            break;
                        case ABORT:
                            c.setBackground(Color.ORANGE);
                            break;
                        case RUN:
                            c.setBackground(Color.YELLOW);
                            break;
                    }
                }
            } else {
                c.setBackground(table.getBackground());
            }
            return c;
        }
    }
}
