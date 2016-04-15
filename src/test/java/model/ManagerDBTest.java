package model;

import controller.Controller;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import static model.tests.BaseTestCase.*;
import static org.mockito.Mockito.when;

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

    @Before
    public void setUp() throws Exception {
        when(mockController.getPathToDatabase()).thenReturn(MOCK_URL);
    }

    @After
    public void tearDown() throws Exception {
        managerDB.destroy();
    }

    @Test
    public void testConnectToWrongDBFile() {
        // Incorrect database URL or user disallowed URL selection
        when(mockController.getPathToDatabase()).thenReturn("");

        assertFalse(managerDB.connect(""));
        assertFalse(managerDB.connect(null));
    }

    @Test
    public void testConnectToSelectedDBFile() {
        // Correct database URL
        when(mockController.getPathToDatabase()).thenReturn(MOCK_URL);

        // Try connect to incorrect database URL, then select correct
        assertTrue(managerDB.connect("C:/sample.db"));
    }

    @Test
    public void testConnectToDBFile() {
        assertTrue(managerDB.connect());
    }

    @Test
    public void testDisconnectFromDB() throws Exception {
        assertTrue(managerDB.connect(MOCK_URL));
        assertTrue(managerDB.disconnect());
    }

    @Test
    public void testInsertReceiverToTable() throws Exception {
        managerDB.connect(MOCK_URL);
        insertReceiverRow();
    }

    private Receiver insertReceiverRow() throws Exception {
        Receiver receiver = createReceiver();

        assertTrue(managerDB.insert(receiver));

        return receiver;
    }

    private Receiver createReceiver() {
        return new Receiver(RECEIVER_ID, RECEIVER_MODEL, RECEIVER_SCHEME, RECEIVER_FIRMWARE);
    }

    @Test
    public void testInsertSessionToTable() throws Exception {
        managerDB.connect(MOCK_URL);
        Receiver receiver = insertReceiverRow();

        insertSessionRow(receiver, createTestCasesResults());
    }

    private void insertSessionRow(Receiver receiver, List<List<Integer>> testCases) throws Exception {
        assertTrue(managerDB.insert(
                receiver.getID(),
                testCases.get(0).toArray(),    // passed tests array
                testCases.get(1).toArray(),    // failed tests array
                testCases.get(2).toArray())    // skipped tests array
        );
    }

    private List<List<Integer>> createTestCasesResults() {
        List<List<Integer>> testCases = new ArrayList<>();
        testCases.add(new ArrayList<Integer>());   // passed
        testCases.add(new ArrayList<Integer>());   // failed
        testCases.add(new ArrayList<Integer>());   // skipped

        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            testCases.get(random.nextInt(3)).add(i);
        }

        return testCases;
    }

    @Test
    public void testSelectFromTable() throws Exception {
        managerDB.connect(MOCK_URL);

        List<List<Integer>> testCasesList = createTestCasesResults();

        String afterDate = dateFormat.format(new GregorianCalendar(2000, 1, 1).getTime());
        String beforeDate = dateFormat.format(new GregorianCalendar(2030, 1, 1).getTime());

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

    private boolean assertSelect(Receiver receiver, String afterDate, String beforeDate, List<List<Integer>> testCasesList) {
        ResultSet select = managerDB.select(receiver, afterDate, beforeDate);

        try {
            assertNotNull(select);
            select.next();

            if (receiver != null) {
                if (receiver.getID() != null)
                    assertEquals(receiver.getID().intValue(), select.getInt(1));

                if (receiver.getModel() != null)
                    assertEquals(receiver.getModel(), select.getString(2));

                if (receiver.getScheme() != null)
                    assertEquals(receiver.getScheme(), select.getString(3));

                if (receiver.getFirmware() != null)
                    assertEquals(receiver.getFirmware(), select.getString(4));
            }

            if (afterDate != null) {
                assertTrue(select.getTimestamp(5).after(dateFormat.parse(afterDate)));
            }

            if (beforeDate != null) {
                assertTrue(select.getTimestamp(5).before(dateFormat.parse(beforeDate)));
            }

            assertArrayEquals(testCasesList.get(0).toArray(), (Object[]) select.getObject(6));
            assertArrayEquals(testCasesList.get(1).toArray(), (Object[]) select.getObject(7));
            assertArrayEquals(testCasesList.get(2).toArray(), (Object[]) select.getObject(8));
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}