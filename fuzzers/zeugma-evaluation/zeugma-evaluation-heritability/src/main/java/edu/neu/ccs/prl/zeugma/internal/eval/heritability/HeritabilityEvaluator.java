package edu.neu.ccs.prl.zeugma.internal.eval.heritability;

import edu.neu.ccs.prl.zeugma.internal.guidance.*;
import edu.neu.ccs.prl.zeugma.internal.guidance.linked.LinkedIndividual;
import edu.neu.ccs.prl.zeugma.internal.guidance.linked.LinkedModifier;
import edu.neu.ccs.prl.zeugma.internal.guidance.modify.OnePointSplicer;
import edu.neu.ccs.prl.zeugma.internal.guidance.modify.TwoPointSplicer;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.Iterator;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.ObjectIntMap;
import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

final class HeritabilityEvaluator implements Guidance, GuidanceBuilder {
    private final List<List<ByteList>> corpora;
    private final PrintWriter writer;
    private final String subject;
    private final int numberOfSamples;
    private final Random random = ThreadLocalRandom.current();
    private final double commonProbeThreshold;
    private final int commonProbeSamples;
    private FuzzTarget target;

    HeritabilityEvaluator(List<List<ByteList>> corpora, PrintWriter writer, String subject, int numberOfSamples,
                          double commonProbeThreshold, int commonProbeSamples) {
        this.corpora = corpora;
        this.writer = writer;
        this.subject = subject;
        this.numberOfSamples = numberOfSamples;
        this.commonProbeSamples = commonProbeSamples;
        this.commonProbeThreshold = commonProbeThreshold;
    }

    private Set<ProbeIdentifier> computeCommonProbes() {
        System.out.println("Computing common probes...");
        ObjectIntMap<ProbeIdentifier> coveredCount = new ObjectIntMap<>();
        for (int i = 0; i < commonProbeSamples; i++) {
            AnnotatedIndividual individual = getIndividual(new ByteArrayList());
            for (ProbeIdentifier probe : individual.runCoverage) {
                int count = coveredCount.getOrDefault(probe, 0);
                coveredCount.put(probe, count + 1);
            }
        }
        Set<ProbeIdentifier> commonProbes = new HashSet<>();
        for (Iterator<ObjectIntMap.Entry<ProbeIdentifier>> itr = coveredCount.entryIterator(); itr.hasNext(); ) {
            ObjectIntMap.Entry<ProbeIdentifier> entry = itr.next();
            int count = entry.getValue();
            if (count > commonProbeThreshold * commonProbeSamples) {
                commonProbes.add(entry.getKey());
            }
        }
        System.out.printf("Marked %d/%d probes covered by random inputs as common.%n",
                commonProbes.size(),
                coveredCount.size());
        return commonProbes;
    }

    @Override
    public void fuzz() {
        Set<ProbeIdentifier> commonEdges = computeCommonProbes();
        for (int i = 0; i < numberOfSamples; i++) {
            if (i % 10 == 0) {
                System.out.printf("Computing sample %d/%d%n", i + 1, numberOfSamples);
            }
            AnnotatedIndividual parent1, parent2;
            Set<ProbeIdentifier> p1, p2;
            do {
                // Select a random corpus.
                List<ByteList> corpus = corpora.get(random.nextInt(corpora.size()));
                // Select two different random parents.
                ByteList list1 = corpus.get(random.nextInt(corpus.size()));
                ByteList list2;
                do {
                    list2 = corpus.get(random.nextInt(corpus.size()));
                } while (list1.equals(list2));
                parent1 = getIndividual(list1);
                parent2 = getIndividual(list2);
                p1 = difference(parent1.runCoverage, commonEdges);
                p2 = difference(parent2.runCoverage, commonEdges);
            } while (p1.containsAll(p2) || p2.containsAll(p1));
            record(p1,
                    p2,
                    new TwoPointSplicer(random, false).splice(parent1.getInput(), parent2.getInput()),
                    "Two Point");
            record(p1,
                    p2,
                    new OnePointSplicer(random, false).splice(parent1.getInput(), parent2.getInput()),
                    "One Point");
            record(p1, p2, LinkedModifier.splice(parent1, parent2, random), "Linked");
        }
    }

    private void record(Set<ProbeIdentifier> p1, Set<ProbeIdentifier> p2, ByteList input, String crossoverOperator) {
        AnnotatedIndividual child = getIndividual(input);
        writer.printf("%s,%s,%f,%s%n",
                subject,
                crossoverOperator,
                inheritanceRate(p1, p2, child.runCoverage),
                isHybrid(p1, p2, child.runCoverage));
    }

    @Override
    public Guidance build(FuzzTarget target) {
        this.target = target;
        return this;
    }

    AnnotatedIndividual getIndividual(ByteList input) {
        CoverageObserver observer = new CoverageObserver();
        TestReport report = new TestRunner(target, random, observer).run(input);
        AnnotatedIndividual individual = new AnnotatedIndividual(report.getRecording(), observer.getCoverageMap());
        individual.initialize(target);
        return individual;
    }

    static double inheritanceRate(Set<?> p1, Set<?> p2, Set<?> c) {
        Set<?> union = union(p1, p2);
        return intersection(union, c).size() / (1.0 * union.size());
    }

    static boolean isHybrid(Set<?> p1, Set<?> p2, Set<?> c) {
        return !intersection(c, difference(p1, p2)).isEmpty() && !intersection(c, difference(p2, p1)).isEmpty();
    }

    static <T> Set<T> union(Set<? extends T> s1, Set<? extends T> s2) {
        Set<T> result = new HashSet<>(s1);
        result.addAll(s2);
        return result;
    }

    static <T> Set<T> difference(Set<? extends T> s1, Set<? extends T> s2) {
        Set<T> result = new HashSet<>(s1);
        result.removeAll(s2);
        return result;
    }

    static <T> Set<T> intersection(Set<? extends T> s1, Set<? extends T> s2) {
        Set<T> result = new HashSet<>(s1);
        result.retainAll(s2);
        return result;
    }

    static final class AnnotatedIndividual extends LinkedIndividual {
        final Set<ProbeIdentifier> runCoverage = new HashSet<>();

        AnnotatedIndividual(ByteList input, boolean[][] coverageMap) {
            super(input, "");
            for (int classIndex = 0; classIndex < coverageMap.length; classIndex++) {
                for (int probeIndex = 0; probeIndex < coverageMap[classIndex].length; probeIndex++) {
                    if (coverageMap[classIndex][probeIndex]) {
                        runCoverage.add(new ProbeIdentifier(classIndex, probeIndex));
                    }
                }
            }
        }
    }
}
