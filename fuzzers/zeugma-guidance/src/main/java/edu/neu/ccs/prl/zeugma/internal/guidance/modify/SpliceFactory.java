package edu.neu.ccs.prl.zeugma.internal.guidance.modify;

import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;

public interface SpliceFactory<T> {

    Mutator createSplice(T parent1, T parent2);

    boolean canSplice(T parent);

    String getCrossoverType();

    Mutator createSplice(T parent, SimpleList<? extends T> population);
}