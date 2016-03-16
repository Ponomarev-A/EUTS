package model.tests;

import model.Receiver;
import model.Stand;

import static model.tests.BaseTestCase.State.READY;

/**
 * Base class for Test cases
 */
public abstract class BaseTestCase extends org.junit.Assert {

    final Receiver receiver;
    final Stand stand;

    private final String name;

    private State state = READY;
    private boolean enabled = true;

    public BaseTestCase(String name, Receiver receiver, Stand stand) {
        super();
        this.name = name;
        this.receiver = receiver;
        this.stand = stand;
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

    public abstract void runTest() throws Error, Exception;

    public enum State {
        READY, PASS, FAIL
    }
}
