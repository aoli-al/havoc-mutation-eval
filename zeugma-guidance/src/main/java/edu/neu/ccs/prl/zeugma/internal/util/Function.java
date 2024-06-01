package edu.neu.ccs.prl.zeugma.internal.util;

@FunctionalInterface
public interface Function<T, R> {
    R apply(T t);
}