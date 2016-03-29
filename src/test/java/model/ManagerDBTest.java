package model;

import controller.Controller;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Test database manager class
 */
public class ManagerDBTest {

    private static final String MOCK_URL = "./database/mockHistory";

    private Controller mockController = ModelTest.createMockController();
    private Model model = new Model(mockController);
    private ManagerDB managerDB = new ManagerDB(model, mockController);

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
}