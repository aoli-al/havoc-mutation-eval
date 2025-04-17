package edu.neu.ccs.prl.zeugma.internal.guidance;

import edu.neu.ccs.prl.zeugma.internal.provider.RecordingDataProvider;

public interface FuzzTarget {
    int getMaxTraceSize();

    int getMaxInputSize();

    String getDescriptor();

    TestReport run(RecordingDataProvider provider);
}
