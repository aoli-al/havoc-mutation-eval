package edu.neu.ccs.prl.zeugma.internal.guidance;

/**
 * Produces arbitrary Java primitive values from a data source.
 */
public interface DataProvider {
    /**
     * Returns an arbitrary {@code float} value.
     *
     * @return an arbitrary {@code float} value
     */
    float nextFloat();

    /**
     * Returns an arbitrary {@code float} value in {@code [min, max)}.
     *
     * @param min the lower bound (inclusive)
     * @param max the upper bound (exclusive)
     * @return an arbitrary {@code float} value in {@code [min, max)}
     * @throws IllegalArgumentException if {@code max <= min}
     */
    float nextFloat(float min, float max);

    /**
     * Returns an arbitrary, finite (neither a NaN value nor an infinite value) {@code float} value.
     *
     * @return an arbitrary, finite (neither a NaN value nor an infinite value) {@code float} value
     */
    float nextFiniteFloat();

    /**
     * Returns an arbitrary {@code float} value in {@code [0.0, 1.0)}.
     *
     * @return an arbitrary {@code float} value in {@code [0.0, 1.0)}
     */
    float nextProbabilityFloat();

    /**
     * Returns an arbitrary {@code double} value.
     *
     * @return an arbitrary {@code double} value
     */
    double nextDouble();

    /**
     * Returns an arbitrary {@code double} value in {@code [min, max)}.
     *
     * @param min the lower bound (inclusive)
     * @param max the upper bound (exclusive)
     * @return an arbitrary {@code double} value in {@code [min, max)}
     * @throws IllegalArgumentException if {@code max <= min}
     */
    double nextDouble(double min, double max);

    /**
     * Returns an arbitrary, finite (neither a NaN value nor an infinite value) {@code double} value.
     *
     * @return an arbitrary, finite (neither a NaN value nor an infinite value) {@code double} value
     */
    double nextFiniteDouble();

    /**
     * Returns an arbitrary {@code double} value in {@code [0.0, 1.0)}.
     *
     * @return an arbitrary {@code double} value in {@code [0.0, 1.0)}
     */
    double nextProbabilityDouble();

    /**
     * Returns an arbitrary {@code long} value.
     *
     * @return an arbitrary {@code long} value
     */
    long nextLong();

    /**
     * Returns an arbitrary {@code long} value in {@code [min, max]}.
     *
     * @param min the lower bound (inclusive)
     * @param max the upper bound (inclusive)
     * @return an arbitrary {@code long} value in {@code [min, max]}
     * @throws IllegalArgumentException if {@code max < min}
     */
    long nextLong(long min, long max);

    /**
     * Returns an arbitrary {@code int} value.
     *
     * @return an arbitrary {@code int} value
     */
    int nextInt();

    /**
     * Returns an arbitrary {@code int} value in {@code [min, max]}.
     *
     * @param min the lower bound (inclusive)
     * @param max the upper bound (inclusive)
     * @return an arbitrary {@code int} value in {@code [min, max]}
     * @throws IllegalArgumentException if {@code max < min}
     */
    int nextInt(int min, int max);

    /**
     * Returns an arbitrary {@code int} value in {@code [0, n)}.
     *
     * @param n the upperbound (exclusive)
     * @return an arbitrary {@code int} value in {@code [0, n)}
     * @throws IllegalArgumentException if {@code n <= 0}
     */
    int nextInt(int n);

    /**
     * Returns an arbitrary {@code boolean} value.
     *
     * @return an arbitrary {@code boolean} value
     */
    boolean nextBoolean();

    /**
     * Returns an arbitrary {@code char} value.
     *
     * @return an arbitrary {@code char} value
     */
    char nextChar();

    /**
     * Returns an arbitrary {@code char} value in {@code [min, max]}.
     *
     * @param min the lower bound (inclusive)
     * @param max the upper bound (inclusive)
     * @return an arbitrary {@code char} value in {@code [min, max]}
     * @throws IllegalArgumentException if {@code max < min}
     */
    char nextChar(char min, char max);

    /**
     * Returns an arbitrary {@code short} value.
     *
     * @return an arbitrary {@code short} value
     */
    short nextShort();

    /**
     * Returns an arbitrary {@code short} value in {@code [min, max]}.
     *
     * @param min the lower bound (inclusive)
     * @param max the upper bound (inclusive)
     * @return an arbitrary {@code short} value in {@code [min, max]}
     * @throws IllegalArgumentException if {@code max < min}
     */
    short nextShort(short min, short max);

    /**
     * Returns an arbitrary {@code byte} value.
     *
     * @return an arbitrary {@code byte} value
     */
    byte nextByte();

    /**
     * Returns an arbitrary {@code byte} value in {@code [min, max]}.
     *
     * @param min the lower bound (inclusive)
     * @param max the upper bound (inclusive)
     * @return an arbitrary {@code byte} value in {@code [min, max]}
     * @throws IllegalArgumentException if {@code max < min}
     */
    byte nextByte(byte min, byte max);

    /**
     * Returns an array of arbitrary length containing arbitrary {@code byte} values. Consumes all remaining data for
     * this provider.
     *
     * @return an array of arbitrary length containing arbitrary {@code byte} values
     */
    byte[] consumeRemaining();

    /**
     * Returns the amount of remaining bytes for this provider.
     *
     * @return the amount of remaining bytes for this provider.
     */
    int remainingBytes();

    /**
     * Returns the number of bytes that have been consumed from this provider.
     *
     * @return the number of bytes that have been consumed from this provider
     */
    int getNumberConsumed();
}
