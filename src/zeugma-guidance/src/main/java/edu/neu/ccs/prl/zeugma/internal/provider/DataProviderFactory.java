package edu.neu.ccs.prl.zeugma.internal.provider;

import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

@FunctionalInterface
public interface DataProviderFactory {
    RecordingDataProvider create(ByteList values);
}
