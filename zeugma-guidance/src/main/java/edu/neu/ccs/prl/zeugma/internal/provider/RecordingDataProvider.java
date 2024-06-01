package edu.neu.ccs.prl.zeugma.internal.provider;

import edu.neu.ccs.prl.zeugma.internal.guidance.DataProvider;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

public interface RecordingDataProvider extends DataProvider {
    ByteList getRecording();

    boolean close();
}
