package edu.neu.ccs.prl.zeugma.internal.provider;

import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.UnmodifiableByteList;

import java.util.Random;

/**
 * A byte producer that generates new, random values when all of its initial values have been read until a maximum size
 * is reached.
 */
public final class ExpandingByteProducer implements ByteProducer {
    /**
     * Maximum number of bytes that can be read from this producer.
     * <p>
     * Positive.
     */
    private final int maxSize;
    /**
     * Pseudo-random number generator that should be used when generating new values. {@code null} if all generated
     * values should be {@code 0}.
     */
    private final Random random;
    /**
     * List of byte values that can be read from this producer.
     * <p>
     * Non-null.
     */
    private final ByteList values;
    /**
     * Number of bytes that have been produced by this producer.
     * <p>
     * Non-negative.
     */
    private int produced = 0;

    public ExpandingByteProducer(Random random, int maxSize, ByteList values) {
        this.random = random;
        this.maxSize = maxSize;
        this.values = UnmodifiableByteList.of(new ByteArrayList(values));
    }

    @Override
    public byte next() {
        if (produced >= maxSize) {
            produced++;
            return 0;
        } else if (produced >= values.size()) {
            // Generate a new value
            produced++;
            return random == null ? 0 : (byte) random.nextInt(256);
        } else {
            return values.get(produced++);
        }
    }
}
