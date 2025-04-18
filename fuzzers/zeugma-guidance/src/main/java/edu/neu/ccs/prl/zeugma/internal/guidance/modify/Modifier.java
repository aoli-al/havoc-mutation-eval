package edu.neu.ccs.prl.zeugma.internal.guidance.modify;

import edu.neu.ccs.prl.zeugma.internal.guidance.Individual;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

public interface Modifier<T extends Individual> {
    ByteList modify(T parent, SimpleList<? extends T> population);
}