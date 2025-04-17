package edu.neu.ccs.prl.zeugma.internal.guidance.modify;

import edu.neu.ccs.prl.zeugma.internal.guidance.Individual;
import edu.neu.ccs.prl.zeugma.internal.guidance.select.RandomSelector;
import edu.neu.ccs.prl.zeugma.internal.guidance.select.Selector;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.IntProducer;

import java.util.Random;

public class BasicModifier implements Modifier<Individual> {
    /**
     * Pseudo-random number generator.
     * <p>
     * Non-null.
     */
    private final Random random;
    /**
     * Mutates individuals.
     * <p>
     * Non-null.
     */
    private final Mutator mutator;
    /**
     * Chooses the number of stacked modifications to apply.
     * <p>
     * Non-null.
     */
    private final IntProducer countSampler;
    /**
     * Instances used to splice individuals together or {@code null} if individuals should not be spliced together.
     */
    private final Splicer splicer;
    /**
     * Selects individuals from the population to be used when splicing.
     * <p>
     * Non-null.
     */
    private final Selector<? super Individual> selector;

    public BasicModifier(Random random, Mutator mutator, IntProducer countSampler, Splicer splicer) {
        if (random == null || mutator == null || countSampler == null) {
            throw new NullPointerException();
        }
        this.random = random;
        this.mutator = mutator;
        this.countSampler = countSampler;
        this.splicer = splicer;
        this.selector = new RandomSelector<>(random);
    }

    @Override
    public ByteList modify(Individual parent, SimpleList<? extends Individual> population) {
        int count = countSampler.get();
        ByteList child = parent.getInput();
        for (int i = 0; i < count; i++) {
            if (splicer != null && !population.isEmpty() && random.nextBoolean()) {
                child = splicer.splice(child, selector.select(population).getInput());
            } else if (mutator.isValid(child)) {
                child = mutator.mutate(child);
            }
        }
        return child;
    }
}
