package edu.neu.ccs.prl.zeugma.framework;

import edu.neu.ccs.prl.meringue.*;
import edu.neu.ccs.prl.zeugma.internal.agent.ZeugmaAgent;
import edu.neu.ccs.prl.zeugma.internal.agent.ZeugmaTransformer;
import edu.neu.ccs.prl.zeugma.internal.fuzz.CampaignOutput;
import edu.neu.ccs.prl.zeugma.internal.instrument.InstrumentMain;
import edu.neu.ccs.prl.zeugma.internal.instrument.InstrumentUtil;
import edu.neu.ccs.prl.zeugma.internal.parametric.GuidanceForkMain;
import edu.neu.ccs.prl.zeugma.internal.parametric.ZeugmaReplayer;
import edu.neu.ccs.prl.zeugma.internal.runtime.event.CoreMethodInfo;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ZeugmaFramework implements JarFuzzFramework {
    private CampaignConfiguration config;
    private Properties properties;
    private File frameworkJar;

    @Override
    public void initialize(CampaignConfiguration config, Properties properties) {
        this.config = config;
        this.properties = properties;
    }

    @Override
    public Process startCampaign() throws IOException {
        FileUtil.ensureEmptyDirectory(config.getOutputDirectory());
        return createLauncher(getJvmDirectory(frameworkJar.getParentFile())).withVerbose(true).launch();
    }

    @Override
    public File[] getCorpusFiles() {
        return CampaignOutput.getCorpusDirectory(config.getOutputDirectory()).listFiles();
    }

    @Override
    public File[] getFailureFiles() {
        return CampaignOutput.getFailuresDirectory(config.getOutputDirectory()).listFiles();
    }

    @Override
    public Class<? extends Replayer> getReplayerClass() {
        return ZeugmaReplayer.class;
    }

    @Override
    public Collection<File> getRequiredClassPathElements() {
        return Collections.singleton(frameworkJar);
    }

    private File getJvmDirectory(File directory) {
        File jvmDir;
        String path = properties.getProperty("jvm");
        jvmDir = path != null && !path.isEmpty() ? new File(path) : new File(directory, "jvm-inst");
        System.out.println("Using JVM directory: " + jvmDir);
        return jvmDir;
    }

    private JvmLauncher createLauncher(File jvmDir) throws IOException {
        List<String> options = new LinkedList<>(config.getJavaOptions());
        options.add(InstrumentUtil.getAgentOption(ZeugmaAgent.class));
        options.add(InstrumentUtil.getBootClassPathOption(CoreMethodInfo.class));
        options.add("-cp");
        options.add(String.join(File.pathSeparator,
                config.getTestClasspathJar().getAbsolutePath(),
                frameworkJar.getAbsolutePath()));
        InstrumentMain.instrument(jvmDir,
                ZeugmaTransformer.class,
                FileUtil.getClassPathElement(CoreMethodInfo.class),
                FileUtil.javaExecToJavaHome(config.getJavaExecutable()));
        File javaExec = FileUtil.javaHomeToJavaExec(jvmDir);
        String[] arguments = {config.getTestClassName(),
                config.getTestMethodName(),
                String.valueOf(config.getDuration().toMillis()),
                config.getOutputDirectory().getAbsolutePath()};
        return JvmLauncher.fromMain(javaExec,
                GuidanceForkMain.class.getName(),
                options.toArray(new String[0]),
                false,
                arguments,
                config.getWorkingDirectory(),
                config.getEnvironment());
    }

    @Override
    public String getCoordinate() {
        return "edu.neu.ccs.prl.zeugma:zeugma-parametric";
    }

    @Override
    public void setFrameworkJar(File frameworkJar) {
        this.frameworkJar = frameworkJar;
    }

    @Override
    public List<String> getAnalysisJavaOptions() {
        return List.of("-Dreplay.directory=" + config.getOutputDirectory().getAbsolutePath());
    }
}