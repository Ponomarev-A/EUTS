package model;

import model.tests.BaseTestCase;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for test cases
 */
public class TestCaseTest {

    private final String name = "sample test case";

    private BaseTestCase testCase = new BaseTestCase(name, null, null) {
        @Override
        public boolean setUp() {
            return true;
        }

        @Override
        public void runTest() {
        }
    };

    @Test
    public void testCreateTC() throws Exception {
        assertNotNull(testCase);
        assertEquals(name, testCase.getName());
    }

    @Test
    public void testStates() throws Exception {
        testCase.setState(BaseTestCase.State.READY);
        assertEquals(BaseTestCase.State.READY, testCase.getState());
    }

    @Test
    public void testEnable() throws Exception {
        testCase.setEnabled(true);
        assertTrue(testCase.isEnabled());
    }
}