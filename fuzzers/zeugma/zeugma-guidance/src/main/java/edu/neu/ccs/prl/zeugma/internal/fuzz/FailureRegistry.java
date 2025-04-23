package edu.neu.ccs.prl.zeugma.internal.fuzz;

import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleSet;

/**
 * Performs naive stack-trace-based deduplication of induced failures.
 */
public final class FailureRegistry {
    /**
     * Set of unique failures found so far.
     * <p>
     * Non-null.
     */
    private final SimpleSet<StackTrace> uniqueFailures = new SimpleSet<>();
    /**
     * Instance used to clean the stack trace of induced failures.
     * <p>
     * Non-null.
     */
    private final StackTraceCleaner cleaner;

    public FailureRegistry(int maxTraceSize) {
        this.cleaner = new StackTraceCleaner(maxTraceSize);
    }

    public boolean add(Throwable failure) {
        return add(cleaner.cleanStackTrace(failure));
    }

    public synchronized boolean add(StackTraceElement[] trace) {
        return trace != null && uniqueFailures.add(new StackTrace(trace));
    }

    /**
     * Produces cleaned stack traces. A cleaned stack trace is created by first identifying the root cause of an
     * exception or error. Internal fuzzer frames are removed from the stack trace for the root cause. Finally, the
     * root cause trace is trimmed to a specified maximum number of elements.
     */
    private static class StackTraceCleaner {
        private static final String INTERNAL_PACKAGE_PREFIX = "edu.neu.ccs.prl.zeugma.internal.";
        private final int maxSize;

        public StackTraceCleaner(int maxSize) {
            if (maxSize < 0) {
                throw new IllegalArgumentException();
            }
            this.maxSize = maxSize;
        }

        public StackTraceElement[] cleanStackTrace(Throwable t) {
            while (t.getCause() != null) {
                t = t.getCause();
            }
            SimpleList<StackTraceElement> cleanedTrace = new SimpleList<>();
            for (StackTraceElement element : t.getStackTrace()) {
                if (cleanedTrace.size() == maxSize) {
                    return cleanedTrace.toArray(new StackTraceElement[cleanedTrace.size()]);
                }
                if (!isExcluded(element.getClassName())) {
                    cleanedTrace.add(element);
                }
            }
            return cleanedTrace.toArray(new StackTraceElement[cleanedTrace.size()]);
        }

        public static boolean isExcluded(String className) {
            return className.startsWith(INTERNAL_PACKAGE_PREFIX);
        }
    }


    private static final class StackTrace {
        private final StackTraceElement[] elements;

        private StackTrace(StackTraceElement[] elements) {
            this.elements = elements;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (!(o instanceof StackTrace)) {
                return false;
            }
            StackTrace that = (StackTrace) o;
            if (elements.length != that.elements.length) {
                return false;
            }
            for (int i = 0; i < elements.length; i++) {
                StackTraceElement e1 = elements[i];
                StackTraceElement e2 = that.elements[i];
                if (e1 != e2 && (e1 == null || !e1.equals(e2))) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = 1;
            for (Object element : elements) {
                result = 31 * result + (element == null ? 0 : element.hashCode());
            }
            return result;
        }
    }
}
