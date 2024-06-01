package edu.berkeley.cs.jqf.fuzz.rl;

import edu.neu.ccs.prl.meringue.CampaignConfiguration;
import edu.neu.ccs.prl.meringue.ZestFramework;

import java.io.File;
import java.util.Properties;

public class RLCheckFramework extends ZestFramework {
    @Override
    public String getMainClassName() {
        return "edu.berkeley.cs.jqf.fuzz.rl.RLDriver";
    }

    @Override
    protected String getJqfVersion() {
        return "1.2-SNAPSHOT-RLCheck";
    }

    @Override
    protected String[] getArguments(CampaignConfiguration configuration, Properties frameworkArguments) {
        String generator = frameworkArguments.getProperty("generator");
        if (generator == null) {
            throw new IllegalArgumentException("Missing RLCheck generator");
        }
        String path = frameworkArguments.getProperty("configuration");
        if (path == null) {
            throw new IllegalArgumentException("Missing RLCheck configuration");
        }
        File c = new File(path);
        if (!c.isFile()) {
            throw new IllegalArgumentException("RLCheck configuration is not a file: " + c);
        }
        return new String[]{configuration.getTestClassName(),
                configuration.getTestMethodName(),
                generator,
                c.getAbsolutePath(),
                configuration.getOutputDirectory().getAbsolutePath()};
    }
}
