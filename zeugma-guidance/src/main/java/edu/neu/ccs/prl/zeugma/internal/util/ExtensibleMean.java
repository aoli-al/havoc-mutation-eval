package edu.neu.ccs.prl.zeugma.internal.util;

/**
 * Tracks the arithmetic mean of an extensible collection of numbers without storing the collection of numbers.
 */
public class ExtensibleMean {
    private long size = 0;
    private double mean = 0;

    public double get() {
        if (size == 0) {
            throw new IllegalStateException("Cannot calculate the mean of an empty collection");
        }
        return mean;
    }

    public long size() {
        return size;
    }

    public void extend(long size, double mean) {
        if (size < 0) {
            throw new IllegalArgumentException();
        }
        if (size != 0 && this.mean != mean) {
            double v1 = this.size + size;
            double v2 = this.size * (this.mean / v1);
            double v3 = size * (mean / v1);
            this.mean = v2 + v3;
        }
        this.size += size;
    }

    public void extend(long value) {
        extend(1, value);
    }

    public void extend(ExtensibleMean other) {
        extend(other.size, other.mean);
    }

    public double orElse(double other) {
        return size == 0 ? other : get();
    }

    @Override
    public String toString() {
        return String.format("%f (size = %d)", mean, size);
    }
}
