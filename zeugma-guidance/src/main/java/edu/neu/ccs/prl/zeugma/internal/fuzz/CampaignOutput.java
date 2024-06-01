package edu.neu.ccs.prl.zeugma.internal.fuzz;

import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.FileUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

/**
 * Maintains the directories and files written during a fuzzing campaign.
 */
public final class CampaignOutput {
    /**
     * Directory in which saving failure-inducing inputs are saved.
     * <p>
     * Non-null.
     */
    private final File failureDirectory;
    /**
     * Directory in which interesting inputs are saved.
     * <p>
     * Non-null.
     */
    private final File corpusDirectory;
    /**
     * File in which campaign statistics are written.
     * <p>
     * Non-null.
     */
    private final File statisticsFile;
    /**
     * Number of inputs saved to the corpus directory.
     * <p>
     * Non-negative.
     */
    private int corpusSize = 0;
    /**
     * Number of failure-inducing inputs saved to the failures directory.
     * <p>
     * Non-negative.
     */
    private int failuresSize = 0;

    public CampaignOutput(File outputDirectory) throws IOException {
        this.failureDirectory = getFailuresDirectory(outputDirectory);
        this.corpusDirectory = getCorpusDirectory(outputDirectory);
        this.statisticsFile = getStatisticsFile(outputDirectory);
        FileUtil.ensureEmptyDirectory(corpusDirectory);
        FileUtil.ensureEmptyDirectory(failureDirectory);
        if (statisticsFile.exists() && !statisticsFile.delete()) {
            throw new IOException("Failed to delete: " + statisticsFile);
        }
    }

    public synchronized void writeStatistics(String line) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(statisticsFile, true))) {
            out.println(line);
        }
    }

    public synchronized void saveToCorpus(ByteList input) throws IOException {
        saveInput(input, corpusDirectory, corpusSize++);
    }

    public synchronized void saveToFailures(ByteList input) throws IOException {
        saveInput(input, failureDirectory, failuresSize++);
    }

    public synchronized int getCorpusSize() {
        return corpusSize;
    }

    public synchronized int getFailuresSize() {
        return failuresSize;
    }

    private void saveInput(ByteList input, File directory, int id) throws IOException {
        File file = new File(directory, String.format("id_%06d.dat", id));
        Files.write(file.toPath(), input.toArray());
    }

    public static File getCorpusDirectory(File outputDirectory) {
        return new File(outputDirectory, "corpus");
    }

    public static File getFailuresDirectory(File outputDirectory) {
        return new File(outputDirectory, "failures");
    }

    public static File getStatisticsFile(File outputDirectory) {
        return new File(outputDirectory, "statistics.csv");
    }

    public static ByteList readInput(File file) throws IOException {
        return new ByteArrayList(Files.readAllBytes(file.toPath()));
    }
}