package edu.neu.ccs.prl.zeugma.internal.runtime.event;

import edu.neu.ccs.prl.zeugma.internal.runtime.model.MethodIdentifier;

public interface ContextEventSubscriber {
    int enteringMethod(MethodIdentifier method);

    void exitingMethod(int level);

    void restore(int level);
}
