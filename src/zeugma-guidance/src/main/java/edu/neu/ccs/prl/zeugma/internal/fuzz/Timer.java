package edu.neu.ccs.prl.zeugma.internal.fuzz;

import edu.neu.ccs.prl.zeugma.internal.runtime.struct.Iterator;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.Queue;

import java.io.IOException;

public final class Timer {
    /**
     * Maximum amount of time to run this timer for in milliseconds.
     * <p>
     * Non-negative.
     */
    private final long duration;
    /**
     * Timed tasks associated with this timer.
     */
    private final Queue<Task> tasks = new Queue<>();
    /**
     * The time (measured in milliseconds between the current time and midnight of January 1, 1970, UTC) when this timer
     * started.
     */
    private long startTime = System.currentTimeMillis();

    public Timer(long duration) {
        if (duration < 0) {
            throw new IllegalArgumentException();
        }
        this.duration = duration;
    }

    public Timer attach(Listener listener, long period) {
        tasks.enqueue(new Task(listener, period));
        return this;
    }

    public Timer start() throws IOException {
        startTime = System.currentTimeMillis();
        unexpired();
        return this;
    }

    public boolean unexpired() throws IOException {
        for (Iterator<Task> itr = tasks.iterator(); itr.hasNext(); ) {
            itr.next().check(this);
        }
        return elapsedTime() < duration;
    }

    public long elapsedTime() {
        return System.currentTimeMillis() - startTime;
    }

    public interface Listener {
        void update(long elapsedTime) throws IOException;
    }

    private static final class Task {
        /**
         * Observer to be notifier of updates.
         */
        private final Listener listener;
        /**
         * Minimum amount of time in milliseconds between updates.
         * <p>
         * Non-negative.
         */
        private final long period;
        /**
         * The last time (measured in milliseconds between the current time and midnight of January 1, 1970, UTC) when
         * an update occurred.
         */
        private long lastUpdateTime;

        private Task(Listener listener, long period) {
            if (period < 0) {
                throw new IllegalArgumentException();
            }
            if (listener == null) {
                throw new NullPointerException();
            }
            this.period = period;
            this.listener = listener;
            this.lastUpdateTime = System.currentTimeMillis() - period;
        }

        private void check(Timer timer) throws IOException {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUpdateTime >= period) {
                listener.update(timer.elapsedTime());
                lastUpdateTime = currentTime;
            }
        }
    }
}
