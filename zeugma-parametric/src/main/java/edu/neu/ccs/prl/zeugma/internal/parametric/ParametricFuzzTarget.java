package edu.neu.ccs.prl.zeugma.internal.parametric;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.neu.ccs.prl.zeugma.internal.guidance.FuzzTarget;
import edu.neu.ccs.prl.zeugma.internal.guidance.TestReport;
import edu.neu.ccs.prl.zeugma.internal.provider.RecordingDataProvider;
import edu.neu.ccs.prl.zeugma.parametric.ProviderBackedRandomness;
import org.junit.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

final class ParametricFuzzTarget implements FuzzTarget {
    private final FailureListener listener = new FailureListener();
    private final JqfRunner runner;
    private final RunNotifier notifier;
    private final StructuredInputGenerator generator;
    private final int maxStatusSize;
    private final int maxTraceSize;
    private final int maxInputSize;
    private final String descriptor;

    ParametricFuzzTarget(JqfRunner runner, RunNotifier notifier, String descriptor,
                         StructuredInputGenerator generator) {
        if (notifier == null || descriptor == null || runner == null || generator == null) {
            throw new NullPointerException();
        }
        this.maxStatusSize = Integer.getInteger("zeugma.maxStatusSize", 100);
        this.maxTraceSize = Integer.getInteger("zeugma.maxTraceSize", 5);
        this.maxInputSize = Integer.getInteger("zeugma.maxInputSize", 10240);
        if (maxStatusSize < 0 || maxTraceSize < 0 || maxInputSize < 0) {
            throw new IllegalArgumentException();
        }
        this.runner = runner;
        this.notifier = notifier;
        notifier.addListener(listener);
        this.generator = generator;
        this.descriptor = descriptor;
    }

    @Override
    public int getMaxTraceSize() {
        return maxTraceSize;
    }

    @Override
    public int getMaxInputSize() {
        return maxInputSize;
    }

    @Override
    public String getDescriptor() {
        return descriptor;
    }

    @Override
    public TestReport run(RecordingDataProvider provider) {
        Throwable failure = runInternal(provider);
        TestReport report = new TestReport(failure, provider.getRecording(), this);
        report.serializedRecording = generatedObjects[0].toString();
        return report;
    }

    private Object[] generatedObjects;

    private Throwable runInternal(RecordingDataProvider provider) {
        Object[] arguments;
        boolean terminateEarly;
        try {
            SourceOfRandomness source = new ProviderBackedRandomness(provider, maxInputSize);
            arguments = generator.generate(source, new AttemptUnawareGenerationStatus(source, maxStatusSize));
            generatedObjects = arguments;
        } catch (InputSizeException | AssumptionViolatedException ignored) {
            return null;
        } catch (Throwable t) {
            return t;
        } finally {
            terminateEarly = provider.close();
        }
        if (terminateEarly) {
            return null;
        }
        try {
            runner.execute(notifier, arguments);
            return listener.getAndReset();
        } catch (Throwable t) {
            propagateOutOfMemoryError(t);
            throw new IllegalStateException("Failed to execute test", t);
        }
    }

    private static void propagateOutOfMemoryError(Throwable t) {
        while (t != null) {
            if (t instanceof OutOfMemoryError) {
                throw (OutOfMemoryError) t;
            }
            t = t.getCause();
        }
    }

    private static final class FailureListener extends RunListener {
        private Throwable failure = null;

        @Override
        public void testStarted(Description description) {
            failure = null;
        }

        @Override
        public void testFailure(Failure failure) {
            this.failure = failure.getException();
        }

        public Throwable getAndReset() {
            Throwable result = failure;
            failure = null;
            return result;
        }
    }
}
