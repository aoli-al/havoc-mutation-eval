package edu.neu.ccs.prl.zeugma.internal.runtime.struct;

public interface Iterator<E> {
    E next();

    boolean hasNext();
}
