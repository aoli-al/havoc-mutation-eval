package edu.neu.ccs.prl.zeugma.internal.provider;

public interface ByteProducer {
    /**
     * Returns the next element from this producer.
     *
     * @return the next element from this producer
     */
    byte next();
}