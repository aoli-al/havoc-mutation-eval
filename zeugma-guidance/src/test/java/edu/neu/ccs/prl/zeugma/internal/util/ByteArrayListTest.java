package edu.neu.ccs.prl.zeugma.internal.util;

import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ByteArrayListTest {
    private static final byte[][] values = new byte[][]{
            new byte[0],
            new byte[2],
            new byte[]{0, 1, 2, 3, 4},
            new byte[]{-1, 3}
    };

    @Test
    public void testNewList() {
        ByteList list = new ByteArrayList();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    @Test
    public void testNewListValidCapacity() {
        ByteList list = new ByteArrayList(10);
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    @Test
    public void testNewListZeroCapacity() {
        ByteList list = new ByteArrayList(0);
        assertTrue(list.isEmpty());
        list.add((byte) 1);
        assertEquals(1, list.size());
    }

    @Test
    public void testNewListInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new ByteArrayList(-1));
    }

    @Test
    public void testCopyConstructor() {
        ByteList list = new ByteArrayList();
        list.add((byte) 1);
        list.add((byte) 2);
        ByteList copy = new ByteArrayList(list);
        assertEquals(list, copy);
        copy.add((byte) 3);
        assertEquals(2, list.size());
        assertEquals(3, copy.size());
    }

    @Test
    public void testCopyConstructorNull() {
        assertThrows(NullPointerException.class, () -> new ByteArrayList((ByteList) null));
    }

    @Test
    public void testArrayConstructor() {
        byte[] bytes = new byte[]{0, 1, 2};
        ByteList list = new ByteArrayList(bytes);
        assertEquals(3, list.size());
        bytes[0] = 7;
        assertEquals(0, list.get(0));
        list.set(1, (byte) 9);
        assertEquals(9, list.get(1));
        assertArrayEquals(new byte[]{7, 1, 2}, bytes);
    }

    @Test
    public void testArrayConstructorNull() {
        assertThrows(NullPointerException.class, () -> new ByteArrayList((byte[]) null));
    }

    @Test
    public void testAddGet() {
        ByteList list = new ByteArrayList();
        for (byte b = 0; b < 100; b++) {
            list.add(b);
        }
        assertEquals(100, list.size());
        for (byte b = 0; b < 100; b++) {
            assertEquals(b, list.get(b));
        }
    }

    @Test
    public void testGetOutOfBounds() {
        ByteList list = new ByteArrayList();
        for (byte b = 0; b < 100; b++) {
            list.add(b);
        }
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(100));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
    }

    @Test
    public void testSet() {
        ByteList list = new ByteArrayList(new byte[]{0, 1, 2, 3});
        list.set(0, (byte) 5);
        list.set(3, (byte) 25);
        assertEquals(new ByteArrayList(new byte[]{5, 1, 2, 25}), list);
    }

    @Test
    public void testSetOutOfBounds() {
        ByteList list = new ByteArrayList(new byte[]{0, 1});
        assertThrows(IndexOutOfBoundsException.class, () -> list.set(2, (byte) 0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.set(-1, (byte) 0));
    }

    @Test
    public void testSize() {
        ByteList list = new ByteArrayList();
        for (byte b = 0; b < 100; b++) {
            assertEquals(b, list.size());
            list.add(b);
        }
    }

    @Test
    public void testIsEmpty() {
        ByteList list = new ByteArrayList();
        assertTrue(list.isEmpty());
        list.add((byte) 0);
        assertFalse(list.isEmpty());
    }

    @Test
    public void trim() {
        ByteList list = new ByteArrayList(new byte[]{0, 1, 2, 3, 4, 5});
        list = list.trim(4);
        assertEquals(4, list.size());
        assertArrayEquals(new byte[]{0, 1, 2, 3}, list.toArray());
    }

    @Test
    public void trimToZero() {
        ByteList list = new ByteArrayList(new byte[]{0, 1, 2});
        list = list.trim(0);
        assertEquals(0, list.size());
    }

    @Test
    public void trimOutOfBounds() {
        ByteList list = new ByteArrayList(new byte[]{0, 1, 2});
        assertThrows(IllegalArgumentException.class, () -> list.trim(4));
    }

    @ParameterizedTest
    @MethodSource("arguments")
    public void testTrimToLength(byte[] values) {
        ByteList list = new ByteArrayList(values);
        ByteList copy = new ByteArrayList(values);
        list = list.trim(values.length);
        assertEquals(copy, list);
    }

    @Test
    public void trimNegativeSize() {
        ByteList list = new ByteArrayList(new byte[]{0, 1, 2});
        assertThrows(IllegalArgumentException.class, () -> list.trim(-1));
    }

    @Test
    public void testToStringEmpty() {
        ByteList list = new ByteArrayList();
        assertEquals("[]", list.toString());
    }

    @Test
    public void testToStringSingleton() {
        ByteList list = new ByteArrayList();
        list.add((byte) 0);
        assertEquals("[0]", list.toString());
    }

    @Test
    public void testToString() {
        ByteList list = new ByteArrayList();
        list.add((byte) 0);
        list.add((byte) 1);
        list.add((byte) 2);
        assertEquals("[0, 1, 2]", list.toString());
    }

    @ParameterizedTest
    @MethodSource("argumentPairs")
    public void testEqualsConsistency(byte[] b1, byte[] b2) {
        ByteList list1 = new ByteArrayList(b1);
        ByteList list2 = new ByteArrayList(b2);
        assertEquals(list1.equals(list2), list1.equals(list2));
    }

    @ParameterizedTest
    @MethodSource("argumentTriples")
    public void testEqualsTransitivity(byte[] b1, byte[] b2, byte[] b3) {
        ByteList list1 = new ByteArrayList(b1);
        ByteList list2 = new ByteArrayList(b2);
        ByteList list3 = new ByteArrayList(b3);
        if (list1.equals(list2) && list2.equals(list3)) {
            assertEquals(list1, list3);
        }
    }

    @ParameterizedTest
    @MethodSource("argumentPairs")
    public void testEqualsSymmetry(byte[] b1, byte[] b2) {
        ByteList list1 = new ByteArrayList(b1);
        ByteList list2 = new ByteArrayList(b2);
        assertEquals(list1.equals(list2), list2.equals(list1));
    }

    @ParameterizedTest
    @MethodSource("arguments")
    public void testEqualsReflexivity(byte[] b1) {
        ByteList list1 = new ByteArrayList(b1);
        assertEquals(list1, list1);
    }

    @ParameterizedTest
    @MethodSource("arguments")
    public void testEqualsNonNull(byte[] b1) {
        ByteList list1 = new ByteArrayList(b1);
        assertNotEquals(null, list1);
    }

    @ParameterizedTest
    @MethodSource("arguments")
    public void testHashCodeInternalConsistency(byte[] b1) {
        ByteList list1 = new ByteArrayList(b1);
        assertEquals(list1.hashCode(), list1.hashCode());
    }

    @ParameterizedTest
    @MethodSource("argumentPairs")
    public void testHashCodeEqualsConsistency(byte[] b1, byte[] b2) {
        ByteList list1 = new ByteArrayList(b1);
        ByteList list2 = new ByteArrayList(b2);
        if (list1.equals(list2)) {
            assertEquals(list1.hashCode(), list2.hashCode());
        }
    }

    @ParameterizedTest
    @MethodSource("arguments")
    public void testToArrayRoundTrip(byte[] values) {
        ByteList list = new ByteArrayList(values);
        assertArrayEquals(values, list.toArray());
    }

    @ParameterizedTest
    @MethodSource("arguments")
    public void testShufflePreservesFrequencies(byte[] values) {
        ByteList list = new ByteArrayList(values);
        int[] frequencies = computeFrequencies(values);
        for (int i = 0; i < 10; i++) {
            list = list.shuffle(ThreadLocalRandom.current());
            Assertions.assertArrayEquals(frequencies, computeFrequencies(list.toArray()));
        }
    }

    private static int[] computeFrequencies(byte[] values) {
        int[] frequencies = new int[Byte.MAX_VALUE - Byte.MIN_VALUE];
        for (int value : values) {
            frequencies[value - Byte.MIN_VALUE]++;
        }
        return frequencies;
    }

    private static Stream<Arguments> arguments() {
        List<Arguments> arguments = new LinkedList<>();
        for (byte[] value : values) {
            arguments.add(Arguments.of(value));
        }
        return arguments.stream();
    }

    private static Stream<Arguments> argumentPairs() {
        List<Arguments> arguments = new LinkedList<>();
        for (byte[] value1 : values) {
            for (byte[] value2 : values) {
                arguments.add(Arguments.of(value1, value2));
            }
        }
        return arguments.stream();
    }

    private static Stream<Arguments> argumentTriples() {
        List<Arguments> arguments = new LinkedList<>();
        for (byte[] value1 : values) {
            for (byte[] value2 : values) {
                for (byte[] value3 : values) {
                    arguments.add(Arguments.of(value1, value2, value3));
                }
            }
        }
        return arguments.stream();
    }
}