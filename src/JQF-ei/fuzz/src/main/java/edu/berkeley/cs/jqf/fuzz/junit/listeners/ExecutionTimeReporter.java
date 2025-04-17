package edu.berkeley.cs.jqf.fuzz.junit.listeners;

import org.junit.internal.JUnitSystem;
import org.junit.internal.TextListener;
import org.junit.runner.Description;
import org.junit.runner.Result;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class ExecutionTimeReporter extends TextListener {
    private Map<String, Long> startTimeMap = new HashMap<>();
    private Map<String, Long> finishTimeMap = new HashMap<>();
    public ExecutionTimeReporter(JUnitSystem system) {
        super(system);
    }

    public ExecutionTimeReporter(PrintStream writer) {
        super(writer);
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
        super.testRunStarted(description);
    }

    @Override
    public void testRunFinished(Result result) {
        super.testRunFinished(result);
    }

    @Override
    public void testStarted(Description description) {
        super.testStarted(description);
    }

    @Override
    public void testFinished(Description description) throws Exception {
        super.testFinished(description);
    }

    @Override
    public void testSuiteFinished(Description description) throws Exception {
        super.testSuiteFinished(description);
    }
}
