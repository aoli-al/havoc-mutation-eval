package edu.neu.ccs.prl.zeugma.internal.guidance.modify;

import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

import java.util.Random;

public final class OnePointSplicer implements Splicer {
    /**
     * Pseudo-random number generator.
     * <p>
     * Non-null.
     */
    private final Random random;

    /**
     * True if the first parent should be chosen at random.
     */
    private final boolean randomFirstParent;

    public OnePointSplicer(Random random) {
        this(random, true);
    }

    public OnePointSplicer(Random random, boolean randomFirstParent) {
        if (random == null) {
            throw new NullPointerException();
        }
        this.random = random;
        this.randomFirstParent = randomFirstParent;
    }

    @Override
    public ByteList splice(ByteList parent1, ByteList parent2) {
        if (randomFirstParent && random.nextBoolean()) {
            // Randomly choose the first parent
            ByteList temp = parent1;
            parent1 = parent2;
            parent2 = temp;
        }
        int p1 = random.nextInt(parent1.size() + 1);
        int p2 = random.nextInt(parent2.size() + 1);
        return splice(parent1, p1, parent2, p2);
    }

    @Override
    public String toString() {
        return "OnePointSplicer";
    }

    public static ByteList splice(ByteList parent1, int p1, ByteList parent2, int p2) {
        return parent1.subList(0, p1).concatenate(parent2.subList(p2, parent2.size()));
    }
}
