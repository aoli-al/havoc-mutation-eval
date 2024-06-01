package edu.neu.ccs.prl.zeugma.internal.guidance;

import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleSet;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.Function;

public class PopulationTracker<T extends Individual> {
    private final SimpleList<T[]> bestMap = new SimpleList<>();
    /**
     * Creates individuals from inputs.
     * <p>
     * Non-null.
     */
    private final Function<? super ByteList, ? extends T> factory;
    /**
     * Population of interesting inputs.
     */
    private SimpleList<T> population = new SimpleList<>();

    public PopulationTracker(Function<? super ByteList, ? extends T> factory) {
        if (factory == null) {
            throw new NullPointerException();
        }
        this.factory = factory;
    }

    public SimpleList<T> getPopulation() {
        if (population == null) {
            population = new SimpleList<>();
            SimpleSet<T> unique = new SimpleSet<>();
            for (int i = 0; i < bestMap.size(); i++) {
                for (T best : bestMap.get(i)) {
                    if (best != null && unique.add(best)) {
                        population.add(best);
                    }
                }
            }
        }
        return population;
    }

    public void update(TestReport report, boolean[][] coverageMap) {
        // Avoid saving inputs that trigger expensive failures to the population
        if (!(report.getFailure() instanceof StackOverflowError || report.getFailure() instanceof OutOfMemoryError)) {
            T individual = factory.apply(report.getRecording());
            if (update(individual, coverageMap)) {
                individual.initialize(report.getTarget());
            }
        }
    }

    private boolean update(T participant, boolean[][] runCoverageMap) {
        boolean changed = false;
        for (int classIndex = 0; classIndex < runCoverageMap.length; classIndex++) {
            boolean[] runCoverage = runCoverageMap[classIndex];
            if (classIndex >= bestMap.size()) {
                bestMap.add(createArray(runCoverage.length));
            }
            T[] best = bestMap.get(classIndex);
            for (int probeIndex = 0; probeIndex < runCoverage.length; probeIndex++) {
                if (runCoverage[probeIndex] && compare(best[probeIndex], participant)) {
                    best[probeIndex] = participant;
                    population = null;
                    changed = true;
                }
            }
        }
        return changed;
    }

    private boolean compare(T incumbent, T challenger) {
        return incumbent == null || incumbent.getInput().size() > challenger.getInput().size();
    }

    @SuppressWarnings("unchecked")
    private static <E> E[] createArray(int size) {
        return (E[]) new Individual[size];
    }
}
