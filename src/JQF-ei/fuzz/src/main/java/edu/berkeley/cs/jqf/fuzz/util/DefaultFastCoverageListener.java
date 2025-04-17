package edu.berkeley.cs.jqf.fuzz.util;

import edu.berkeley.cs.jqf.fuzz.guidance.TimeoutException;
import janala.instrument.FastCoverageListener;

import java.util.Date;

public class DefaultFastCoverageListener implements FastCoverageListener {
    long tick = 0;
    long singleRunTimeout = 0;
    Date runStart = null;

    public DefaultFastCoverageListener() {
        String timeout = System.getProperty("jqf.ei.TIMEOUT");
        if (timeout != null && !timeout.isEmpty()) {
            singleRunTimeout = Long.parseLong(timeout);
            runStart = new Date();
        }
    }

    public void logMethodBegin(int iid) {
        checkTimeout();
    }

    public void logMethodEnd(int iid) {
        checkTimeout();
    }

    public void logJump(int iid, int branch) {
        checkTimeout();
    }

    public void logLookUpSwitch(int value, int iid, int dflt, int[] cases) {
        checkTimeout();
    }

    public void logTableSwitch(int value, int iid, int min, int max, int dflt) {
        checkTimeout();
    }

    public void done() {
        runStart = null;
    }

    public void start() {
        runStart = new Date();
        tick = 0;
    }

    public void checkTimeout() {
        if (singleRunTimeout > 0 && runStart != null && (++tick) % 10_000 == 0) {
            long elapsed = new Date().getTime() - runStart.getTime();
            if (elapsed > singleRunTimeout) {
                throw new TimeoutException(elapsed, singleRunTimeout);
            }
        }
    }
}
