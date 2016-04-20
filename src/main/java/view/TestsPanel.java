package view;

import controller.Controller;
import model.tests.BaseTestCase;
import model.tests.TestManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

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

        TableColumnModel columnModel = jtTests.getColumnModel();
        columnModel.setColumnSelectionAllowed(false);

        TableColumn columnNumber = columnModel.getColumn(0);
        TableColumn columnCheck = columnModel.getColumn(1);
        TableColumn columnState = columnModel.getColumn(3);

        columnNumber.setMaxWidth(50);
        columnNumber.setResizable(false);
        columnCheck.setMaxWidth(60);
        columnCheck.setResizable(false);
        columnState.setMaxWidth(100);
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
            testTableModel.setValueAt(setChecked, i, 1);
        }
    }

    private void setCheckFailedTests() {
        for (int i = 0; i < testTableModel.getRowCount(); i++) {
            TestManager.State state = testTableModel.getTestResults().get(testTableModel.getTestList().get(i).getId());
            testTableModel.setValueAt(state != null && state.equals(TestManager.State.FAIL), i, 1);
        }
    }

    private class TestTableModel extends AbstractTableModel {

        private static final String INDEX = "ID";
        private static final String CHECK = "Check";
        private static final String NAME = "Description";
        private static final String STATE = "State";

        private List<String> columnNames = Arrays.asList(INDEX, CHECK, NAME, STATE);

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
                case 0:
                    return testCase.getId();
                case 1:
                    return testCase.isEnabled();
                case 2:
                    return testCase.getName();
                case 3:
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
                case 0:
                    return Number.class;
                case 1:
                    return Boolean.class;
                case 2:
                    return String.class;
                case 3:
                    return String.class;

                default:
                    return Object.class;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 1) {
                testList.get(rowIndex).setEnabled((Boolean) aValue);
            }
        }
    }
}
