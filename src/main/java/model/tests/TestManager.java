package model.tests;

import controller.Controller;
import model.Receiver;
import model.Stand;
import view.LogPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * The test manager class is used to control test cases
 */
public class TestManager {

    private final Controller controller;
    private List<BaseTestCase> testsList;

    public TestManager(Controller controller, Receiver receiver, Stand stand) {
        this.controller = controller;
        testsList = new ArrayList<>();
        testsList.add(new AnalogFilterTest(50,    20, receiver, stand));
        testsList.add(new AnalogFilterTest(60,    20, receiver, stand));
        testsList.add(new AnalogFilterTest(100,   20, receiver, stand));
        testsList.add(new AnalogFilterTest(120,   20, receiver, stand));
        testsList.add(new AnalogFilterTest(512,   20, receiver, stand));
        testsList.add(new AnalogFilterTest(1024,  20, receiver, stand));
        testsList.add(new AnalogFilterTest(8192,  20, receiver, stand));
        testsList.add(new AnalogFilterTest(32768, 20, receiver, stand));
    }


    public void startTests() {

        for (int i = 1; i <= testsList.size(); i++) {
            BaseTestCase testCase = testsList.get(i - 1);

            if (testCase.isEnabled()) {
                controller.updateLog(String.format("Test #%d %s is running...", i, testCase.getName()));
                try {
                    testCase.runTest();
                    testCase.setState(BaseTestCase.State.PASS);
                } catch (Error | Exception error) {
                    testCase.setState(BaseTestCase.State.FAIL);
                    controller.updateLog("ERROR: " + error.getLocalizedMessage(), LogPanel.BOLD);
                } finally {
                    controller.updateLog(String.format("Test #%d %s is %s.", i, testCase.getName(),
                            (testCase.getState() == BaseTestCase.State.PASS) ? "passed" : "failed"));
                }
            } else {
                controller.updateLog(String.format("Test #%d %s is disabled.", i, testCase.getName()));
            }

            controller.updateTestList();
        }
    }

    public List<BaseTestCase> getTestsList() {
        return testsList;
    }
}
