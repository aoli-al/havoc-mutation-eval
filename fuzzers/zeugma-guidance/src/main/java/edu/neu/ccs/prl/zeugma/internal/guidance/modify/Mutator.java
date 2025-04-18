package edu.neu.ccs.prl.zeugma.internal.guidance.modify;

import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

public interface Mutator {
    /**
     * Returns a new child list created based on the specified parent list. Does not modify the parent list.
     *
     * @param parent the parent list
     * @return a new child list created based on the specified parent list
     * @throws NullPointerException     if the parent list is null
     * @throws IllegalArgumentException if {@code !isValid(parent)}
     */
    ByteList mutate(ByteList parent);

    boolean isValid(ByteList parent);
}