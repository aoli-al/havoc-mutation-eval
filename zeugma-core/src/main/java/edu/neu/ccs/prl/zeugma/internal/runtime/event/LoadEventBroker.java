package edu.neu.ccs.prl.zeugma.internal.runtime.event;

public final class LoadEventBroker {
    private static volatile LoadEventSubscriber subscriber = null;

    private LoadEventBroker() {
        throw new AssertionError();
    }

    public static void setSubscriber(LoadEventSubscriber subscriber) {
        // Ensure that ThreadFieldAccessor is loaded
        if (ThreadFieldAccessor.reserve()) {
            ThreadFieldAccessor.free();
        }
        LoadEventBroker.subscriber = subscriber;
    }

    public static void classLoaded() {
        LoadEventSubscriber s = subscriber;
        if (s != null && ThreadFieldAccessor.reserve()) {
            try {
                s.classLoaded();
            } finally {
                ThreadFieldAccessor.free();
            }
        }
    }
}
