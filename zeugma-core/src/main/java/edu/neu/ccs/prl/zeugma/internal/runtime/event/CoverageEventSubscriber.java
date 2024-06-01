package edu.neu.ccs.prl.zeugma.internal.runtime.event;

import edu.neu.ccs.prl.zeugma.internal.runtime.model.ClassModel;

public interface CoverageEventSubscriber {
    void cover(ClassModel model, int probeIndex);
}
