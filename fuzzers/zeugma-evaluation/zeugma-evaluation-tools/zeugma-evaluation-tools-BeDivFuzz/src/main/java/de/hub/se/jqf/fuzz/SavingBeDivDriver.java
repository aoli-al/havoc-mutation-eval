package de.hub.se.jqf.fuzz;

import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;

public class SavingBeDivDriver {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        try {
            String testClassName = args[0];
            String testMethodName = args[1];
            File outputDirectory = new File(args[2]);
            Guidance guidance = new SavingBeDivFuzzGuidance(testClassName + "#" + testMethodName, null,
                    null, outputDirectory, new Random());
            Locale.setDefault(Locale.US);
            GuidedFuzzing.run(testClassName, testMethodName, SavingBeDivDriver.class.getClassLoader(),
                    guidance, System.out);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }
}