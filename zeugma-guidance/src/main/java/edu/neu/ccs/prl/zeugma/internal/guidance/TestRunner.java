package edu.neu.ccs.prl.zeugma.internal.guidance;

import edu.neu.ccs.prl.zeugma.internal.provider.BasicRecordingDataProvider;
import edu.neu.ccs.prl.zeugma.internal.provider.DataProviderFactory;
import edu.neu.ccs.prl.zeugma.internal.provider.RecordingDataProvider;
import edu.neu.ccs.prl.zeugma.internal.runtime.event.LoadEventBroker;
import edu.neu.ccs.prl.zeugma.internal.runtime.event.LoadEventSubscriber;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Random;

public final class TestRunner implements LoadEventSubscriber {
    private final FuzzTarget target;
    private final DataProviderFactory factory;
    private final TestObserver observer;
    private volatile boolean running = true;

    public TestRunner(FuzzTarget target, DataProviderFactory factory, TestObserver observer) {
        if (factory == null || observer == null || target == null) {
            throw new NullPointerException();
        }
        this.target = target;
        this.observer = observer;
        this.factory = factory;
    }

    public TestRunner(FuzzTarget target, Random random, TestObserver observer) {
        this(target, BasicRecordingDataProvider.createFactory(random, target.getMaxInputSize()), observer);
    }

    public TestReport run(ByteList input) {
        PrintStream out = suppressStandardOut();
        PrintStream error = suppressStandardErr();
        try {
            return runInternal(input, false);
        } finally {
            // Restore standard error and out
            System.setOut(out);
            System.setErr(error);
        }
    }

    private void stop() {
        running = false;
        // Stop listening for class loads
        LoadEventBroker.setSubscriber(null);
        // Notify the observer that the test execution has finished
        observer.finished();
    }

    private void start(boolean rerunning, RecordingDataProvider provider) {
        // Notify the observer that a new test execution is about to begin
        observer.starting(provider.getRecording());
        // Start listening for class loads
        LoadEventBroker.setSubscriber(rerunning ? null : this);
        running = true;
    }

    private TestReport runInternal(ByteList values, boolean rerunning) {
        // Wrap the input values
        RecordingDataProvider provider = factory.create(values);
        start(rerunning, provider);
        TestReport report;
        boolean stoppedEarly;
        try {
            report = target.run(provider);
            stoppedEarly = !running;
        } finally {
            provider.close();
            stop();
        }
        // Rerun the test if it completed without error, and the observer was stopped early
        // Note: Failing test runs can be rerun because either the failure will occur again or the failure cannot be
        // consistently reproduced using the input that was run
        return stoppedEarly ? runInternal(provider.getRecording(), true) : report;
    }

    @Override
    public void classLoaded() {
        stop();
    }

    private static PrintStream suppressStandardErr() {
        PrintStream result = System.err;
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
        }));
        return result;
    }

    private static PrintStream suppressStandardOut() {
        PrintStream result = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
        }));
        return result;
    }
}
