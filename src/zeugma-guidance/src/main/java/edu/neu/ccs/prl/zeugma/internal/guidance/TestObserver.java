package edu.neu.ccs.prl.zeugma.internal.guidance;

import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

/**
 * Observes system behavior during the execution a test.
 */
public interface TestObserver {
    /**
     * Notifies this observer that a new test execution is starting.
     */
    void starting(ByteList input);

    /**
     * Notifies this observer that the current test execution has finished. May be called more than once for the same
     * test.
     */
    void finished();
}