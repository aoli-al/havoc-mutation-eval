package edu.neu.ccs.prl.zeugma.internal.parametric;

import edu.neu.ccs.prl.zeugma.internal.guidance.GuidanceBuilder;
import edu.neu.ccs.prl.zeugma.internal.guidance.ParametricGuidanceBuilder;

import java.io.File;

public final class GuidanceForkMain {
    private GuidanceForkMain() {
        throw new AssertionError();
    }

    public static void main(String[] args) {
        try {
            GuidanceBuilder builder = new ParametricGuidanceBuilder(Long.parseLong(args[2]), new File(args[3]));
            ClassLoader classLoader = GuidanceForkMain.class.getClassLoader();
            JqfRunner.createInstance(args[0], args[1], classLoader, builder)
                    .run();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }
    }
}