package edu.neu.ccs.prl.zeugma.internal.fuzz;

import java.io.Console;
import java.time.Duration;

/**
 * Formats campaign statistics and prints them to the console.
 */
final class StatusScreen implements Timer.Listener {
    /**
     * Maximum number of hours digits for elapsed times.
     * <p>
     * Non-negative.
     */
    private final int maxHourDigits;
    /**
     * Maximum amount of time to execute the campaign for in milliseconds.
     * <p>
     * Non-negative.
     */
    private final long duration;
    /**
     * Text representation of the test being fuzzed.
     * <p>
     * Non-null.
     */
    private final String testDescriptor;
    /**
     * Progress of the fuzzing campaign.
     * <p>
     * Non-null.
     */
    private final CampaignStatus status;

    StatusScreen(String testDescriptor, long duration, CampaignStatus status) {
        if (testDescriptor == null || status == null) {
            throw new NullPointerException();
        }
        if (duration < 0) {
            throw new IllegalArgumentException();
        }
        this.duration = duration;
        this.testDescriptor = testDescriptor;
        this.status = status;
        long maxHours = Duration.ofMillis(duration).toHours();
        this.maxHourDigits = maxHours == 0 ? 0 : Long.toString(maxHours).length();
    }

    private String formatElapsedTime(long elapsedTime) {
        return String.format("%s/%s", formatTime(elapsedTime, maxHourDigits), formatTime(duration, maxHourDigits));
    }

    @Override
    public void update(long elapsedTime) {
        String[] lines = formatAll(elapsedTime);
        Console console = System.console();
        if (console != null) {
            console.printf("\033[2J"); // Clear the entire screen
            console.printf("\033[H"); // Move the cursor to home position
            for (String line : lines) {
                console.printf("%s", line);
            }
        } else {
            for (String line : lines) {
                System.out.print(line);
            }
        }
    }

    private String[] formatAll(long elapsedTime) {
        if (elapsedTime == 0) {
            elapsedTime = 1;
        }
        return new String[]{String.format("          Test: %s%n", testDescriptor),
                String.format("  Elapsed Time: %s%n", formatElapsedTime(elapsedTime)),
                String.format("Covered Probes: %,d (%.2f%% of known probes)%n",
                        status.getNumberOfCoveredProbes(),
                        formatPercent(status.getNumberOfCoveredProbes(), status.getNumberOfRegisteredProbes())),
                String.format("%d execs | %d exec/s | %d saved | %d failures | %d mean size%n",
                        status.getNumberOfExecutions(),
                        status.getNumberOfExecutions() * 1000L / elapsedTime,
                        status.getCorpusSize(),
                        status.getNumberOfSavedFailures(),
                        (long) status.getMeanInputSize())};
    }

    private static String formatTime(long time, int maxHourDigits) {
        Duration d = Duration.ofMillis(time);
        long hours = d.toHours();
        long minutes = d.toMinutes() % 60;
        long seconds = d.getSeconds() % 60;
        long milliseconds = time % 1000;
        if (hours > 0 && Long.toString(hours).length() > maxHourDigits) {
            return String.format("%d:%02d:%02d:%03d", hours, minutes, seconds, milliseconds);
        } else if (maxHourDigits == 0) {
            return String.format("%02d:%02d:%03d", minutes, seconds, milliseconds);
        } else {
            return String.format("%0" + maxHourDigits + "d:%02d:%02d:%03d", hours, minutes, seconds, milliseconds);
        }
    }

    private static double formatPercent(long numerator, long denominator) {
        return denominator == 0 ? 0 : (100.0 * numerator) / denominator;
    }

    static long calculateRecordPeriod() {
        return System.console() == null ? 30_000 : 100;
    }
}