package model;

import model.tests.BaseTestCase;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for test cases
 */
public class TestCaseTest {

    private final String name = "sample test case";

    private BaseTestCase testCase = new BaseTestCase(name) {
        @Override
        public void runTest(Receiver receiver, Stand stand) throws Error, Exception {
        }
    };

    @Test
    public void testCreateTC() throws Exception {
        assertNotNull(testCase);
        assertEquals(name, testCase.getName());
    }

    @Test
    public void testEnable() throws Exception {
        testCase.setEnabled(true);
        assertTrue(testCase.isEnabled());
    }
}