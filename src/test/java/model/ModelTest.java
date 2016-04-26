package model;

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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Test class for model
 * <p/>
 * !!! TESTING ONLY ON REAL DEVICE !!!
 */
public class ModelTest {
    private static final String TEST_PORTNAME = UARTTest.COM_PORTNAME;

    private Controller controller;
    private Model model;

    @Before
    public void setUp() throws Exception {
        controller = createMockController();
        model = new Model(controller);

        model.connectToDevice(TEST_PORTNAME);
    }

    static Controller createMockController() {
        Controller mockController = mock(Controller.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                String text = invocationOnMock.getArgumentAt(0, String.class);

                System.out.println("### MOCK CONTROLLER ###\n" + text);
                System.out.flush();
                return null;
            }
        }).when(mockController).updateLog(anyString(), (SimpleAttributeSet) anyObject());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                String title = invocationOnMock.getArgumentAt(0, String.class);
                String text = invocationOnMock.getArgumentAt(1, String.class);
                Exception exception = invocationOnMock.getArgumentAt(2, Exception.class);

                System.out.println("Mock Controller: " + title + "\n" + text + "\n" + exception.getLocalizedMessage());
                System.out.flush();
                return null;
            }
        }).when(mockController).showErrorMessage(anyString(), anyString(), (Exception) anyObject());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                String title = invocationOnMock.getArgumentAt(0, String.class);
                String text = invocationOnMock.getArgumentAt(1, String.class);

                System.out.println("Mock Controller: " + title + "\n" + text);
                System.out.flush();
                return null;
            }
        }).when(mockController).showMessage(anyString(), anyString());


        return mockController;
    }

    @After
    public void tearDown() throws Exception {
        model.disconnectFromDevice();
    }

    @Test
    public void testGetReceiver() throws Exception {
        assertNotNull(model.getReceiver());

        model.disconnectFromDevice();

        assertNull(model.getReceiver());
    }

    @Test
    public void testGetStand() throws Exception {
        assertNotNull(model.getStand());

        model.disconnectFromDevice();

        assertNull(model.getStand());
    }

    @Test
    public void testGetAvailableCOMPorts() throws Exception {
        assertTrue(model.getAvailableCOMPorts().size() > 0);
    }

    @Test
    public void testConnectToDevice() throws Exception {
        assertTrue(model.isReceiverConnected());
        assertTrue(model.isStandConnected());
    }

    @Test
    public void testDisconnectFromDevice() throws Exception {
        assertTrue(model.isReceiverConnected());
        assertTrue(model.isStandConnected());

        model.disconnectFromDevice();
        assertFalse(model.isReceiverConnected());
        assertFalse(model.isStandConnected());
    }

    @Test
    public void testIsReceiverConnected() throws Exception {
        assertEquals(model.getReceiver().getConnectionStatus() == ConnectionStatus.CONNECTED, model.isReceiverConnected());
    }

    @Test
    public void testIsStandConnected() throws Exception {
        assertEquals(model.getStand().getConnectionStatus() == ConnectionStatus.CONNECTED, model.isStandConnected());
    }

    @Test
    public void testTestCaseExecute() throws Exception {
        assertFalse(model.isTestRunning());
        model.startTesting();
        assertTrue(model.isTestRunning());
        model.stopTesting();
        assertFalse(model.isTestRunning());
    }
}