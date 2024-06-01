package edu.neu.ccs.prl.zeugma.internal.guidance;

import edu.neu.ccs.prl.zeugma.internal.fuzz.GuidanceManager;
import edu.neu.ccs.prl.zeugma.internal.guidance.modify.Modifier;
import edu.neu.ccs.prl.zeugma.internal.guidance.select.RandomSelector;
import edu.neu.ccs.prl.zeugma.internal.guidance.select.Selector;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

import java.io.IOException;
import java.util.Random;

public class ParametricGuidance<T extends Individual> implements Guidance {
    /**
     * Maximum size (inclusive) of randomly created inputs.
     * <p>
     * Non-negative.
     */
    private static final int MAX_INITIAL_SIZE = 64;
    /**
     * Pseudo-random number generator.
     * <p>
     * Non-null.
     */
    private final Random random;
    /**
     * Executes inputs.
     * <p>
     * Non-null.
     */
    private final TestRunner runner;
    /**
     * Manages the state of the fuzzing campaign.
     * <p>
     * Non-null.
     */
    private final GuidanceManager manager;
    /**
     * Maintains a population of interesting individuals.
     * <p>
     * Non-null.
     */
    private final PopulationTracker<? extends T> tracker;
    /**
     * Selects individuals from the population to be perturbed.
     * <p>
     * Non-null.
     */
    private final Selector<? super T> selector;
    /**
     * Perturbs individuals.
     * <p>
     * Non-null.
     */
    private final Modifier<? super T> modifier;
    /**
     * Observes coverage during the execution of a test.
     * <p>
     * Non-null.
     */
    private final CoverageObserver observer = new CoverageObserver();

    public ParametricGuidance(FuzzTarget target, GuidanceManager manager, Random random,
                              PopulationTracker<? extends T> tracker, Modifier<? super T> modifier) {
        if (manager == null || random == null || tracker == null || modifier == null) {
            throw new NullPointerException();
        }
        this.runner = new TestRunner(target, random, observer);
        this.manager = manager;
        this.tracker = tracker;
        this.selector = new RandomSelector<>(random);
        this.random = random;
        this.modifier = modifier;
    }

    @Override
    public void fuzz() throws IOException {
        while (manager.unexpired()) {
            // Execute the next input
            TestReport report = runner.run(nextInput());
            // Collect run coverage
            boolean[][] coverageMap = observer.getCoverageMap();
            // Update the population
            tracker.update(report, coverageMap);
            // Notify the manager that the execution finished
            manager.finishedExecution(report, coverageMap);
        }
    }

    private ByteList nextInput() {
        SimpleList<? extends T> population = tracker.getPopulation();
        if (population.isEmpty()) {
            // Create a new random input
            return createRandomInput();
        } else {
            // Select a parent from the population and perturb it
            return modifier.modify(selector.select(population), population);
        }
    }

    private ByteList createRandomInput() {
        byte[] b = new byte[random.nextInt(MAX_INITIAL_SIZE + 1)];
        random.nextBytes(b);
        return new ByteArrayList(b);
    }
}