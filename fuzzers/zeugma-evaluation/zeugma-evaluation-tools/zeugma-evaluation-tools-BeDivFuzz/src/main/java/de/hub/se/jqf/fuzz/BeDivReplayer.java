package de.hub.se.jqf.fuzz;

import de.hub.se.jqf.fuzz.repro.BeDivReproGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import edu.neu.ccs.prl.meringue.Replayer;
import edu.neu.ccs.prl.meringue.ReplayerManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class BeDivReplayer implements Replayer {
    private Class<?> testClass;
    private String testMethodName;

    @Override
    public void configure(String testClassName, String testMethodName, ClassLoader classLoader)
            throws ClassNotFoundException {
        if (testClassName == null || testMethodName == null) {
            throw new NullPointerException();
        }
        this.testMethodName = testMethodName;
        this.testClass = Class.forName(testClassName, true, classLoader);
    }

    @Override
    public void accept(ReplayerManager manager) throws IOException {
        while (manager.hasNextInput()) {
            File input = manager.nextInput();
            Throwable failure = execute(input);
            manager.handleResult(failure);
        }
    }

    private Throwable execute(File input) {
        BeDivReplayGuidance guidance = new BeDivReplayGuidance(input);
        try {
            GuidedFuzzing.run(testClass, testMethodName, guidance, System.out);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return guidance.error;
    }

    private static final class BeDivReplayGuidance extends BeDivReproGuidance {
        private Throwable error = null;
        private final String outputPath = System.getProperty("replay.directory");
        private static int outputIndex = 0;

        public BeDivReplayGuidance(File input) {
            super(input, null);
        }

        @Override
        public void observeGeneratedArgs(Object[] args) {
            super.observeGeneratedArgs(args);
            if (outputPath != null) {
                File folder = new File(outputPath + "/gen");
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                try (FileWriter f = new FileWriter(outputPath + "/gen/" + "id" + outputIndex++ + ".txt")) {
                    System.out.println("saving: " + outputPath);
                    f.write(args[0].toString());
                } catch (IOException e) {
                }
            }
        }

        @Override
        public void handleResult(Result result, Throwable error) {
            if (result == Result.FAILURE) {
                this.error = error;
            }
            super.handleResult(result, error);
        }
    }
}