package edu.neu.ccs.prl.zeugma.internal.guidance;

import edu.neu.ccs.prl.zeugma.internal.runtime.event.CoverageEventBroker;
import edu.neu.ccs.prl.zeugma.internal.runtime.event.CoverageEventSubscriber;
import edu.neu.ccs.prl.zeugma.internal.runtime.model.ClassModel;
import edu.neu.ccs.prl.zeugma.internal.runtime.model.ModelRegistry;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

public class CoverageObserver implements TestObserver, CoverageEventSubscriber {
    @Override
    public void starting(ByteList input) {
        // Reset the coverage map
        ModelRegistry.resetCoverageMap();
        CoverageEventBroker.setSubscriber(this);
    }

    @Override
    public void finished() {
        CoverageEventBroker.setSubscriber(null);
    }

    @Override
    public void cover(ClassModel model, int probeIndex) {
        model.cover(probeIndex);
    }

    public boolean[][] getCoverageMap() {
        return ModelRegistry.getCoverageMap();
    }
}
