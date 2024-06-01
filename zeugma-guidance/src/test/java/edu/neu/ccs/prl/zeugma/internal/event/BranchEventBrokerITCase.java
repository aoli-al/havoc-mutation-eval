package edu.neu.ccs.prl.zeugma.internal.event;

import edu.neu.ccs.prl.zeugma.examples.BranchExamples;
import edu.neu.ccs.prl.zeugma.internal.guidance.CoverageObserver;
import edu.neu.ccs.prl.zeugma.internal.runtime.event.CoverageEventBroker;
import edu.neu.ccs.prl.zeugma.internal.runtime.model.ModelRegistry;
import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Checks that branch edge executions are published to the {@link CoverageEventBroker}.
 */
class BranchEventBrokerITCase {
    private static final String TARGET_CLASS_NAME = BranchExamples.class.getName().replace(".", "/");

    @Test
    void unaryIntEquals() {
        assertAllProbesCovered(() -> BranchExamples.equality(1), () -> BranchExamples.equality(0));
    }

    @Test
    void binaryIntEquals() {
        assertAllProbesCovered(() -> BranchExamples.equality(1, 1), () -> BranchExamples.equality(90, 1));
    }

    @Test
    void isNull() {
        assertAllProbesCovered(() -> BranchExamples.isNull(null), () -> BranchExamples.isNull("hello"));
    }

    @Test
    void objectEquals() {
        Object o = "hello";
        assertAllProbesCovered(() -> BranchExamples.equality(o, "World"), () -> BranchExamples.equality(o, o));
    }

    private void assertAllProbesCovered(Runnable... r) {
        String className = BranchExamples.class.getName().replace('.', '/');
        int[] covered = new int[r.length];
        Arrays.fill(covered, -1);
        for (int i = 0; i < r.length; i++) {
            CoverageObserver observer = new CoverageObserver();
            try {
                observer.starting(new ByteArrayList());
                r[i].run();
            } finally {
                observer.finished();
            }
            covered[i] = checkCoverage(className, observer.getCoverageMap());
            Assertions.assertNotEquals(-1, covered[i]);
        }
        Assertions.assertTrue(IntStream.of(covered).min().orElse(0) >= 0);
        Assertions.assertEquals(covered.length, IntStream.of(covered).distinct().count());
    }

    private int checkCoverage(String className, boolean[][] coverageMap) {
        for (int classIndex = 0; classIndex < coverageMap.length; classIndex++) {
            if (ModelRegistry.get(classIndex).getName().equals(className)) {
                boolean[] coverage = coverageMap[classIndex];
                int found = -1;
                for (int probeIndex = 0; probeIndex < coverage.length; probeIndex++) {
                    if (coverage[probeIndex]) {
                        Assertions.assertEquals(-1, found);
                        found = probeIndex;
                    }
                }
                return found;
            }
        }
        return -1;
    }
}
