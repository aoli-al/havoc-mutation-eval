package edu.neu.ccs.prl.zeugma;

import edu.berkeley.cs.jqf.fuzz.ei.ExecutionIndexingDriver;
import edu.berkeley.cs.jqf.fuzz.ei.ExecutionIndexingGuidance;
import edu.neu.ccs.prl.meringue.ZestFramework;

public class EIFramework extends ZestFramework {
    @Override
    public String getMainClassName() {
        return ExecutionIndexingDriver.class.getName();
    }
    @Override
    protected String getJqfVersion() {
        return "2.1-ei";
    }
}
