package edu.neu.ccs.prl.zeugma.internal.guidance.modify;

import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

import java.util.Random;

public final class TwoPointSplicer implements Splicer {
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

    public TwoPointSplicer(Random random) {
        this(random, true);
    }

    public TwoPointSplicer(Random random, boolean randomFirstParent) {
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
        int start1 = random.nextInt(parent1.size() + 1);
        int end1 = random.nextInt(parent1.size() + 1);
        int start2 = random.nextInt(parent2.size() + 1);
        int end2 = random.nextInt(parent2.size() + 1);
        // Swap so that the start is always before or at the end
        if (start1 > end1) {
            int temp = start1;
            start1 = end1;
            end1 = temp;
        }
        if (start2 > end2) {
            int temp = start2;
            start2 = end2;
            end2 = temp;
        }
        return splice(parent1, start1, end1, parent2, start2, end2);
    }

    @Override
    public String toString() {
        return "TwoPointSplicer";
    }

    static ByteList splice(ByteList parent1, int start1, int end1, ByteList parent2, int start2, int end2) {
        return parent1.subList(0, start1)
                .concatenate(parent2.subList(start2, end2))
                .concatenate(parent1.subList(end1, parent1.size()));
    }
}

