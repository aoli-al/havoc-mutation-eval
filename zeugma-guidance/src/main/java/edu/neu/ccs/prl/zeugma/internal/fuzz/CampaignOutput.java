package edu.neu.ccs.prl.zeugma.internal.fuzz;

import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.FileUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
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
    private final File genDirectory;
    /**
     * File in which campaign statistics are written.
     * <p>
     * Non-null.
     */
    private final File statisticsFile;

    private final File mutationLogFile;
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
        this.genDirectory = getGenDirectory(outputDirectory);
        this.statisticsFile = getStatisticsFile(outputDirectory);
        this.mutationLogFile = getMutationLogFile(outputDirectory);
        FileUtil.ensureEmptyDirectory(corpusDirectory);
        FileUtil.ensureEmptyDirectory(failureDirectory);
        FileUtil.ensureEmptyDirectory(genDirectory);
        if (statisticsFile.exists() && !statisticsFile.delete()) {
            throw new IOException("Failed to delete: " + statisticsFile);
        }
    }

    public synchronized void writeStatistics(String line) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(statisticsFile, true))) {
            out.println(line);
        }
    }

    public synchronized void writeMutationLog(String line) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(mutationLogFile, true))) {
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

    public void saveToGen(String serialized) throws IOException {
        File file = new File(genDirectory, String.format("id_%06d.txt", corpusSize));
        Files.write(file.toPath(), serialized.getBytes(StandardCharsets.UTF_8));
    }

    public static File getCorpusDirectory(File outputDirectory) {
        return new File(outputDirectory, "corpus_trial_controlled");
    }

    public static File getGenDirectory(File outputDirectory) {
        return new File(outputDirectory, "gen");
    }

    public static File getFailuresDirectory(File outputDirectory) {
        return new File(outputDirectory, "failures");
    }

    public static File getStatisticsFile(File outputDirectory) {
        return new File(outputDirectory, "statistics.csv");
    }

    public static File getMutationLogFile(File outputDirectory) {
        return new File(outputDirectory, "mutation.log");
    }

    public static ByteList readInput(File file) throws IOException {
        return new ByteArrayList(Files.readAllBytes(file.toPath()));
    }
}