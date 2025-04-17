package edu.neu.ccs.prl.zeugma.internal.eval.heritability;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

class CorporaScanner {
    private final File directory;

    public CorporaScanner(File directory) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(directory + " is not a directory");
        }
        this.directory = directory;
    }

    public List<Corpus> scan() throws IOException {
        List<Corpus> corpora = new ArrayList<>();
        Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String name = file.getFileName().toString();
                Corpus corpus = null;
                if (name.endsWith(".tgz")) {
                    corpus = ArchiveCorpus.create(file.toFile());
                } else if (name.equals(Corpus.SUMMARY_FILE_NAME)) {
                    corpus = DirectoryCorpus.create(file.getParent().toFile());
                }
                if (corpus != null) {
                    corpora.add(corpus);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return corpora;
    }
}