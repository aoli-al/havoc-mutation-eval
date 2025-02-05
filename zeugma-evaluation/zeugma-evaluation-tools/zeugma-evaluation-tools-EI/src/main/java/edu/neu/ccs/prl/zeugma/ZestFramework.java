package edu.neu.ccs.prl.zeugma;

import edu.berkeley.cs.jqf.fuzz.ei.ExecutionIndexingGuidance;
import edu.berkeley.cs.jqf.fuzz.ei.ZestDriver;

public class ZestFramework extends edu.neu.ccs.prl.meringue.ZestFramework {
    @Override
    public String getMainClassName() {
        return ZestDriver.class.getName();
    }
    @Override
    protected String getJqfVersion() {
        return "2.1-ei";
    }
}
