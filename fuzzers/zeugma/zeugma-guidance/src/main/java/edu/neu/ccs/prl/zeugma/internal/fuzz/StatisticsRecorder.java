package edu.neu.ccs.prl.zeugma.internal.fuzz;


import java.io.IOException;
import java.time.Duration;

/**
 * Write campaign statistics to a CSV file.
 */
final class StatisticsRecorder implements Timer.Listener {
    private final CampaignStatus status;
    private final CampaignOutput out;

    StatisticsRecorder(CampaignOutput out, CampaignStatus status) throws IOException {
        if (out == null || status == null) {
            throw new NullPointerException();
        }
        this.out = out;
        this.status = status;
        out.writeStatistics(createHeader());
    }

    @Override
    public void update(long elapsedTime) throws IOException {
        out.writeStatistics(createRow(elapsedTime));
    }

    private String createRow(long elapsedTime) {
        return String.format("%d,%d,%d,%d,%d,%d,%d",
                elapsedTime,
                status.getNumberOfCoveredProbes(),
                status.getNumberOfRegisteredProbes(),
                status.getNumberOfExecutions(),
                status.getCorpusSize(),
                status.getNumberOfSavedFailures(),
                (long) status.getMeanInputSize());
    }

    public String createHeader() {
        return "\"elapsed_time_ms\",\"covered_probes\",\"known_probes\",\"executions\"" +
                ",\"corpus_size\",\"saved_failures\",\"mean_input_size\"";
    }

    static long calculateRecordPeriod(long duration) {
        long period = duration / 2000;
        if (period < 10_000) {
            return 10_000;
        } else {
            return Math.min(period, Duration.ofMinutes(5).toMillis());
        }
    }
}
