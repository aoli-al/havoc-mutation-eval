package edu.neu.ccs.prl.zeugma.internal.util;

import java.util.Random;

public final class SynchronizedByteList implements ByteList {
    private final ByteList delegate;

    public SynchronizedByteList(ByteList delegate) {
        if (delegate == null) {
            throw new NullPointerException();
        }
        this.delegate = delegate;
    }

    @Override
    public synchronized void add(byte element) {
        delegate.add(element);
    }

    @Override
    public synchronized byte get(int index) {
        return delegate.get(index);
    }

    @Override
    public synchronized boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public synchronized int size() {
        return delegate.size();
    }

    @Override
    public synchronized byte set(int index, byte element) {
        return delegate.set(index, element);
    }

    @Override
    public synchronized byte[] toArray() {
        return delegate.toArray();
    }

    @Override
    public synchronized void addAll(ByteList other) {
        delegate.addAll(other);
    }

    @Override
    public synchronized ByteList trim(int size) {
        return delegate.trim(size);
    }

    @Override
    public synchronized ByteList shuffle(Random random) {
        return delegate.shuffle(random);
    }

    @Override
    public synchronized ByteList subList(int start, int end) {
        return delegate.subList(start, end);
    }

    @Override
    public synchronized ByteList reverse() {
        return delegate.reverse();
    }

    @Override
    public synchronized ByteList concatenate(ByteList other) {
        return delegate.concatenate(other);
    }

    @Override
    public synchronized void addAll(byte[] elements) {
        for (byte element : elements) {
            delegate.add(element);
        }
    }

    @Override
    public synchronized int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public synchronized boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public synchronized String toString() {
        return delegate.toString();
    }

    public static SynchronizedByteList of(ByteList list) {
        return new SynchronizedByteList(list);
    }
}
