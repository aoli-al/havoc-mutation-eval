package edu.neu.ccs.prl.zeugma.internal.runtime.struct;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ObjectIntMapTest {
    @ParameterizedTest
    @MethodSource("maps")
    void isEmptyConsistentWithSize(ObjectIntMap<String> map) {
        Assertions.assertEquals(map.size() == 0, map.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("maps")
    void isEmptyTrueAfterClear(ObjectIntMap<String> map) {
        map.clear();
        Assertions.assertTrue(map.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("maps")
    void isEmptyFalseAfterPut(ObjectIntMap<String> map) {
        map.put("Hello", 0);
        Assertions.assertFalse(map.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("maps")
    void clearConsistentWithSize(ObjectIntMap<String> map) {
        map.clear();
        Assertions.assertEquals(0, map.size());
    }

    @ParameterizedTest
    @MethodSource("mapKeyPairs")
    void putThenContainsKeyTrue(ObjectIntMap<String> map, String key) {
        map.put(key, 100);
        Assertions.assertTrue(map.containsKey(key));
    }

    @ParameterizedTest
    @MethodSource("mapKeyPairs")
    void removeThenContainsKeyFalse(ObjectIntMap<String> map, String key) {
        if (map.containsKey(key)) {
            map.remove(key);
        }
        Assertions.assertFalse(map.containsKey(key));
    }

    @Test
    public void testNewMap() {
        ObjectIntMap<Object> map = new ObjectIntMap<>();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    public void newMapValidCapacity() {
        ObjectIntMap<Object> map = new ObjectIntMap<>(10);
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    public void newMapZeroCapacity() {
        ObjectIntMap<Object> map = new ObjectIntMap<>(0);
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    public void newMapInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new ObjectIntMap<>(-1));
    }

    @Test
    public void copyConstructor() {
        ObjectIntMap<Integer> map = new ObjectIntMap<>();
        map.put(1, 1);
        map.put(2, 2);
        ObjectIntMap<Integer> copy = new ObjectIntMap<>(map);
        assertEquals(map, copy);
        copy.put(3, 3);
        assertEquals(2, map.size());
        assertEquals(3, copy.size());
    }

    @Test
    public void copyConstructorNull() {
        assertThrows(NullPointerException.class, () -> new ObjectIntMap<>((ObjectIntMap<?>) null));
    }

    @Test
    public void putGet() {
        ObjectIntMap<Integer> map = new ObjectIntMap<>();
        for (int b = 0; b < 100; b++) {
            map.put(b, b);
        }
        assertEquals(100, map.size());
        for (int b = 0; b < 100; b++) {
            assertEquals(b, map.get(b));
        }
    }

    @Test
    public void getMissingKey() {
        ObjectIntMap<Integer> map = new ObjectIntMap<>();
        for (int b = 0; b < 100; b++) {
            map.put(b, b);
        }
        assertThrows(NoSuchElementException.class, () -> map.get(100));
        assertThrows(NoSuchElementException.class, () -> map.get(-1));
    }

    @Test
    public void putReplaceExisting() {
        ObjectIntMap<Integer> map = new ObjectIntMap<>();
        for (int b = 0; b < 100; b++) {
            map.put(b, b);
        }
        map.put(0, 1000);
        assertEquals(1000, map.get(0));
    }


    @Test
    public void size() {
        ObjectIntMap<Integer> map = new ObjectIntMap<>();
        for (int b = 0; b < 100; b++) {
            assertEquals(b, map.size());
            map.put(b, b);
        }
    }

    @Test
    public void isEmpty() {
        ObjectIntMap<Integer> map = new ObjectIntMap<>();
        assertTrue(map.isEmpty());
        map.put(0, 1000);
        assertFalse(map.isEmpty());
    }

    @Test
    public void toStringEmpty() {
        ObjectIntMap<Integer> map = new ObjectIntMap<>();
        assertEquals("{}", map.toString());
    }

    @Test
    public void toStringSingleton() {
        ObjectIntMap<Integer> map = new ObjectIntMap<>();
        assertTrue(map.isEmpty());
        map.put(0, 1000);
        assertEquals("{0=1000}", map.toString());
    }

    @Test
    public void toStringPair() {
        ObjectIntMap<Integer> map = new ObjectIntMap<>();
        assertTrue(map.isEmpty());
        map.put(0, 1000);
        map.put(1, 500);
        assertTrue(map.toString().equals("{0=1000, 1=500}") || map.toString().equals("{1=500, 0=1000}"));
    }

    @ParameterizedTest
    @MethodSource("mapPairs")
    public void equalsConsistency(ObjectIntMap<?> o1, ObjectIntMap<?> o2) {
        assertEquals(o1.equals(o2), o1.equals(o2));
    }

    @ParameterizedTest
    @MethodSource("mapTriples")
    public void equalsTransitivity(ObjectIntMap<?> o1, ObjectIntMap<?> o2, ObjectIntMap<?> o3) {
        if (o1.equals(o2) && o2.equals(o3)) {
            assertEquals(o1, o3);
        }
    }

    @ParameterizedTest
    @MethodSource("mapPairs")
    public void equalsSymmetry(ObjectIntMap<?> o1, ObjectIntMap<?> o2) {
        assertEquals(o1.equals(o2), o2.equals(o1));
    }

    @ParameterizedTest
    @MethodSource("maps")
    public void equalsReflexivity(ObjectIntMap<?> o1) {
        assertEquals(o1, o1);
    }

    @ParameterizedTest
    @MethodSource("maps")
    public void equalsNonNull(ObjectIntMap<?> o1) {
        assertNotEquals(null, o1);
    }

    @ParameterizedTest
    @MethodSource("maps")
    public void hashCodeInternalConsistency(ObjectIntMap<?> o1) {
        assertEquals(o1.hashCode(), o1.hashCode());
    }

    @ParameterizedTest
    @MethodSource("mapPairs")
    public void hashCodeEqualsConsistency(ObjectIntMap<?> o1, ObjectIntMap<?> o2) {
        if (o1.equals(o2)) {
            assertEquals(o1.hashCode(), o2.hashCode());
        }
    }

    @ParameterizedTest
    @MethodSource("mapStringPairs")
    public void putAllConsistentWithPut(ObjectIntMap<String> map1, ObjectIntMap<String> map2) {
        ObjectIntMap<String> copy = new ObjectIntMap<>(map1);
        edu.neu.ccs.prl.zeugma.internal.runtime.struct.Iterator<ObjectIntMap.Entry<String>> itr = map2.entryIterator();
        while (itr.hasNext()) {
            ObjectIntMap.Entry<String> entry = itr.next();
            copy.put(entry.getKey(), entry.getValue());
        }
        map1.putAll(map2);
        assertEquals(copy, map1);
    }

    @Test
    void removeMissingKey() {
        ObjectIntMap<String> map = new ObjectIntMap<>();
        assertFalse(map.remove("hello"));
    }

    private static List<Integer> values2() {
        List<Integer> keys = IntStream.range(0, 10).boxed().collect(Collectors.toCollection(LinkedList::new));
        IntStream.range(3, 100).boxed().forEach(keys::add);
        return keys;
    }

    private static List<Integer> values1() {
        List<Integer> keys = IntStream.range(0, 100).boxed().collect(Collectors.toCollection(LinkedList::new));
        keys.add(-1);
        return keys;
    }

    private static List<String> keys1() {
        List<String> keys =
                IntStream.range(0, 20).mapToObj(Integer::toString).collect(Collectors.toCollection(LinkedList::new));
        keys.add(null);
        return keys;
    }

    private static List<String> keys2() {
        List<String> keys =
                IntStream.range(0, 100).mapToObj(Integer::toString).collect(Collectors.toCollection(LinkedList::new));
        keys.add(null);
        return keys;
    }

    static Stream<Arguments> mapStringPairs() {
        List<Arguments> arguments = new LinkedList<>();
        List<ObjectIntMap<String>> maps = Arrays.asList(makeMap(keys1(), values1()),
                makeMap(keys1(), values2()),
                makeMap(keys2(), values1()),
                makeMap(keys2(), values2()));
        for (ObjectIntMap<String> map1 : maps) {
            for (ObjectIntMap<String> map2 : maps) {
                arguments.add(Arguments.of(map1, map2));
            }
        }
        return arguments.stream();
    }

    static Stream<Arguments> mapPairs() {
        List<Arguments> arguments = new LinkedList<>();
        List<ObjectIntMap<?>> maps = Arrays.asList(makeMap(keys1(), values1()),
                makeMap(keys1(), values2()),
                makeMap(keys2(), values1()),
                makeMap(keys2(), values2()),
                makeMap(values1(), values1()),
                makeMap(values2(), values2()),
                makeMap(values2(), values1()),
                makeMap(values2(), values2()));
        for (ObjectIntMap<?> map1 : maps) {
            for (ObjectIntMap<?> map2 : maps) {
                arguments.add(Arguments.of(map1, map2));
            }
        }
        return arguments.stream();
    }

    static Stream<Arguments> mapTriples() {
        List<Arguments> arguments = new LinkedList<>();
        List<ObjectIntMap<?>> maps = Arrays.asList(makeMap(keys1(), values1()),
                makeMap(keys1(), values2()),
                makeMap(keys2(), values1()),
                makeMap(keys2(), values2()),
                makeMap(values1(), values1()),
                makeMap(values2(), values2()),
                makeMap(values2(), values1()),
                makeMap(values2(), values2()));
        for (ObjectIntMap<?> map1 : maps) {
            for (ObjectIntMap<?> map2 : maps) {
                for (ObjectIntMap<?> map3 : maps) {
                    arguments.add(Arguments.of(map1, map2, map3));
                }
            }
        }
        return arguments.stream();
    }

    static Stream<Arguments> maps() {
        return Stream.of(Arguments.of(makeMap(keys1(), values1())),
                Arguments.of(makeMap(keys1(), values2())),
                Arguments.of(makeMap(keys2(), values1())),
                Arguments.of(makeMap(keys2(), values2())));
    }

    static Stream<Arguments> mapKeyPairs() {
        List<Arguments> arguments = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            arguments.add(Arguments.of(makeMap(keys1(), values1()), keys1().get(i)));
            arguments.add(Arguments.of(makeMap(keys1(), values2()), keys1().get(i)));
            arguments.add(Arguments.of(makeMap(keys2(), values1()), keys1().get(i)));
            arguments.add(Arguments.of(makeMap(keys2(), values2()), keys1().get(i)));
        }
        arguments.add(Arguments.of(makeMap(keys1(), values1()), "hello"));
        arguments.add(Arguments.of(makeMap(keys1(), values2()), "hello"));
        arguments.add(Arguments.of(makeMap(keys2(), values1()), "hello"));
        arguments.add(Arguments.of(makeMap(keys2(), values2()), "hello"));
        return arguments.stream();
    }

    private static <K> ObjectIntMap<K> makeMap(Iterable<K> keys, Iterable<Integer> values) {
        ObjectIntMap<K> map = new ObjectIntMap<>();
        java.util.Iterator<K> itr1 = keys.iterator();
        java.util.Iterator<Integer> itr2 = values.iterator();
        while (itr1.hasNext() && itr2.hasNext()) {
            map.put(itr1.next(), itr2.next());
        }
        return map;
    }
}