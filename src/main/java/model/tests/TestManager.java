package model.tests;

import controller.Controller;
import model.Receiver;
import model.Stand;
import org.apache.commons.lang3.time.DurationFormatUtils;
import view.LogPanel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static model.tests.BaseTestCase.State.FAIL;
import static model.tests.BaseTestCase.State.PASS;

/**
 * The test manager class is used to control test cases
 */
public class TestManager {

    private final Controller controller;
    private List<BaseTestCase> testsList;
    private boolean testRunning = false;
    private long testsTimeMs;


    public TestManager(Controller controller, Receiver receiver, Stand stand) {
        this.controller = controller;
        testsList = new ArrayList<>();
        testsList.add(new AFEqualSignalLevelsTest(50, 20, receiver, stand));
        testsList.add(new AFEqualSignalLevelsTest(60, 20, receiver, stand));
        testsList.add(new AFEqualSignalLevelsTest(100, 20, receiver, stand));
        testsList.add(new AFEqualSignalLevelsTest(120, 20, receiver, stand));
        testsList.add(new AFEqualSignalLevelsTest(512, 20, receiver, stand));
        testsList.add(new AFEqualSignalLevelsTest(1024, 20, receiver, stand));
        testsList.add(new AFEqualSignalLevelsTest(8192, 20, receiver, stand));
        testsList.add(new AFEqualSignalLevelsTest(32768, 20, receiver, stand));

        testsList.add(new AFValidFilterBandpassTest(50, 20, receiver, stand));
        testsList.add(new AFValidFilterBandpassTest(100, 20, receiver, stand));
        testsList.add(new AFValidFilterBandpassTest(512, 20, receiver, stand));
        testsList.add(new AFValidFilterBandpassTest(1024, 20, receiver, stand));
        testsList.add(new AFValidFilterBandpassTest(8192, 20, receiver, stand));
        testsList.add(new AFValidFilterBandpassTest(32768, 20, receiver, stand));

        testsList.add(new ScalingAmplifierTest(receiver, stand));

        testsList.add(new WideBandTest(receiver, stand));

        testsList.add(new ExternalSensorsTest(receiver, stand));

        testsList.add(new TwoFrequencyTest(receiver, stand));
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Tests results:\n");

        int testPassed = 0,
                testFailed = 0,
                testSkipped = 0;

        for (BaseTestCase test : testsList) {
            switch (test.getState()) {
                case PASS:
                    testPassed++;
                    break;
                case FAIL:
                    testFailed++;
                    break;
                case READY:
                    testSkipped++;
                    break;
            }
        }

        builder.append(String.format("%-10s%d%n", "Passed: ", testPassed))
                .append(String.format("%-10s%d%n", "Failed: ", testFailed))
                .append(String.format("%-10s%d%n", "Skipped: ", testSkipped))
                .append(String.format("\nTotal time: %s", DurationFormatUtils.formatDuration(testsTimeMs, "HH:mm:ss,SSS")));

        return builder.toString();
    }

    public void startTests() {

        long startTestsTime = new Date().getTime();

        for (int i = 1; i <= testsList.size(); i++) {
            BaseTestCase testCase = testsList.get(i - 1);

            if (!isTestRunning())
                break;

            if (!testCase.isEnabled())
                continue;

            controller.updateLog(String.format("Test #%d %s is running...", i, testCase.getName()));
            try {
                testCase.runTest();
                testCase.setState(PASS);
            } catch (Error | Exception error) {
                if (error instanceof InterruptedException)
                    setTestRunning(false);

                String causeMessage = error.getCause() != null ? "\nCause:" + error.getCause().getLocalizedMessage() : "";
                controller.updateLog("ERROR: " + error.getLocalizedMessage() + causeMessage, LogPanel.BOLD, LogPanel.RED);
                testCase.setState(FAIL);
            } finally {
                controller.updateLog(String.format("Test #%d %s is %s.", i, testCase.getName(), (testCase.getState() == PASS) ? "passed" : "failed"),
                        LogPanel.NORMAL, (testCase.getState() == PASS) ? LogPanel.GREEN : LogPanel.RED);
                controller.updateTestList();

                testsTimeMs = new Date().getTime() - startTestsTime;
            }
        }
    }

    public boolean isTestRunning() {
        return testRunning;
    }

    public void setTestRunning(boolean testRunning) {
        this.testRunning = testRunning;
    }

    public List<BaseTestCase> getTestsList() {
        return testsList;
    }
}
