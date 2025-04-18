package edu.neu.ccs.prl.zeugma.internal.eval.heritability;

import edu.neu.ccs.prl.zeugma.internal.parametric.JqfRunner;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.FileUtil;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class HeritabilityEvaluationMain {
    /**
     * Number of samples to collect for each crossover operator.
     * <p>
     * Non-negative.
     */
    private static final int NUMBER_OF_SAMPLES = 1_000;
    /**
     * Maximum amount of time between the start of a fuzzing campaign and the time when an eligible parent input was
     * saved.
     * <p>
     * Non-negative.
     */
    private static final Duration DURATION = Duration.ofMinutes(5);
    /**
     * Name of the fuzzer whose corpora can be sampled from.
     * <p>
     * Non-null, non-empty.
     */
    private static final String FUZZER = "Zeugma-None";
    /**
     * Number of random inputs to run when computing "common" probes.
     * <p>
     * Non-negative.
     */
    private static final int COMMON_PROBE_SAMPLES = 1_000;
    /**
     * Percent of random inputs that can cover a probe before it is considered to be "common".
     * <p>
     * {@code COMMON_PROBE_THRESHOLD >= 0.0 && COMMON_PROBE_THRESHOLD <= 1.0}
     */
    private static final double COMMON_PROBE_THRESHOLD = 0.5;

    /**
     * Usage: HeritabilityEvaluationMain corpora_dir output_file
     * <p>
     * corpora_dir: path of the directory to scan for fuzzing campaign corpora
     * output_file: path of file to which the results should be written in CSV format.
     */
    public static void main(String[] args) throws Throwable {
        CorporaScanner scanner = new CorporaScanner(new File(args[0]));
        File outputFile = new File(args[1]);
        List<Corpus> corpora = scanner.scan();
        // Remove all corpora not created with the target fuzzer
        corpora.removeIf(c -> !FUZZER.equals(c.getFuzzer()));
        if (corpora.isEmpty()) {
            throw new IllegalArgumentException("No valid corpora found.");
        } else {
            System.out.printf("Found %d corpora.%n", corpora.size());
        }
        // Group the corpora by the fuzzing subject
        Map<String, List<Corpus>> groups =
                corpora.stream().collect(Collectors.groupingBy(Corpus::getSubject, Collectors.toList()));
        FileUtil.ensureDirectory(outputFile.getParentFile());
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, false))) {
            // Write the header to the output file
            writer.println("subject,crossover_operator,inheritance_rate,hybrid");
            // Process each fuzzing target
            for (List<Corpus> group : groups.values()) {
                processGroup(writer, group);
            }
        }
    }

    private static void processGroup(PrintWriter writer, List<Corpus> corpora)
            throws IOException, InitializationError, ClassNotFoundException {
        String subject = corpora.get(0).getSubject();
        System.out.println("Processing subject: " + subject);
        ClassLoader classLoader = HeritabilityEvaluationMain.class.getClassLoader();
        List<List<ByteList>> inputLists = new ArrayList<>();
        for (Corpus corpus : corpora) {
            inputLists.add(corpus.readInputs(DURATION));
        }
        HeritabilityEvaluator evaluator = new HeritabilityEvaluator(inputLists,
                writer,
                subject,
                NUMBER_OF_SAMPLES,
                COMMON_PROBE_THRESHOLD,
                COMMON_PROBE_SAMPLES);
        JqfRunner.createInstance(corpora.get(0).getTestClassName(),
                corpora.get(0).getTestMethodName(),
                classLoader,
                evaluator).run();
    }
}
