package edu.neu.ccs.prl.zeugma.internal.runtime.event;

import edu.neu.ccs.prl.zeugma.internal.runtime.model.ClassModel;

public final class ContextEventBroker {
    private static volatile ContextEventSubscriber subscriber = null;

    private ContextEventBroker() {
        throw new AssertionError();
    }

    public static void setSubscriber(ContextEventSubscriber subscriber) {
        // Ensure that ThreadFieldAccessor is loaded
        if (ThreadFieldAccessor.reserve()) {
            ThreadFieldAccessor.free();
        }
        ContextEventBroker.subscriber = subscriber;
    }

    @InvokedViaInstrumentation(info = CoreMethodInfo.PUBLISH_ENTERING_METHOD)
    public static int entering(ClassModel model, int index) {
        ContextEventSubscriber s = subscriber;
        if (s != null && model != null && ThreadFieldAccessor.reserve()) {
            try {
                return s.enteringMethod(model.getMethod(index));
            } finally {
                ThreadFieldAccessor.free();
            }
        }
        return -1;
    }

    @InvokedViaInstrumentation(info = CoreMethodInfo.PUBLISH_EXITING_METHOD)
    public static void exiting(int level) {
        ContextEventSubscriber s = subscriber;
        if (s != null && level != -1 && ThreadFieldAccessor.reserve()) {
            try {
                s.exitingMethod(level);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }

    @InvokedViaInstrumentation(info = CoreMethodInfo.PUBLISH_RESTORE)
    public static void restore(int level) {
        ContextEventSubscriber s = subscriber;
        if (s != null && level != -1 && ThreadFieldAccessor.reserve()) {
            try {
                s.restore(level);
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }
}
