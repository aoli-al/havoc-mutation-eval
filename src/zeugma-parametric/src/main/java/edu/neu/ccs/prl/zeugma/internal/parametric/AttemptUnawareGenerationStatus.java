package edu.neu.ccs.prl.zeugma.internal.parametric;

import com.pholser.junit.quickcheck.internal.GeometricDistribution;
import com.pholser.junit.quickcheck.internal.generator.SimpleGenerationStatus;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

public class AttemptUnawareGenerationStatus extends SimpleGenerationStatus {
    private final int maxSize;

    public AttemptUnawareGenerationStatus(SourceOfRandomness random, int maxSize) {
        this(new GeometricDistribution(), random, maxSize);
    }

    private AttemptUnawareGenerationStatus(GeometricDistribution distribution, SourceOfRandomness random,
                                           int maxSize) {
        super(distribution, random, 0);
        if (distribution == null || random == null) {
            throw new NullPointerException();
        }
        if (maxSize < 0) {
            throw new IllegalArgumentException();
        }
        this.maxSize = maxSize;
    }

    @Override
    public int size() {
        return random().nextInt(maxSize);
    }

    @Override
    public int attempts() {
        throw new UnsupportedOperationException();
    }
}
