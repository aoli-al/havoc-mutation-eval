package edu.neu.ccs.prl.zeugma.internal.runtime.event;

/**
 * Before notifying a subscriber of an event, brokers must check whether the event was triggered from
 * within a subscriber's handling of a different event.
 * This "inner" event should be suppressed to prevent the risk of triggering an infinite loop.
 * This suppression is done using {@link ThreadFieldAccessor#reserve()}.
 */
public final class ThreadFieldAccessor {
    static {
        // Ensure that used classes are loaded
        new ThreadState();
    }

    private ThreadFieldAccessor() {
        throw new AssertionError();
    }

    @SuppressWarnings("unused")
    private static Object get(Thread thread) {
        throw new AssertionError("Called un-instrumented method body");
    }

    @SuppressWarnings("unused")
    private static void set(Thread thread, Object value) {
        throw new AssertionError("Called un-instrumented method body");
    }

    private static ThreadState getCurrentState() {
        Thread current = Thread.currentThread();
        ThreadState state = (ThreadState) get(current);
        if (state == null) {
            state = new ThreadState();
            set(current, state);
        }
        return state;
    }

    public static void free() {
        getCurrentState().reserved = false;
    }

    public static boolean reserve() {
        ThreadState state = getCurrentState();
        if (!state.reserved) {
            // Prevent re-entry on the same Thread
            state.reserved = true;
            return true;
        } else {
            return false;
        }
    }

    public static final class ThreadState {
        private boolean reserved = false;
    }
}