package edu.neu.ccs.prl.zeugma.internal.fuzz;

import edu.neu.ccs.prl.zeugma.internal.guidance.TestReport;

/**
 * Describes system behavior observed during the execution of a test.
 */
public final class TestObservation {
    private final TestReport report;
    private final boolean[][] coverageMap;

    public TestObservation(TestReport report, boolean[][] coverageMap) {
        if (report == null || coverageMap == null) {
            throw new NullPointerException();
        }
        this.report = report;
        this.coverageMap = coverageMap;
    }

    public TestReport getReport() {
        return report;
    }

    public boolean[][] getCoverageMap() {
        return coverageMap;
    }
}