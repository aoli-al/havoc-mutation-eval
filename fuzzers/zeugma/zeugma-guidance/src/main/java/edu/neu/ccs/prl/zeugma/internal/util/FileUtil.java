package edu.neu.ccs.prl.zeugma.internal.util;

import java.io.File;
import java.io.IOException;

public final class FileUtil {
    private FileUtil() {
        throw new AssertionError();
    }

    /**
     * Creates the specified directory if it does not already exist.
     *
     * @param dir the directory to create
     * @throws NullPointerException if {@code dir} is {@code null}
     * @throws IOException          if the specified directory did not already exist and was not successfully created
     * @throws SecurityException    a security manager exists and denies access to {@code dir}
     */
    public static void ensureDirectory(File dir) throws IOException {
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new IOException("Failed to create directory: " + dir);
        }
    }

    /**
     * Ensures that the specified directory exists and is empty.
     *
     * @param dir the directory to be created or emptied
     * @throws IOException if the specified directory could not be created or emptied
     */
    public static void ensureEmptyDirectory(File dir) throws IOException {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files == null) {
                throw new IOException("Unable to clear: " + dir);
            }
            for (File file : files) {
                if (!file.delete()) {
                    throw new IOException("Unable to delete existing file: " + file);
                }
            }
        } else {
            if (dir.exists() && !dir.delete()) {
                throw new IOException("Failed to delete: " + dir);
            }
        }
        ensureDirectory(dir);
    }
}
