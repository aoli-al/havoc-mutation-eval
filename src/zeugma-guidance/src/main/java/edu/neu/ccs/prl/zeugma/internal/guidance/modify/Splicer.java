package edu.neu.ccs.prl.zeugma.internal.guidance.modify;

import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

public interface Splicer {
    /**
     * Returns a new child list created based on the two specified parent lists. Does not modify either of the parent
     * lists.
     *
     * @param parent1 the first parent list
     * @param parent2 the second parent list
     * @return a new child list created based on the two specified parent lists
     * @throws NullPointerException if {@code parent1 == null} or {@code parent2 == null}
     */
    ByteList splice(ByteList parent1, ByteList parent2);
}