package model.tests;

import model.Receiver;
import model.Stand;

import java.util.concurrent.atomic.AtomicInteger;

import static model.tests.BaseTestCase.State.READY;

/**
 * Base class for Test cases
 */
public abstract class BaseTestCase extends org.junit.Assert {

    private static AtomicInteger nextID = new AtomicInteger(0);

    private final String name;

    private State state = READY;
    private boolean enabled = true;
    private Integer id;

    protected BaseTestCase(String name) {
        super();
        this.name = name;
        id = nextID.incrementAndGet();
    }

    public String getName() {
        return name;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public abstract void runTest(Receiver receiver, Stand stand) throws Error, Exception;

    public Integer getId() {
        return id;
    }

    public enum State {
        READY, PASS, FAIL
    }
}
