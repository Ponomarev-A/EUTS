package model.tests;

import model.Receiver;
import model.Stand;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for Test cases
 */
public abstract class BaseTestCase extends org.junit.Assert {

    private static AtomicInteger nextID = new AtomicInteger(0);

    private final String name;
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
}
