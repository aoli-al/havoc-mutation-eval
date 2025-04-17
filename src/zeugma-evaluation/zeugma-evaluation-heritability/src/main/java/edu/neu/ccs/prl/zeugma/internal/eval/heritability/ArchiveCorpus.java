package edu.neu.ccs.prl.zeugma.internal.eval.heritability;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

public class ArchiveCorpus extends Corpus {
    private final File archive;

    ArchiveCorpus(String summary, File archive) {
        super(summary);
        if (!archive.isFile()) {
            throw new IllegalArgumentException();
        }
        this.archive = archive;
    }

    @Override
    protected List<TimestampedInput> readInputs() throws IOException {
        List<TimestampedInput> elements = new ArrayList<>();
        try (TarArchiveInputStream in = new TarArchiveInputStream(new GzipCompressorInputStream(Files.newInputStream(
            archive.toPath())))) {
            TarArchiveEntry entry;
            while ((entry = in.getNextTarEntry()) != null) {
                if (entry.getName().contains(CORPUS_PREFIX) && entry.isFile()) {
                    ByteList values = new ByteArrayList(IOUtils.toByteArray(in));
                    long lastModified = entry.getLastModifiedDate().getTime();
                    elements.add(new TimestampedInput(values, lastModified));
                }
            }
        }
        return elements;
    }

    public static Corpus create(File archive) throws IOException {
        try (TarArchiveInputStream in = new TarArchiveInputStream(new GzipCompressorInputStream(Files.newInputStream(
            archive.toPath())))) {
            TarArchiveEntry entry;
            while ((entry = in.getNextTarEntry()) != null) {
                if (entry.isFile() && entry.getName().endsWith(File.separator + SUMMARY_FILE_NAME)) {
                    return new ArchiveCorpus(new String(IOUtils.toByteArray(in)), archive);
                }
            }
        }
        return null;
    }
}
