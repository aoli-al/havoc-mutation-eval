package edu.neu.ccs.prl.zeugma.internal.util;

import java.util.Random;

public final class UnmodifiableByteList implements ByteList {
    private final ByteList delegate;

    public UnmodifiableByteList(ByteList delegate) {
        if (delegate == null) {
            throw new NullPointerException();
        }
        this.delegate = delegate;
    }

    @Override
    public void add(byte element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte get(int index) {
        return delegate.get(index);
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public byte set(int index, byte element) {
        if (index < 0 || index >= delegate.size()) {
            throw new IndexOutOfBoundsException();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] toArray() {
        return delegate.toArray();
    }

    @Override
    public void addAll(ByteList other) {
        if (other == null) {
            throw new NullPointerException();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public ByteList trim(int size) {
        return delegate.trim(size);
    }

    @Override
    public ByteList shuffle(Random random) {
        return delegate.shuffle(random);
    }

    @Override
    public ByteList subList(int start, int end) {
        return delegate.subList(start, end);
    }

    @Override
    public ByteList reverse() {
        return delegate.reverse();
    }

    @Override
    public ByteList concatenate(ByteList other) {
        return delegate.concatenate(other);
    }

    @Override
    public void addAll(byte[] elements) {
        if (elements == null) {
            throw new NullPointerException();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    public static UnmodifiableByteList of(ByteList list) {
        return new UnmodifiableByteList(list);
    }
}
