package model;

import controller.Controller;
import org.junit.After;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import static model.tests.BaseTestCase.*;

/**
 * Test database manager class
 */
public class ManagerDBTest {

    private static final String MOCK_URL = "./database/mockHistory";
    private static final Integer RECEIVER_ID = 12345;
    private static final String RECEIVER_MODEL = "AP019.1";
    private static final String RECEIVER_SCHEME = "AP019.01.020izm11";
    private static final String RECEIVER_FIRMWARE = "3.07";
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Controller mockController = ModelTest.createMockController();
    private ManagerDB managerDB = new ManagerDB(mockController);


    @After
    public void tearDown() throws Exception {
        managerDB.destroy();
    }

    @Test
    public void testConnectToIncorrectURL() {
        managerDB.connect("");
        managerDB.connect(null);
        managerDB.connect("C:/incorrectURL");
    }

    @Test
    public void testConnectToDefaultURL() {
        managerDB.connectToDefaultURL();
    }

    @Test
    public void testDisconnectFromDB() throws Exception {
        managerDB.connect(MOCK_URL);
        managerDB.disconnect();
    }

    @Test
    public void testInsertReceiverToTable() throws Exception {
        managerDB.connect(MOCK_URL);
        insertReceiverRow();
    }

    private Receiver insertReceiverRow() throws Exception {
        Receiver receiver = createReceiver();

        assertEquals(1, managerDB.insert(receiver, null, null));

        return receiver;
    }

    private Receiver createReceiver() {
        return new Receiver(RECEIVER_ID, RECEIVER_MODEL, RECEIVER_SCHEME, RECEIVER_FIRMWARE);
    }

    @Test
    public void testInsertReceiverCoeffsToTable() throws Exception {
        managerDB.connect(MOCK_URL);

        // Start conditions
        Receiver receiver = createReceiver();
        Float[] depthCoeffs = new Float[]{0.01f, 0.02f, 1.123f};
        Float[] currentCoeffs = new Float[]{1.221f, -10.02f, 51.788998f};

        // Insert row to tables
        assertEquals(1, managerDB.insert(receiver, depthCoeffs, currentCoeffs));
        insertSessionRow(receiver, createTestCasesResults());

        // Select calibration coeffs from Receiver table
        ResultSet resultSet = managerDB.select(receiver);
        resultSet.next();

        assertNotNull(resultSet);
        assertArrayEquals(depthCoeffs, (Object[]) resultSet.getObject(1));
        assertArrayEquals(currentCoeffs, (Object[]) resultSet.getObject(2));
    }

    private void insertSessionRow(Receiver receiver, List<Integer[]> testCases) throws Exception {
        assertEquals(1, managerDB.insert(
                receiver.getID(),
                testCases.get(0),    // passed tests array
                testCases.get(1),    // failed tests array
                testCases.get(2))    // skipped tests array
        );
    }

    private List<Integer[]> createTestCasesResults() {
        List<List<Integer>> testCases = new ArrayList<>();
        testCases.add(new ArrayList<Integer>());   // passed
        testCases.add(new ArrayList<Integer>());   // failed
        testCases.add(new ArrayList<Integer>());   // skipped

        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            testCases.get(random.nextInt(3)).add(i);
        }
        ArrayList<Integer[]> result = new ArrayList<>();
        result.add(testCases.get(0).toArray(new Integer[]{}));
        result.add(testCases.get(1).toArray(new Integer[]{}));
        result.add(testCases.get(2).toArray(new Integer[]{}));
        return result;
    }

    @Test
    public void testInsertSessionToTable() throws Exception {
        managerDB.connect(MOCK_URL);
        Receiver receiver = insertReceiverRow();


        List<Integer[]> testCasesResults = createTestCasesResults();
        insertSessionRow(receiver, testCasesResults);

        testCasesResults.set(0, new Integer[0]);
        insertSessionRow(receiver, testCasesResults);

        testCasesResults.set(0, null);
        insertSessionRow(receiver, testCasesResults);
    }

    @Test
    public void testUpdateReceiverToTable() throws Exception {
        managerDB.connect(MOCK_URL);

        Receiver receiver = createReceiver();
        Float[] depthCoeffs = new Float[]{0.01f, 0.02f, 1.123f};
        Float[] currentCoeffs = new Float[]{1.221f, -10.02f, 51.788998f};

        assertEquals(1, managerDB.insert(receiver, depthCoeffs, currentCoeffs));

        // Change coeffs
        depthCoeffs[0] *= 10;

        assertEquals(1, managerDB.update(receiver, depthCoeffs, currentCoeffs));
        ResultSet rs = managerDB.select(receiver);
        rs.next();

        assertArrayEquals(depthCoeffs, (Object[]) rs.getObject(1));
        assertArrayEquals(currentCoeffs, (Object[]) rs.getObject(2));
    }

    @Test
    public void testSelectFromTable() throws Exception {
        managerDB.connect(MOCK_URL);

        List<Integer[]> testCasesList = createTestCasesResults();

        String afterDate = dateFormat.format(new GregorianCalendar(2000, 1, 1).getTime());
        String beforeDate = dateFormat.format(new GregorianCalendar(2020, 1, 1).getTime());

        Receiver receiver = insertReceiverRow();
        insertSessionRow(receiver, testCasesList);

        assertTrue(assertSelect(receiver, afterDate, beforeDate, testCasesList));
        assertTrue(assertSelect(new Receiver(), afterDate, beforeDate, testCasesList));
        assertTrue(assertSelect(receiver, null, beforeDate, testCasesList));
        assertTrue(assertSelect(receiver, afterDate, null, testCasesList));
        assertTrue(assertSelect(null, null, null, testCasesList));

        Receiver wrongReceiver = new Receiver(123, null, null, null);
        String wrongAfterDate = dateFormat.format(new GregorianCalendar(2020, 1, 1).getTime());
        String wrongBeforeDate = dateFormat.format(new GregorianCalendar(2000, 1, 1).getTime());

        assertFalse(assertSelect(wrongReceiver, afterDate, beforeDate, testCasesList));
        assertFalse(assertSelect(receiver, wrongAfterDate, beforeDate, testCasesList));
        assertFalse(assertSelect(receiver, afterDate, wrongBeforeDate, testCasesList));
    }

    private boolean assertSelect(Receiver receiver, String afterDate, String beforeDate, List<Integer[]> testCasesList) throws SQLException {
        ResultSet rs = managerDB.select(receiver, afterDate, beforeDate);

        try {
            assertNotNull(rs);
            rs.next();

            if (receiver != null) {
                if (receiver.getID() != null)
                    assertEquals(receiver.getID().intValue(), rs.getInt(1));

                if (receiver.getModel() != null)
                    assertEquals(receiver.getModel(), rs.getString(2));

                if (receiver.getScheme() != null)
                    assertEquals(receiver.getScheme(), rs.getString(3));

                if (receiver.getFirmware() != null)
                    assertEquals(receiver.getFirmware(), rs.getString(4));
            }

            if (afterDate != null) {
                assertTrue(rs.getTimestamp(5).after(dateFormat.parse(afterDate)));
            }

            if (beforeDate != null) {
                assertTrue(rs.getTimestamp(5).before(dateFormat.parse(beforeDate)));
            }

            assertArrayEquals(testCasesList.get(0), (Object[]) rs.getObject(6));
            assertArrayEquals(testCasesList.get(1), (Object[]) rs.getObject(7));
            assertArrayEquals(testCasesList.get(2), (Object[]) rs.getObject(8));
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}