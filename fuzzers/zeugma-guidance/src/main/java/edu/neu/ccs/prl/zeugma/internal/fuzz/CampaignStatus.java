package edu.neu.ccs.prl.zeugma.internal.fuzz;

/**
 * Tracks progress of a fuzzing campaign.
 */
public interface CampaignStatus {
    long getNumberOfRegisteredProbes();

    long getNumberOfCoveredProbes();

    long getCorpusSize();

    long getNumberOfSavedFailures();

    long getNumberOfExecutions();

    double getMeanInputSize();
}
