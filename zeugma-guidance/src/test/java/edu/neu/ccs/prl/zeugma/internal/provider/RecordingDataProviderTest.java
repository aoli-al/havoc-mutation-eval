package edu.neu.ccs.prl.zeugma.internal.provider;

import edu.neu.ccs.prl.zeugma.internal.guidance.DataProvider;
import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

class RecordingDataProviderTest {
    private static final DataProviderFactory factory =
            BasicRecordingDataProvider.createFactory(ThreadLocalRandom.current(), Integer.MAX_VALUE);

    private static int byteToIndex(byte b) {
        return (b - ((int) Byte.MIN_VALUE));
    }

    static Stream<Arguments> byteArguments() {
        List<Arguments> arguments = new LinkedList<>();
        byte[] values = new byte[]{Byte.MIN_VALUE, Byte.MIN_VALUE + 1, -1, 0, 1, Byte.MAX_VALUE - 1, Byte.MAX_VALUE};
        for (byte min : values) {
            for (byte max : values) {
                if (min <= max) {
                    arguments.add(Arguments.of(min, max));
                }
            }
        }
        return arguments.stream();
    }

    static Stream<Arguments> longArguments() {
        List<Arguments> arguments = new LinkedList<>();
        long[] values =
                new long[]{Long.MIN_VALUE, Long.MIN_VALUE + 1, -100, -1, 0, 1, 100, Long.MAX_VALUE - 1, Long.MAX_VALUE};
        for (long min : values) {
            for (long max : values) {
                if (min <= max) {
                    for (long data : values) {
                        arguments.add(Arguments.of(min, max, data));
                    }
                }
            }
        }
        return arguments.stream();
    }

    private static DataProvider createAllBytesProvider() {
        ByteList allBytes = new ByteArrayList();
        for (int b = Byte.MIN_VALUE; b <= Byte.MAX_VALUE; b++) {
            allBytes.add((byte) b);
        }
        return factory.create(allBytes);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void recordReadRoundTrip(boolean x) {
        ByteList list = new ByteArrayList();
        PrimitiveWriter.write(list, x);
        RecordingDataProvider provider = factory.create(list);
        provider.nextBoolean();
        provider = factory.create(provider.getRecording());
        Assertions.assertEquals(x, provider.nextBoolean());
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, Integer.MIN_VALUE + 1, -100, -1, 0, 1, 100, Integer.MAX_VALUE - 1,
            Integer.MAX_VALUE})
    void recordReadRoundTrip(int x) {
        ByteList list = new ByteArrayList();
        PrimitiveWriter.write(list, x);
        RecordingDataProvider provider = factory.create(list);
        provider.nextInt();
        provider = factory.create(provider.getRecording());
        Assertions.assertEquals(x, provider.nextInt());
    }

    @ParameterizedTest
    @ValueSource(
            shorts = {Short.MIN_VALUE, Short.MIN_VALUE + 1, -100, -1, 0, 1, 100, Short.MAX_VALUE - 1, Short.MAX_VALUE})
    void recordReadRoundTrip(short x) {
        ByteList list = new ByteArrayList();
        PrimitiveWriter.write(list, x);
        RecordingDataProvider provider = factory.create(list);
        provider.nextShort();
        provider = factory.create(provider.getRecording());
        Assertions.assertEquals(x, provider.nextShort());
    }

    @ParameterizedTest
    @ValueSource(chars = {Character.MIN_VALUE, Character.MIN_VALUE + 1, 0, 1, 100, Character.MAX_VALUE - 1,
            Character.MAX_VALUE})
    void recordReadRoundTrip(char x) {
        ByteList list = new ByteArrayList();
        PrimitiveWriter.write(list, x);
        RecordingDataProvider provider = factory.create(list);
        provider.nextChar();
        provider = factory.create(provider.getRecording());
        Assertions.assertEquals(x, provider.nextChar());
    }

    @ParameterizedTest
    @ValueSource(longs = {Long.MIN_VALUE, Long.MIN_VALUE + 1, -100, -1, 0, 1, 100, Long.MAX_VALUE - 1, Long.MAX_VALUE})
    void recordReadRoundTrip(long x) {
        ByteList list = new ByteArrayList();
        PrimitiveWriter.write(list, x);
        RecordingDataProvider provider = factory.create(list);
        provider.nextLong();
        provider = factory.create(provider.getRecording());
        Assertions.assertEquals(x, provider.nextLong());
    }

    @ParameterizedTest
    @ValueSource(floats = {-100, -1, 0, 1, 100, Float.MAX_VALUE, Float.MIN_VALUE, Float.NaN, Float.NEGATIVE_INFINITY,
            Float.POSITIVE_INFINITY, 0.5f, -0.5f, 1777.777f, -1777.88f})
    void recordReadRoundTrip(float x) {
        ByteList list = new ByteArrayList();
        PrimitiveWriter.write(list, x);
        RecordingDataProvider provider = factory.create(list);
        provider.nextFloat();
        provider = factory.create(provider.getRecording());
        Assertions.assertEquals(x, provider.nextFloat());
    }

    @ParameterizedTest
    @ValueSource(
            doubles = {-100, -1, 0, 1, 100, Double.MAX_VALUE, Double.MIN_VALUE, Double.NaN, Double.NEGATIVE_INFINITY,
                    Double.POSITIVE_INFINITY, 0.5, -0.5, 1777.777, -1777.88})
    void recordReadRoundTrip(double x) {
        ByteList list = new ByteArrayList();
        PrimitiveWriter.write(list, x);
        RecordingDataProvider provider = factory.create(list);
        provider.nextDouble();
        provider = factory.create(provider.getRecording());
        Assertions.assertEquals(x, provider.nextDouble());
    }

    @ParameterizedTest
    @MethodSource(value = "byteArguments")
    void nextByteRange(byte min, byte max) {
        DataProvider provider = createAllBytesProvider();
        int[] counts = new int[Byte.MAX_VALUE - Byte.MIN_VALUE + 1];
        for (int providerNext = Byte.MIN_VALUE; providerNext <= Byte.MAX_VALUE; providerNext++) {
            byte next = provider.nextByte(min, max);
            if (providerNext >= min && providerNext <= max) {
                // Check that is the source byte is within the range, the generated byte matches the source byte
                Assertions.assertEquals((byte) providerNext, next);
            }
            // Check that the generated value is within the range [min, max]
            Assertions.assertTrue(next >= min && next <= max);
            counts[byteToIndex(next)]++;
        }
        int minCount = counts[byteToIndex(max)];
        int maxCount = minCount;
        for (byte i = min; i < max; i++) {
            int count = counts[byteToIndex(i)];
            minCount = Math.min(minCount, count);
            maxCount = Math.max(maxCount, count);
        }
        // Check that every value in the range is generated at least once
        Assertions.assertTrue(minCount > 0);
        // Check that the most frequent value was generated no more than 3 more times than the least frequent
        Assertions.assertTrue(maxCount - minCount <= 3);
    }

    @ParameterizedTest
    @MethodSource(value = "longArguments")
    void nextLongRange(long min, long max, long data) {
        byte[] b = new byte[Long.BYTES];
        ByteBuffer.wrap(b).putLong(data);
        ByteList source = new ByteArrayList(b);
        DataProvider provider = factory.create(source);
        long next = provider.nextLong(min, max);
        // Check that the generated value is within the range [min, max]
        Assertions.assertTrue(next >= min && next <= max);
    }
}