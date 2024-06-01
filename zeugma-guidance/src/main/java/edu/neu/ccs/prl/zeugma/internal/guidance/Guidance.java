package edu.neu.ccs.prl.zeugma.internal.guidance;

import java.io.IOException;

/**
 * Guides a fuzzing campaign based on feedback about observed system behaviors.
 */
public interface Guidance {
    void fuzz() throws IOException;
}