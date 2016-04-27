package model.tests;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import controller.Controller;
import view.LogPanel;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static model.tests.TestManager.State.*;

/**
 * The test manager class is used to control test cases
 */
public class TestManager {

    private final static List<? extends BaseTestCase> ALL_TEST_CASES = Collections.unmodifiableList(Arrays.asList(
            new AFEqualSignalLevelsTest(50, 20, 10.0),
            new AFEqualSignalLevelsTest(60, 20, 15.0),
            new AFEqualSignalLevelsTest(100, 20, 10.0),
            new AFEqualSignalLevelsTest(120, 20, 15.0),
            new AFEqualSignalLevelsTest(512, 20, 10.0),
            new AFEqualSignalLevelsTest(1024, 20, 10.0),
            new AFEqualSignalLevelsTest(8192, 20, 10.0),
            new AFEqualSignalLevelsTest(32768, 20, 10.0),

            new AFValidFilterBandpassTest(50, 20),
            new AFValidFilterBandpassTest(100, 20),
            new AFValidFilterBandpassTest(512, 20),
            new AFValidFilterBandpassTest(1024, 20),
            new AFValidFilterBandpassTest(8192, 20),
            new AFValidFilterBandpassTest(32768, 20),

            new ScalingAmplifierTest(),
            new WideBandTest(),
            new ExternalSensorsTest(),
            new TwoFrequencyTest()
    ));
    private static final ThreadFactory TESTING_EXECUTOR = new ThreadFactoryBuilder().setNameFormat("TestingExecutor-%d").setDaemon(true).build();
    private final Controller controller;

    /* List of test cases, used by this test manager */
    private List<BaseTestCase> testList = new ArrayList<>();
    /* Map of test results: K - test case ID, V - test result state */
    private Map<Integer, State> testResults = new TreeMap<>();
    private boolean testRunning = false;
    private long executionTimeMs;
    private ExecutorService testingExecutor;

    public TestManager(Controller controller) {
        this.controller = controller;
    }

    public void fillTestList() {
        testList.clear();
        testList.addAll(ALL_TEST_CASES);
    }

    public List<BaseTestCase> getTestList() {
        return testList;
    }

    public Map<Integer, State> getTestResults() {
        return testResults;
    }

    public boolean isTestRunning() {
        return testRunning;
    }

    private void setTestRunning(boolean isRun) {
        if (isRun) {
            executionTimeMs = System.currentTimeMillis();
        } else {
            executionTimeMs = System.currentTimeMillis() - executionTimeMs;
        }
        this.testRunning = isRun;
    }

    public void start() {

        setTestRunning(true);
        clearTestResultStates();

        controller.updateLog("\n================================" +
                "\n=======   START TESTING  =======" +
                "\n================================", LogPanel.BOLD);

        testingExecutor = Executors.newSingleThreadExecutor(TESTING_EXECUTOR);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (final BaseTestCase testCase : testList) {
                    testingExecutor.submit(new TestWorker(testCase));
                }
                testingExecutor.shutdown();

                // Wait while all tests done
                testingExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                return null;
            }

            @Override
            protected void done() {
                controller.stopTesting();
                controller.insertResultToDB();
            }
        }.execute();
    }

    private void clearTestResultStates() {
        for (BaseTestCase testCase : testList)
            testResults.put(testCase.getId(), null);
    }

    public void stop() {
        try {
            testingExecutor.shutdownNow();

            // Wait while interrupted test canceled
            testingExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException ignored) {
        } finally {
            setTestRunning(false);

            controller.updateLog("\n================================" +
                    "\n=======   STOP TESTING   =======" +
                    "\n================================", LogPanel.BOLD);
            printTestResults(executionTimeMs);
        }
    }

    private void printTestResults(long executionTimeMs) {
        Collection<State> testStates = testResults.values();
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss,SS");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        controller.updateLog("Tests results:", LogPanel.BOLD);
        controller.updateLog(String.format("%-10s%d", "Passed: ", Collections.frequency(testStates, PASS)), LogPanel.BOLD, LogPanel.GREEN);
        controller.updateLog(String.format("%-10s%d", "Failed: ", Collections.frequency(testStates, FAIL)), LogPanel.BOLD, LogPanel.RED);
        controller.updateLog(String.format("%-10s%d", "Skipped: ", Collections.frequency(testStates, SKIP)), LogPanel.BOLD);
        controller.updateLog(String.format("Total time: %s", sdf.format(new Date(executionTimeMs))), LogPanel.BOLD);
    }

    public Integer[] getTestIDs(State state) {
        ArrayList<Integer> list = new ArrayList<>();
        for (Map.Entry<Integer, State> pair : testResults.entrySet()) {
            if (pair.getValue() != null && pair.getValue().equals(state))
                list.add(pair.getKey());
        }

        return list.toArray(new Integer[]{});
    }

    public enum State {
        PASS, FAIL, SKIP, ABORT, RUN
    }

    private class TestWorker extends SwingWorker<Void, State> {

        private final BaseTestCase testCase;
        private Integer id;
        private String name;
        private Throwable error;

        TestWorker(BaseTestCase testCase) {
            this.testCase = testCase;

            id = testCase.getId();
            name = testCase.getName();
        }


        @Override
        protected Void doInBackground() throws Exception {
            State resultState = RUN;
            try {
                if (testCase.isEnabled()) {
                    publish(resultState);
                    testCase.runTest(controller.getReceiver(), controller.getStand());
                    resultState = PASS;
                } else {
                    resultState = SKIP;
                }
            } catch (InterruptedException interruptByUser) {
                resultState = ABORT;
            } catch (Error | Exception error) {
                resultState = FAIL;
                this.error = error;
            }
            publish(resultState);
            return null;
        }

        @Override
        protected void process(List<State> chunks) {
            State state = chunks.get(0);

            testResults.put(id, state);

            switch (state) {
                case RUN:
                    controller.updateLog(String.format("#%d %s test is running...", id, name));
                    break;
                case PASS:
                    controller.updateLog(String.format("#%d %s test is passed.", id, name), LogPanel.NORMAL, LogPanel.GREEN);
                    break;
                case FAIL:
                    controller.updateLog(String.format("#%d %s test is failed.", id, name), LogPanel.NORMAL, LogPanel.RED);

                    controller.updateLog("ERROR: " + error.getLocalizedMessage(), LogPanel.BOLD, LogPanel.RED);
                    if (error.getCause() != null)
                        controller.updateLog("CAUSE: " + error.getCause().getLocalizedMessage(), LogPanel.BOLD, LogPanel.RED);
                    break;
                case SKIP:
                    controller.updateLog(String.format("#%d %s is skipped", id, name));
                    break;

                case ABORT:
                    controller.updateLog("Testing aborted by user", LogPanel.BOLD, LogPanel.RED);
                    break;
            }

            // TODO: now we update ALL table, think how optimize it's function to update only changed row
            controller.updateTestList();
        }
    }
}
