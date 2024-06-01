package edu.neu.ccs.prl.zeugma.internal.guidance.modify;

import edu.neu.ccs.prl.zeugma.internal.util.IntProducer;

import java.util.Random;

import static java.lang.Math.ceil;

/**
 * Produces samples from a shifted geometric distribution (i.e., the distribution of the number Bernoulli trials needed
 * to obtain one success) with the specified mean value.
 */
public final class ShiftedGeometricSampler implements IntProducer {
    /**
     * Pseudo-random number generator.
     * <p>
     * Non-null.
     */
    private final Random random;
    /**
     * Mean value for the distribution.
     * <p>
     * {@code mean >= 1}
     */
    private final double mean;

    /**
     * Constructs a new producer that produces samples from a shifted geometric distribution.
     *
     * @param random pseudo-random number generator used to produce samples
     * @param mean   mean value for the distribution
     * @throws NullPointerException     if the specified pseudo-random number generator is {@code null}
     * @throws IllegalArgumentException if {@code mean < 1}.
     */
    public ShiftedGeometricSampler(Random random, double mean) {
        if (random == null) {
            throw new NullPointerException();
        } else if (mean < 1) {
            throw new IllegalArgumentException("Mean must be greater than or equal to 1");
        }
        this.random = random;
        this.mean = mean;
    }

    @Override
    public int get() {
        if (mean == 1) {
            return 1;
        } else {
            double lp = Math.log1p(-1.0 / mean);
            return (int) ceil(Math.log1p(-random.nextDouble()) / lp);
        }
    }
}
