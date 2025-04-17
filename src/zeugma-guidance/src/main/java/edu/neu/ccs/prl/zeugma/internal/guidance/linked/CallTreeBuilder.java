package edu.neu.ccs.prl.zeugma.internal.guidance.linked;

import edu.neu.ccs.prl.zeugma.internal.guidance.FuzzTarget;
import edu.neu.ccs.prl.zeugma.internal.guidance.TestObserver;
import edu.neu.ccs.prl.zeugma.internal.guidance.TestRunner;
import edu.neu.ccs.prl.zeugma.internal.provider.BasicRecordingDataProvider;
import edu.neu.ccs.prl.zeugma.internal.provider.DataProviderFactory;
import edu.neu.ccs.prl.zeugma.internal.runtime.event.ContextEventBroker;
import edu.neu.ccs.prl.zeugma.internal.runtime.event.ContextEventSubscriber;
import edu.neu.ccs.prl.zeugma.internal.runtime.model.MethodIdentifier;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.Stack;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

public class CallTreeBuilder implements ContextEventSubscriber, TestObserver {
    private final TestRunner runner;
    private Stack<CallTreeVertex> stack;
    private CallTree tree = new CallTree();
    private Thread trackedThread;

    public CallTreeBuilder(FuzzTarget target) {
        DataProviderFactory factory = BasicRecordingDataProvider.createFactory(null, target.getMaxInputSize());
        factory = RequestPublishingProvider.attach(factory, this);
        this.runner = new TestRunner(target, factory, this);
    }

    public CallTree build(ByteList values) {
        runner.run(values);
        finished();
        CallTree copy = tree;
        tree = null;
        return copy;
    }

    @Override
    public synchronized void starting(ByteList input) {
        finished();
        tree = new CallTree();
        trackedThread = Thread.currentThread();
        stack = new Stack<>();
        ContextEventBroker.setSubscriber(this);
    }

    @Override
    public synchronized void finished() {
        ContextEventBroker.setSubscriber(null);
        restore(0);
    }

    @Override
    public int enteringMethod(MethodIdentifier method) {
        if (Thread.currentThread() == trackedThread) {
            stack.push(new MethodCallVertex(method));
            return stack.size();
        }
        return -1;
    }

    @Override
    public void exitingMethod(int level) {
        restore(level - 1);
    }

    @Override
    public void restore(int level) {
        if (Thread.currentThread() == trackedThread) {
            while (stack.size() > level) {
                CallTreeVertex call = stack.pop();
                if (call.getNumberOfChildren() > 0) {
                    (stack.isEmpty() ? tree : stack.peek()).addChild(call);
                }
            }
        }
    }

    public boolean close() {
        finished();
        return true;
    }

    public void parameterRequest(int start, int end) {
        if (Thread.currentThread() == trackedThread) {
            (stack.isEmpty() ? tree : stack.peek()).addChild(new ParameterRequestVertex(start, end));
        }
    }
}
