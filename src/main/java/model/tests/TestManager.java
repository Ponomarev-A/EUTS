package model.tests;

import model.Receiver;
import model.Stand;

import java.util.ArrayList;
import java.util.List;

/**
 * The test manager class is used to control test cases
 */
public class TestManager {

    private List<BaseTestCase> testsList;

    public TestManager(Receiver receiver, Stand stand) {
        testsList = new ArrayList<>();

        testsList.add(new Test1("Freq. 1024Hz ", receiver, stand));
    }


    public void startTests() throws Exception {

        for (BaseTestCase testCase : testsList) {
            if (testCase.setUp())
                testCase.runTest();
        }

    }

    public List<BaseTestCase> getTestsList() {
        return testsList;
    }
}
