package edu.neu.ccs.prl.zeugma.internal.guidance.modify;

import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.IntProducer;
import edu.neu.ccs.prl.zeugma.internal.util.Math;

import java.util.Random;

public final class RegionMutator implements Mutator {
    /**
     * Pseudo-random number generator.
     * <p>
     * Non-null.
     */
    private final Random random;
    /**
     * Chooses the number of contiguous bytes to perturb.
     * <p>
     * Non-null.
     */
    private final IntProducer sizeSampler;

    public RegionMutator(Random random, IntProducer sizeSampler) {
        if (random == null || sizeSampler == null) {
            throw new NullPointerException();
        }
        this.random = random;
        this.sizeSampler = sizeSampler;
    }

    @Override
    public ByteList mutate(ByteList parent) {
        int position = random.nextInt(parent.size());
        int size = Math.min(parent.size() - position, sizeSampler.get());
        return ModifierUtil.replaceRange(parent, position, size, produceX(size));
    }

    @Override
    public boolean isValid(ByteList parent) {
        return !parent.isEmpty();
    }

    private ByteList produceX(int x) {
        ByteList values = new ByteArrayList();
        for (int i = 0; i < x; i++) {
            values.add((byte) random.nextInt(256));
        }
        return values;
    }
}
