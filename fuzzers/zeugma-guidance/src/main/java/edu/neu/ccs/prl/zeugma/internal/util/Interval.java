package edu.neu.ccs.prl.zeugma.internal.util;

/**
 * Immutable set of containing all integers between an upper and lower bound. Note: Cannot represent a set containing
 * {@link Integer#MAX_VALUE} or the empty set.
 */
public final class Interval {
    /**
     * The smallest element in this set.
     * <p>
     * {@code start < end}
     */
    private final int start;
    /**
     * One greater than the last element in this set.
     * <p>
     * {@code end > start}
     */
    private final int end;

    public Interval(int start, int end) {
        if (start >= end) {
            throw new IllegalArgumentException("The start of an interval must be less than its end");
        }
        this.start = start;
        this.end = end;
    }

    public int size() {
        return end - start;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public int hashCode() {
        return 31 * start + end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Interval)) {
            return false;
        }
        Interval interval = (Interval) o;
        return start == interval.start && end == interval.end;
    }

    @Override
    public String toString() {
        return String.format("[%d, %d)", start, end);
    }

    public boolean contains(Interval other) {
        return contains(other.start) && contains(other.end - 1);
    }

    public boolean contains(int value) {
        return value >= start && value < end;
    }

    public boolean disjoint(Interval other) {
        return start >= other.end || end <= other.start;
    }
}
