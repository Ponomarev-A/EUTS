package model;

import controller.Controller;
import model.tests.BaseTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import static model.tests.BaseTestCase.*;
import static model.tests.BaseTestCase.State.values;
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
        Receiver receiver = new Receiver();
        receiver.setID(RECEIVER_ID);
        receiver.setModel(RECEIVER_MODEL);
        receiver.setScheme(RECEIVER_SCHEME);
        receiver.setFirmware(RECEIVER_FIRMWARE);
        return receiver;
    }

    @Test
    public void testInsertSessionToTable() throws Exception {
        managerDB.connect(MOCK_URL);
        Receiver receiver = insertReceiverRow();

        insertSessionRow(new Timestamp(System.currentTimeMillis()), receiver, createTestCasesList());
    }

    private void insertSessionRow(Timestamp timestamp, Receiver receiver, List<List<Integer>> testCases) throws Exception {
        assertTrue(managerDB.insert(
                receiver.getID(),
                timestamp,
                testCases.get(0).toArray(),    // passed tests array
                testCases.get(1).toArray(),    // failed tests array
                testCases.get(2).toArray())    // skipped tests array
        );
    }

    private List<List<Integer>> createTestCasesList() {
        List<List<Integer>> testStates = new ArrayList<>();
        testStates.add(new ArrayList<Integer>());   // passed
        testStates.add(new ArrayList<Integer>());   // failed
        testStates.add(new ArrayList<Integer>());   // skipped

        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            BaseTestCase testCase = new BaseTestCase("test #" + i) {
                @Override
                public void runTest(Receiver receiver, Stand stand) throws Error, Exception {
                }
            };

            int ordinal = random.nextInt(3);
            State state = values()[ordinal];
            testCase.setState(state);
            testStates.get(ordinal).add(testCase.getId());
        }
        return testStates;
    }

    @Test
    public void testSelectFromTable() throws Exception {
        managerDB.connect(MOCK_URL);

        List<List<Integer>> testCasesList = createTestCasesList();

        Timestamp timestamp = new Timestamp(new GregorianCalendar(2011, 1, 1).getTimeInMillis());
        Timestamp afterTimestamp = new Timestamp(new GregorianCalendar(2010, 1, 1).getTimeInMillis());
        Timestamp beforeTimestamp = new Timestamp(new GregorianCalendar(2012, 1, 1).getTimeInMillis());

        Receiver receiver = insertReceiverRow();
        insertSessionRow(timestamp, receiver, testCasesList);

        assertTrue(assertSelect(receiver, afterTimestamp, beforeTimestamp, testCasesList));
        assertTrue(assertSelect(new Receiver(), afterTimestamp, beforeTimestamp, testCasesList));
        assertTrue(assertSelect(receiver, null, beforeTimestamp, testCasesList));
        assertTrue(assertSelect(receiver, afterTimestamp, null, testCasesList));
        assertTrue(assertSelect(null, null, null, testCasesList));

        Receiver wrongReceiver = new Receiver();
        wrongReceiver.setID(123);
        Timestamp wrongAfterTimestamp = new Timestamp(new GregorianCalendar(2020, 1, 1).getTimeInMillis());
        Timestamp wrongBeforeTimestamp = new Timestamp(new GregorianCalendar(2000, 1, 1).getTimeInMillis());

        assertFalse(assertSelect(wrongReceiver, afterTimestamp, beforeTimestamp, testCasesList));
        assertFalse(assertSelect(receiver, wrongAfterTimestamp, beforeTimestamp, testCasesList));
        assertFalse(assertSelect(receiver, afterTimestamp, wrongBeforeTimestamp, testCasesList));
    }

    private boolean assertSelect(Receiver receiver, Timestamp afterTimestamp, Timestamp beforeTimestamp, List<List<Integer>> testCasesList) {
        ResultSet select = managerDB.select(receiver, afterTimestamp, beforeTimestamp);

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

            if (afterTimestamp != null) {
                assertTrue(select.getTimestamp(5).after(afterTimestamp));
            }

            if (beforeTimestamp != null) {
                assertTrue(select.getTimestamp(5).before(beforeTimestamp));
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