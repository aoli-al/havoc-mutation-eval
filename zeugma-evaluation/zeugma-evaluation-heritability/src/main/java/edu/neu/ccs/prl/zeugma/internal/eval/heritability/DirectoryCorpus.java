package edu.neu.ccs.prl.zeugma.internal.eval.heritability;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

public class DirectoryCorpus extends Corpus {
    private final File directory;

    DirectoryCorpus(String summary, File directory) {
        super(summary);
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException();
        }
        this.directory = directory;
    }

    @Override
    protected List<TimestampedInput> readInputs() throws IOException {
        List<TimestampedInput> elements = new ArrayList<>();
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            ByteList values = new ByteArrayList(Files.readAllBytes(file.toPath()));
            long lastModified = file.lastModified();
            elements.add(new TimestampedInput(values, lastModified));
        }
        return elements;
    }

    public static Corpus create(File directory) throws IOException {
        File summaryFile = new File(directory, SUMMARY_FILE_NAME);
        File corpusDirectory = new File(directory, CORPUS_PREFIX);
        if (summaryFile.isFile() && corpusDirectory.isDirectory()) {
            return new DirectoryCorpus(new String(Files.readAllBytes(summaryFile.toPath())), corpusDirectory);
        }
        return null;
    }
}
