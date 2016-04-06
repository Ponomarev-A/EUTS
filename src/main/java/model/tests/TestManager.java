package model.tests;

import controller.Controller;
import model.Receiver;
import model.Stand;
import org.apache.commons.lang3.time.DurationFormatUtils;
import view.LogPanel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static model.tests.BaseTestCase.State.*;

/**
 * The test manager class is used to control test cases
 */
public class TestManager {

    private final Controller controller;
    private final Receiver receiver;
    private final Stand stand;

    private List<BaseTestCase> testsList;
    private boolean testRunning = false;
    private long testsTimeMs;

    private List<BaseTestCase> passed = new ArrayList<>();
    private List<BaseTestCase> failed = new ArrayList<>();
    private List<BaseTestCase> skipped = new ArrayList<>();

    public TestManager(Controller controller, Receiver receiver, Stand stand) {
        this.controller = controller;
        this.receiver = receiver;
        this.stand = stand;

        testsList = new ArrayList<>();

        testsList.add(new AFEqualSignalLevelsTest(50, 20));
        testsList.add(new AFEqualSignalLevelsTest(60, 20));
        testsList.add(new AFEqualSignalLevelsTest(100, 20));
        testsList.add(new AFEqualSignalLevelsTest(120, 20));
        testsList.add(new AFEqualSignalLevelsTest(512, 20));
        testsList.add(new AFEqualSignalLevelsTest(1024, 20));
        testsList.add(new AFEqualSignalLevelsTest(8192, 20));
        testsList.add(new AFEqualSignalLevelsTest(32768, 20));

        testsList.add(new AFValidFilterBandpassTest(50, 20));
        testsList.add(new AFValidFilterBandpassTest(100, 20));
        testsList.add(new AFValidFilterBandpassTest(512, 20));
        testsList.add(new AFValidFilterBandpassTest(1024, 20));
        testsList.add(new AFValidFilterBandpassTest(8192, 20));
        testsList.add(new AFValidFilterBandpassTest(32768, 20));

        testsList.add(new ScalingAmplifierTest());
        testsList.add(new WideBandTest());
        testsList.add(new ExternalSensorsTest());
        testsList.add(new TwoFrequencyTest());
    }


    public List<BaseTestCase> getPassed() {
        return passed;
    }

    public List<BaseTestCase> getFailed() {
        return failed;
    }

    public List<BaseTestCase> getSkipped() {
        return skipped;
    }

    public List<BaseTestCase> getTestsList() {
        return testsList;
    }

    @Override
    public String toString() {
        String builder = "Tests results:\n" +
                String.format("%-10s%d%n", "Passed: ", passed.size()) +
                String.format("%-10s%d%n", "Failed: ", failed.size()) +
                String.format("%-10s%d%n", "Skipped: ", skipped.size()) +
                String.format("\nTotal time: %s", DurationFormatUtils.formatDuration(testsTimeMs, "HH:mm:ss,SSS"));

        return builder;
    }

    public void startTests() {

        long startTestsTime = new Date().getTime();
        passed.clear();
        failed.clear();
        skipped.clear();
        for (BaseTestCase test : testsList) {
            test.setState(READY);
        }

        for (int i = 1; i <= testsList.size(); i++) {
            BaseTestCase testCase = testsList.get(i - 1);

            if (!isTestRunning())
                break;

            if (!testCase.isEnabled()) {
                skipped.add(testCase);
                continue;
            }

            controller.updateLog(String.format("Test #%d %s is running...", i, testCase.getName()));
            try {
                testCase.runTest(receiver, stand);
                testCase.setState(PASS);
                passed.add(testCase);

            } catch (Error | Exception error) {
                if (error instanceof InterruptedException)
                    setTestRunning(false);

                String causeMessage = error.getCause() != null ? "\nCause: " + error.getCause().getLocalizedMessage() : "";
                controller.updateLog("ERROR: " + error.getLocalizedMessage() + causeMessage, LogPanel.BOLD, LogPanel.RED);
                testCase.setState(FAIL);
                failed.add(testCase);

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
}
