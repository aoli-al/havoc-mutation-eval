package edu.neu.ccs.prl.zeugma.internal.event;

import edu.neu.ccs.prl.zeugma.examples.ContextExamples;
import edu.neu.ccs.prl.zeugma.internal.runtime.event.ContextEventBroker;
import edu.neu.ccs.prl.zeugma.internal.runtime.event.ContextEventSubscriber;
import edu.neu.ccs.prl.zeugma.internal.runtime.model.MethodIdentifier;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.Stack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ContextEventSubscriberITCase {
    @AfterEach
    void reset() {
        ContextEventBroker.setSubscriber(null);
    }

    @Test
    void contextCorrectStandardReturn() {
        ContextTracker tracker = new ContextTracker();
        ContextEventBroker.setSubscriber(tracker);
        ContextExamples.outerStandardReturn(tracker);
        tracker.report();
    }

    @Test
    void contextCorrectExceptionalReturn() {
        ContextTracker tracker = new ContextTracker();
        ContextEventBroker.setSubscriber(tracker);
        ContextExamples.outerExceptionalReturn(tracker);
        tracker.report();
    }

    @Test
    void contextCorrectInitStandardReturn() {
        ContextTracker tracker = new ContextTracker();
        ContextEventBroker.setSubscriber(tracker);
        ContextExamples.outerInitStandardReturn(tracker);
        tracker.report();
    }

    @Test
    void contextCorrectInitExceptionalReturn() {
        ContextTracker tracker = new ContextTracker();
        ContextEventBroker.setSubscriber(tracker);
        ContextExamples.outerInitExceptionalReturn(tracker);
        tracker.report();
    }

    @Test
    void contextCorrectInitExceptionalReturnFromMethod() {
        ContextTracker tracker = new ContextTracker();
        ContextEventBroker.setSubscriber(tracker);
        ContextExamples.outerInitExceptionalReturnFromMethod(tracker);
        tracker.report();
    }

    static final class ContextTracker extends ContextChecker implements ContextEventSubscriber {
        private final Thread tracked = Thread.currentThread();
        private final Stack<MethodIdentifier> stack = new Stack<>();

        @Override
        protected void checkInternal() {
            StackTraceElement trace = findCallee();
            Assertions.assertFalse(stack.isEmpty());
            MethodIdentifier method = stack.peek();
            Assertions.assertNotNull(method);
            Assertions.assertEquals(trace.getClassName(), method.getOwner().getName().replace('/', '.'));
            Assertions.assertEquals(trace.getMethodName(), method.getName());
        }

        @Override
        public int enteringMethod(MethodIdentifier method) {
            if (Thread.currentThread() == tracked) {
                stack.push(method);
                return stack.size();
            } else {
                return -1;
            }
        }

        @Override
        public void exitingMethod(int level) {
            restore(level - 1);
        }

        @Override
        public void restore(int level) {
            while (stack.size() > level) {
                stack.pop();
            }
        }
    }
}