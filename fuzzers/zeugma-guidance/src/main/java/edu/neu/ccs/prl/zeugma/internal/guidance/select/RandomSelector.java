package edu.neu.ccs.prl.zeugma.internal.guidance.select;

import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;

import java.util.Random;

/**
 * Chooses elements uniformly at random.
 */
public final class RandomSelector<T> implements Selector<T> {
    private final Random random;

    public RandomSelector(Random random) {
        if (random == null) {
            throw new NullPointerException();
        }
        this.random = random;
    }

    @Override
    public <R extends T> R select(SimpleList<R> list) {
        if (list.size() == 0) {
            throw new IllegalArgumentException();
        }
        return list.get(random.nextInt(list.size()));
    }
}