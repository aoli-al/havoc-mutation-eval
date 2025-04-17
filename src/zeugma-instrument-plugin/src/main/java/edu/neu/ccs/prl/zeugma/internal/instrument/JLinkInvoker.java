package edu.neu.ccs.prl.zeugma.internal.instrument;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class JLinkInvoker {
    private JLinkInvoker() {
        throw new AssertionError();
    }

    public static void invoke(File javaHome, File outputDirectory, List<String> jlinkOptions, Class<?> transformerClass,
                              File coreJar) throws InterruptedException, IOException {
        String jlinkAgentJar = InstrumentUtil.getClassPathElement(JLinkRegistrationAgent.class).getAbsolutePath();
        List<String> command = new ArrayList<>();
        command.add(InstrumentUtil.javaHomeToJLinkExec(javaHome).getAbsolutePath());
        command.add("-J-javaagent:" + jlinkAgentJar);
        command.add("-J--class-path=" + InstrumentUtil.getClassPathElement(transformerClass).getAbsolutePath() +
                File.pathSeparator + coreJar.getAbsolutePath());
        command.add("-J--add-reads=" + JLinkRegistrationAgent.MODULE_NAME + "=ALL-UNNAMED");
        command.add("-J--module-path=" + jlinkAgentJar);
        command.add("-J--add-modules=" + JLinkRegistrationAgent.MODULE_NAME);
        command.add(getPluginOption(transformerClass, coreJar));
        command.add("--output=" + outputDirectory.getAbsolutePath());
        command.addAll(processJLinkOptions(jlinkOptions));
        ProcessBuilder builder = new ProcessBuilder(command);
        System.out.println(String.join(" ", builder.command()));
        Process process = builder.inheritIO().start();
        if (process.waitFor() != 0) {
            throw new RuntimeException("Failed to create instrumented runtime image");
        }
    }

    private static List<String> processJLinkOptions(List<String> jlinkOptions) {
        jlinkOptions = new ArrayList<>(jlinkOptions);
        Set<String> modules =
                new LinkedHashSet<>(Arrays.asList("java.base", "jdk.jdwp.agent", "java.instrument", "jdk.unsupported"));
        int i = jlinkOptions.indexOf("--add-modules");
        if (i != -1) {
            modules.addAll(Arrays.asList(jlinkOptions.get(i + 1).split(",")));
            jlinkOptions.set(i + 1, String.join(",", modules));
        } else {
            jlinkOptions.add("--add-modules");
            jlinkOptions.add(String.join(",", modules));
        }
        return jlinkOptions;
    }

    private static String getPluginOption(Class<?> transformerClass, File coreJar) {
        return String.format("--zeugma-instrument=x:transformer=%s:core=%s",
                transformerClass.getName(),
                coreJar.getAbsolutePath());
    }
}