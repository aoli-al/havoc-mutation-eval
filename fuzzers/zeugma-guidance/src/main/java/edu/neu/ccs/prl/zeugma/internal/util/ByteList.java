package edu.neu.ccs.prl.zeugma.internal.util;

import java.util.Random;

public interface ByteList {
    /**
     * Adds the specified element to the end of this list.
     *
     * @param element the element to add
     * @throws UnsupportedOperationException if the {@code add} operation is not supported by this list
     */
    void add(byte element);

    /**
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range {@code index < 0 || index >= size()}
     */
    byte get(int index);

    /**
     * @return true if this list contains no elements
     */
    boolean isEmpty();

    /**
     * @return the number of elements in this list
     */
    int size();

    /**
     * Replaces the element at the specified position in this list with the specified element.
     *
     * @param index   index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException     if the index is out of range {@code index < 0 || index >= size()}
     * @throws UnsupportedOperationException if the {@code set} operation is not supported by this list
     */
    byte set(int index, byte element);

    /**
     * Returns an array containing the elements of this list in the order that they appear in this list.
     *
     * @return an array containing the elements of this list in the order that they appear in this list
     */
    byte[] toArray();

    /**
     * Adds the elements of the specified other list to the end of this list.
     *
     * @param other the list whose elements are to be added to this list
     * @throws NullPointerException          if {@code other} is null
     * @throws UnsupportedOperationException if the {@code addAll} operation is not supported by this list
     */
    void addAll(ByteList other);


    /**
     * Returns a copy of this list cut to the specified size by removing any elements at indices greater than or equal
     * to the specified size.
     *
     * @param size the size that the list is to be cut to
     * @return a copy of this list cut to the specified size
     * @throws IllegalArgumentException if the specified size is larger than the current size of this list or the
     *                                  specified size is negative
     */
    ByteList trim(int size);

    /**
     * Returns a list containing a random permutation of the elements in this list using the specified source of
     * randomness. Every permutation is approximately equally likely.
     *
     * @param random the source of randomness
     * @return a list containing a random permutation of the elements in this list
     * @throws NullPointerException if the specified source of randomness is {@code null}.
     */
    ByteList shuffle(Random random);

    /**
     * Returns a copy of the portion of this list between the specified start (inclusive) and end (exclusive) indices.
     *
     * @param start the start (inclusive) of the portion of this list to be returned
     * @param end   the end (exclusive) of the portion of this list to be returned
     * @return a copy of the portion of this list between the specified start (inclusive) and end (exclusive) indices
     * @throws IndexOutOfBoundsException if {@code start < 0, start > end} or {@code end > this.size()}
     */
    ByteList subList(int start, int end);


    /**
     * Returns a copy of this list with its elements in reverse order.
     *
     * @return a copy of this list with its elements in reverse order
     */
    ByteList reverse();

    /**
     * Return a new list containing the elements of this list followed by the elements of the specified other list.
     *
     * @param other the list o be concatenated with this list
     * @return a new list containing the elements of this list followed by the elements of the specified other list
     * @throws NullPointerException if {@code other} is {@code null}
     */
    ByteList concatenate(ByteList other);

    /**
     * Adds the specified elements to the end of this list.
     *
     * @param elements elements be added to this list
     * @throws NullPointerException          if {@code elements} is {@code null}
     * @throws UnsupportedOperationException if the {@code addAll} operation is not supported by this list
     */
    void addAll(byte[] elements);
}