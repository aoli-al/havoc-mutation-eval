package edu.neu.ccs.prl.zeugma.internal.util;

import java.util.Random;

public final class ByteArrayList implements ByteList {
    private int size = 0;
    private byte[] elements;

    /**
     * Constructs a new list with an initial capacity of 10.
     */
    public ByteArrayList() {
        this(10);
    }

    /**
     * Constructs a new list with the specified initial capacity.
     *
     * @param capacity the initial capacity of this list.
     * @throws IllegalArgumentException if capacity is less than 0
     */
    public ByteArrayList(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException();
        }
        elements = new byte[capacity];
    }

    /**
     * Constructs a list containing the elements of the specified list.
     *
     * @param list list containing the initial element of the list to be constructed
     * @throws NullPointerException if list is null
     */
    public ByteArrayList(ByteList list) {
        this(list.toArray());
    }

    /**
     * Constructs a new list containing the specified initial elements.
     *
     * @param elements the initial elements of the list
     * @throws NullPointerException if elements is null
     */
    public ByteArrayList(byte[] elements) {
        this.size = elements.length;
        this.elements = elements.clone();
    }

    /**
     * Adds the specified element to the end of this list.
     *
     * @param element the element to add
     */
    @Override
    public void add(byte element) {
        if (size == elements.length) {
            grow(size + 1);
        }
        elements[size++] = element;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return elements[index];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Replaces the element at the specified position in this list with the specified element.
     *
     * @param index   index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range {@code index < 0 || index >= size()}
     */
    @Override
    public byte set(int index, byte element) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        byte result = elements[index];
        elements[index] = element;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] toArray() {
        byte[] result = new byte[size];
        System.arraycopy(elements, 0, result, 0, result.length);
        return result;
    }

    /**
     * Adds the elements of the specified other list to the end of this list.
     *
     * @param other the list whose elements are to be added to this list
     * @throws NullPointerException if {@code other} is null
     */
    @Override
    public void addAll(ByteList other) {
        for (int i = 0; i < other.size(); i++) {
            add(other.get(i));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ByteArrayList trim(int size) {
        if (size < 0 || size > this.size) {
            throw new IllegalArgumentException();
        }
        byte[] copy = new byte[size];
        System.arraycopy(elements, 0, copy, 0, copy.length);
        return new ByteArrayList(copy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ByteArrayList shuffle(Random random) {
        ByteArrayList copy = new ByteArrayList(this);
        for (int i = 0; i < copy.size; i++) {
            int j = random.nextInt(i + 1);
            byte temp = copy.elements[i];
            copy.elements[i] = copy.elements[j];
            copy.elements[j] = temp;
        }
        return copy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ByteArrayList subList(int start, int end) {
        ByteArrayList result = new ByteArrayList();
        if (start < 0 || start > end || end > size()) {
            throw new IndexOutOfBoundsException();
        }
        for (int i = start; i < end; i++) {
            result.add(get(i));
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ByteArrayList reverse() {
        ByteArrayList result = new ByteArrayList();
        for (int i = size() - 1; i >= 0; i--) {
            result.add(get(i));
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ByteArrayList concatenate(ByteList other) {
        ByteArrayList result = new ByteArrayList(this);
        result.addAll(other);
        return result;
    }

    /**
     * Adds the specified elements to the end of this list.
     *
     * @param elements elements be added to this list
     * @throws NullPointerException if {@code elements} is null
     */
    @Override
    public void addAll(byte[] elements) {
        for (byte element : elements) {
            add(element);
        }
    }

    private void grow(int minCapacity) {
        int oldCapacity = elements.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }
        if (newCapacity - (Integer.MAX_VALUE - 8) > 0) {
            newCapacity = Integer.MAX_VALUE - 8;
        }
        byte[] temp = elements;
        elements = new byte[newCapacity];
        System.arraycopy(temp, 0, elements, 0, temp.length);
    }

    @Override
    public int hashCode() {
        int result = size();
        for (int i = 0; i < size(); i++) {
            result = 31 * result + ((int) get(i));
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof ByteList)) {
            return false;
        }
        ByteList that = (ByteList) o;
        if (size() != that.size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (get(i) != that.get(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(size() * 16).append('[');
        for (int i = 0; i < size(); i++) {
            buffer.append(get(i));
            if (i != size() - 1) {
                buffer.append(", ");
            }
        }
        return buffer.append(']').toString();
    }

    /**
     * Returns a sequential, ordered list of bytes from the specified start (inclusive) to the specified end
     * (exclusive).
     *
     * @param start the inclusive first value
     * @param end   the exclusive upper bound
     * @return a sequential, ordered list of bytes from the specified start (inclusive) to the specified end (exclusive)
     */
    public static ByteArrayList range(byte start, byte end) {
        ByteArrayList result = new ByteArrayList();
        for (byte i = start; i < end; i++) {
            result.add(i);
        }
        return result;
    }
}