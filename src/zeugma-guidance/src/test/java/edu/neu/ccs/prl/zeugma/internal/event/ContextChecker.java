package edu.neu.ccs.prl.zeugma.internal.event;

import org.opentest4j.MultipleFailuresError;

public abstract class ContextChecker {
    private final java.util.List<Throwable> errors = new java.util.LinkedList<>();

    protected abstract void checkInternal() throws Throwable;

    public void check() {
        try {
            checkInternal();
        } catch (Throwable t) {
            synchronized (this) {
                errors.add(t);
            }
        }
    }

    public synchronized void report() {
        if (!errors.isEmpty()) {
            throw new MultipleFailuresError(null, errors);
        }
    }

    public static StackTraceElement findCallee() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            StackTraceElement element = trace[i];
            if (element.getClassName().equals(ContextChecker.class.getName()) &&
                    element.getMethodName().equals("check")) {
                return trace[i + 1];
            }
        }
        throw new AssertionError("Caller could not be found");
    }

    public static StackTraceElement findCaller() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            StackTraceElement element = trace[i];
            if (element.getClassName().equals(ContextChecker.class.getName()) &&
                    element.getMethodName().equals("check")) {
                return trace[i + 2];
            }
        }
        throw new AssertionError("Callee could not be found");
    }
}
