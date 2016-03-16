package view;

import controller.Controller;
import model.tests.BaseTestCase;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Test panel class contains testList list
 */
public class TestsPanel extends JPanel implements TableModelListener {

    private Controller controller;

    private TestTableModel tableModel = new TestTableModel();
    private JTable jtTests = new JTable(tableModel);


    public TestsPanel(Controller controller) {
        this.controller = controller;
    }

    public JPanel create(int width, int height) {

        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.setMaximumSize(new Dimension(width, height));
        jPanel.setMinimumSize(new Dimension(width, height));
        jPanel.setBackground(Color.LIGHT_GRAY);

        jPanel.setBorder(new TitledBorder(
                View.TITLE_BORDER,
                "Tests",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                View.TITLE_FONT
        ));

        jPanel.add(new JScrollPane(jtTests));

        jtTests.getModel().addTableModelListener(this);
        TableColumnModel columnModel = jtTests.getColumnModel();
        columnModel.setColumnSelectionAllowed(false);

        TableColumn columnNumber = columnModel.getColumn(0);
        TableColumn columnCheck = columnModel.getColumn(1);
        TableColumn columnState = columnModel.getColumn(3);

        columnNumber.setMaxWidth(20);
        columnNumber.setResizable(false);
        columnCheck.setMaxWidth(45);
        columnCheck.setResizable(false);
        columnState.setMaxWidth(90);
        columnState.setResizable(false);

        jtTests.setColumnModel(columnModel);
        jtTests.setRowHeight(20);
        jtTests.setRowSelectionAllowed(false);

        return jPanel;
    }

    /**
     * This fine grain notification tells listeners the exact range
     * of cells, rows, or columns that changed.
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        int column = e.getColumn();
        int firstRow = e.getFirstRow();
        int lastRow = e.getLastRow();
    }

    public void loadTestList() {

        List<BaseTestCase> testsList = controller.getTestsList();
        tableModel.setData(testsList);
    }

    public void updateTestList() {
        tableModel.fireTableDataChanged();
    }

    private class TestTableModel extends AbstractTableModel {

        private String[] columnNames = {"№", "Check", "Name", "State"};
        private List<BaseTestCase> data = new ArrayList<>();

        public void setData(List<BaseTestCase> data) {
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
            return columnNames.length;
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
            BaseTestCase testCase = data.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return rowIndex + 1;
                case 1:
                    return testCase.isEnabled();
                case 2:
                    return testCase.getName();
                case 3:
                    return testCase.getState().toString();

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
            return columnNames[column];
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
                case 0:
                    return Number.class;
                case 1:
                    return Boolean.class;
                case 2:
                    return String.class;
                case 3:
                    return BaseTestCase.State.class;

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
            BaseTestCase testCase = data.get(rowIndex);
            switch (columnIndex) {
                case 1:
                    testCase.setEnabled((Boolean) aValue);
                    break;

                case 3:
                    testCase.setState((BaseTestCase.State) aValue);
                    break;
            }

            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}
