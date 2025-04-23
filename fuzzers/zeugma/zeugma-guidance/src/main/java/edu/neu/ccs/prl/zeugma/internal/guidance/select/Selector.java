package edu.neu.ccs.prl.zeugma.internal.guidance.select;

import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;

public interface Selector<T> {
    /**
     * Returns an element from the specified list.
     *
     * @return an element from the specified list.
     * @throws NullPointerException     if the specified list is {@code null}
     * @throws IllegalArgumentException if the specified list is empty
     */
    <R extends T> R select(SimpleList<R> list);
}