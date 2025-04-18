package edu.neu.ccs.prl.zeugma.internal.guidance;

import java.io.IOException;

public interface GuidanceBuilder {
    Guidance build(FuzzTarget target) throws IOException, ReflectiveOperationException;
}
