package edu.neu.ccs.prl.zeugma.internal.guidance.linked;

import edu.neu.ccs.prl.zeugma.internal.guidance.modify.Modifier;
import edu.neu.ccs.prl.zeugma.internal.guidance.modify.Mutator;
import edu.neu.ccs.prl.zeugma.internal.guidance.select.RandomSelector;
import edu.neu.ccs.prl.zeugma.internal.guidance.select.Selector;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.IntProducer;

import java.util.Random;

public class LinkedModifier<E extends LinkedIndividual> implements Modifier<E> {
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
     * Pseudo-random number generator.
     * <p>
     * Non-null.
     */
    private final Random random;
    private final Selector<Object> selector;

    public LinkedModifier(Random random, Mutator mutator, IntProducer countSampler) {
        if (random == null || mutator == null || countSampler == null) {
            throw new NullPointerException();
        }
        this.random = random;
        this.mutator = mutator;
        this.countSampler = countSampler;
        this.selector = new RandomSelector<>(random);
    }

    @Override
    public ByteList modify(E parent, SimpleList<? extends E> population) {
        int count = countSampler.get();
        SimpleList<LinkedSplice> splices = new SimpleList<>(count);
        int mutationCount = count;
        if (!population.isEmpty() && !parent.getApplicationPoints().isEmpty()) {
            for (int i = 0; i < count; i++) {
                if (random.nextBoolean()) {
                    splices.add(createSplice(parent, population));
                    mutationCount--;
                }
            }
        }
        ByteList child = apply(splices.toArray(new LinkedSplice[splices.size()]), parent.getInput());
        for (int i = 0; i < mutationCount && mutator.isValid(child); i++) {
            child = mutator.mutate(child);
        }
        return child;
    }

    private LinkedSplice createSplice(LinkedIndividual parent, SimpleList<? extends LinkedIndividual> population) {
        SimpleList<MethodCallVertex> applicationPoints = parent.getApplicationPoints();
        MethodCallVertex applicationPoint = selector.select(applicationPoints);
        SimpleList<LinkedIndividual> donors = collectEligibleDonors(population, applicationPoint);
        LinkedIndividual donor = selector.select(donors);
        SimpleList<MethodCallVertex> list = donor.getDonationMap().get(applicationPoint.getMethod());
        MethodCallVertex donationPoint = selector.select(list);
        return new LinkedSplice(applicationPoint, donationPoint, donor.getInput());
    }

    private static SimpleList<LinkedIndividual> collectEligibleDonors(SimpleList<? extends LinkedIndividual> population,
                                                                      MethodCallVertex applicationPoint) {
        SimpleList<LinkedIndividual> donors = new SimpleList<>();
        for (int i = 0; i < population.size(); i++) {
            LinkedIndividual next = population.get(i);
            if (next.getDonationMap().containsKey(applicationPoint.getMethod())) {
                donors.add(next);
            }
        }
        return donors;
    }

    private static ByteList apply(LinkedSplice[] splices, ByteList values) {
        // Apply the splice in reverse-order by position; splices targeting earlier positions can shift the start of
        // splices that target later positions
        java.util.Arrays.sort(splices, (m1, m2) -> {
            int s1 = m1.getStart();
            int s2 = m2.getStart();
            //noinspection UseCompareMethod
            return (s1 < s2) ? 1 : ((s1 == s2) ? 0 : -1);
        });
        LinkedSplice last = null;
        for (LinkedSplice splice : splices) {
            // Check that splice targets a range before the range targeted by the last splice
            if (last == null || splice.getEnd() <= last.getStart()) {
                last = splice;
                values = splice.apply(values);
            }
        }
        return values;
    }

    private static MethodCallVertex[] shuffle(SimpleList<MethodCallVertex> applicationPoints, Random random) {
        MethodCallVertex[] points = applicationPoints.toArray(new MethodCallVertex[applicationPoints.size()]);
        for (int i = 0; i < points.length; i++) {
            int j = random.nextInt(i + 1);
            MethodCallVertex temp = points[i];
            points[i] = points[j];
            points[j] = temp;
        }
        return points;
    }

    public static ByteList splice(LinkedIndividual parent1, LinkedIndividual parent2, Random random) {
        // Try the possible applications points in a random order
        MethodCallVertex[] applicationPoints = shuffle(parent1.getApplicationPoints(), random);
        for (MethodCallVertex applicationPoint : applicationPoints) {
            if (parent2.getDonationMap().containsKey(applicationPoint.getMethod())) {
                SimpleList<MethodCallVertex> list = parent2.getDonationMap().get(applicationPoint.getMethod());
                MethodCallVertex donationPoint = new RandomSelector<>(random).select(list);
                LinkedSplice splice = new LinkedSplice(applicationPoint, donationPoint, parent2.getInput());
                return splice.apply(parent1.getInput());
            }
        }
        return parent1.getInput();
    }
}
