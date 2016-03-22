package model;

import connections.UART;
import connections.UARTTest;
import controller.Controller;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.swing.text.SimpleAttributeSet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for model
 * <p/>
 * !!! TESTING ONLY ON REAL DEVICE !!!
 */
public class ModelTest {

    //    public static final String TEST_PORTNAME = Model.DEFAULT_PORTNAME;
    private static final String TEST_PORTNAME = UARTTest.COM_PORTNAME;

    private Controller controller;
    private Model model;

    @Before
    public void setUp() throws Exception {
        controller = createMockController();
        model = new Model(controller);
    }

    static Controller createMockController() {
        Controller mockController = mock(Controller.class);

        doNothing().when(mockController).updateLog(anyString(), (SimpleAttributeSet) anyObject());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                String title = invocationOnMock.getArgumentAt(0, String.class);
                Exception exception = invocationOnMock.getArgumentAt(1, Exception.class);

                System.out.println("Mock Controller: " + title + "\n" + exception.getLocalizedMessage());
                System.out.flush();

                return null;
            }
        }).when(mockController).showErrorMessage(anyString(), anyString(), (Exception) anyObject());

        return mockController;
    }

    @After
    public void tearDown() throws Exception {
        model.destroyConnectionManager();
    }

    @Test
    public void testInitByDefaultPortName() throws Exception {
        assertNull(model.getConnectionManager());
        model.init();
        assertNotNull(model.getConnectionManager());

        assertNotNull(model.getConnectionManager().getConnection());
        assertTrue(model.getConnectionManager().getConnection() instanceof UART);
        assertFalse(model.getConnectionManager().getConnection().isOpened());

        assertEquals(Model.DEFAULT_PORTNAME, ((UART) model.getConnectionManager().getConnection()).getSerialPort().getPortName());
    }


    @Test
    public void testGetConnectionManager() throws Exception {
        assertNull(model.getConnectionManager());
        model.createConnectionManager(TEST_PORTNAME);
        assertNotNull(model.getConnectionManager());
    }

    @Test
    public void testGetReceiver() throws Exception {
        assertNull(model.getReceiver());
        model.createConnectionManager(TEST_PORTNAME);
        assertNotNull(model.getReceiver());
    }

    @Test
    public void testGetStand() throws Exception {
        assertNull(model.getStand());
        model.createConnectionManager(TEST_PORTNAME);
        assertNotNull(model.getStand());
    }

    @Test
    public void testGetAvailableCOMPorts() throws Exception {
        assertTrue(model.getAvailableCOMPorts().length > 0);
    }

    @Test
    public void testCreateConnectionManager() throws Exception {
        assertNull(model.getConnectionManager());

        model.createConnectionManager(TEST_PORTNAME);

        assertNotNull(model.getConnectionManager());
        assertNotNull(model.getStand());
        assertNotNull(model.getReceiver());
        assertNotNull(model.getTestsList());
    }

    @Test
    public void testDestroyConnectionManager() throws Exception {
        model.createConnectionManager(TEST_PORTNAME);
        model.destroyConnectionManager();

        assertNull(model.getConnectionManager());
        assertNull(model.getReceiver());
        assertNull(model.getStand());
    }

    @Test
    public void testConnectToDevice() throws Exception {
        model.createConnectionManager(TEST_PORTNAME);
        model.connectToDevice();

        assertTrue(model.isReceiverConnected());
        assertTrue(model.isStandConnected());

        model.disconnectFromDevice();
    }

    @Test
    public void testDisconnectFromDevice() throws Exception {
        model.createConnectionManager(TEST_PORTNAME);
        model.connectToDevice();

        assertTrue(model.isReceiverConnected());
        assertTrue(model.isStandConnected());

        model.disconnectFromDevice();
        assertFalse(model.isReceiverConnected());
        assertFalse(model.isStandConnected());
    }

    @Test
    public void testIsCOMPortSelected() throws Exception {
        model.createConnectionManager(TEST_PORTNAME);
        assertTrue(model.isCOMPortSelected(TEST_PORTNAME));

        model.destroyConnectionManager();
        assertFalse(model.isCOMPortSelected(TEST_PORTNAME));
    }

    @Test
    public void testIsReceiverConnected() throws Exception {
        model.createConnectionManager(TEST_PORTNAME);
        model.connectToDevice();

        assertEquals(model.getReceiver().getConnectionStatus() == ConnectionStatus.CONNECTED, model.isReceiverConnected());

        model.disconnectFromDevice();
    }

    @Test
    public void testIsStandConnected() throws Exception {
        model.createConnectionManager(TEST_PORTNAME);
        model.connectToDevice();

        assertEquals(model.getStand().getConnectionStatus() == ConnectionStatus.CONNECTED, model.isStandConnected());

        model.disconnectFromDevice();
    }

    @Test
    public void testTestCaseExecute() throws Exception {
        model.createConnectionManager(TEST_PORTNAME);
        model.connectToDevice();

        assertFalse(model.isTestRunning());
        model.startTesting();
        assertTrue(model.isTestRunning());
        model.stopTesting();
        assertFalse(model.isTestRunning());

        model.disconnectFromDevice();
    }

    @Test
    public void testGetTestsList() throws Exception {
        model.createConnectionManager(TEST_PORTNAME);
        model.connectToDevice();

        assertNotNull(model.getTestsList());

        model.disconnectFromDevice();
    }
}