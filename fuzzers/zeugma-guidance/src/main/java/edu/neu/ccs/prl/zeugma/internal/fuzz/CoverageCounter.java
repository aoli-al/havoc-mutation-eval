package edu.neu.ccs.prl.zeugma.internal.fuzz;

import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;

public final class CoverageCounter {
    private final SimpleList<boolean[]> totalCoverageMap = new SimpleList<>();
    private long numberOfCoveredProbes = 0;

    public long getNumberOfCoveredProbes() {
        return numberOfCoveredProbes;
    }

    public boolean update(boolean[][] runCoverageMap) {
        boolean result = false;
        for (int classIndex = 0; classIndex < runCoverageMap.length; classIndex++) {
            boolean[] runCoverage = runCoverageMap[classIndex];
            if(classIndex >= totalCoverageMap.size()) {
                totalCoverageMap.add(new boolean[runCoverage.length]);
            }
            boolean[] totalCoverage = totalCoverageMap.get(classIndex);
            for (int probeIndex = 0; probeIndex < runCoverage.length; probeIndex++) {
                if (!totalCoverage[probeIndex] && runCoverage[probeIndex]) {
                    totalCoverage[probeIndex] = true;
                    result = true;
                    numberOfCoveredProbes++;
                }
            }
        }
        return result;
    }
}