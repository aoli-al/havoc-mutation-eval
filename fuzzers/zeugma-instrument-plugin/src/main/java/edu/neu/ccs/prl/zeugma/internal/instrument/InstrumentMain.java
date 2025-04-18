package edu.neu.ccs.prl.zeugma.internal.instrument;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public final class InstrumentMain {
    /**
     * Usage: InstrumentMain output_directory transformer_class core_jar [java_home]
     * <p>
     * output_directory: directory to which the instrumented JVM should be written.
     * transformer_class: fully-qualified name of the {@link ClassFileTransformer} that should be used to instrument
     * the JVM
     * core_jar: path to JAR file containing classes that need to be packed into the "java.base" module.
     * java_home: home directory of the JVM to be instrumented, if not specified the value of the environment
     * variable JAVA_HOME is used.
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        File outputDirectory = new File(args[0]);
        Class<?> transformerClass = Class.forName(args[1]);
        File coreJar = new File(args[2]);
        File javaHome = args.length == 3 ? null : new File(args[3]);
        instrument(outputDirectory, transformerClass, coreJar, javaHome);
    }

    public static void instrument(File outputDirectory, Class<?> transformerClass, File coreJar, File javaHome)
            throws IOException {
        if (!checkOutputDirectory(outputDirectory)) {
            javaHome = getJavaHome(javaHome);
            long startTime = System.currentTimeMillis();
            System.out.println("Generating instrumented JVM: " + outputDirectory);
            try {
                if (InstrumentUtil.isModularJvm(javaHome)) {
                    JLinkInvoker.invoke(javaHome,
                            outputDirectory,
                            Arrays.asList("--add-modules", "ALL-MODULE-PATH"),
                            transformerClass,
                            coreJar);
                } else {
                    new Instrumenter(createTransformer(transformerClass)).process(javaHome, outputDirectory);
                }
                System.out.println("Finished generation after " + (System.currentTimeMillis() - startTime) + " ms");
            } catch (IOException | InterruptedException | ExecutionException e) {
                throw new IOException("Failed to generate instrumented JVM", e);
            }
        }
    }

    private static boolean checkOutputDirectory(File outputDirectory) {
        if (InstrumentUtil.isJavaHome(outputDirectory)) {
            System.out.println("Existing instrumented JVM detected; no generation necessary.");
            return true;
        } else if (outputDirectory.exists()) {
            throw new IllegalArgumentException("Output directory exists but is not a JVM");
        }
        return false;
    }

    private static File getJavaHome(File javaHome) {
        if (javaHome == null && System.getenv("JAVA_HOME") != null) {
            javaHome = new File(System.getenv("JAVA_HOME"));
        }
        if (javaHome == null) {
            throw new IllegalArgumentException(
                    "Java home argument or the JAVA_HOME environmental variable must be " + "set");
        } else if (!InstrumentUtil.isJavaHome(javaHome)) {
            throw new IllegalArgumentException("Invalid Java home directory: " + javaHome);
        }
        return javaHome;
    }

    private static ClassFileTransformer createTransformer(Class<?> transformerClass) {
        try {
            return (ClassFileTransformer) transformerClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Failed to create transformer of type: " + transformerClass);
        }
    }
}
