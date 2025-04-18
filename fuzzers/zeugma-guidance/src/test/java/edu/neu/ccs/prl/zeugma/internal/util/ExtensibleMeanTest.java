package edu.neu.ccs.prl.zeugma.internal.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedList;
import java.util.List;
import java.util.function.LongConsumer;
import java.util.stream.Stream;

class ExtensibleMeanTest {
    @ParameterizedTest
    @MethodSource("arguments")
    void sizeEqualsCallsToUpdate(long[] values) {
        ExtensibleMean mean = new ExtensibleMean();
        setup(mean::extend, values);
        Assertions.assertEquals(values.length, mean.size());
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void extendMatchesOffline(long[] values) {
        Assumptions.assumeFalse(values.length == 0);
        ExtensibleMean mean = new ExtensibleMean();
        setup(mean::extend, values);
        double expected = mean(values);
        Assertions.assertEquals(expected, mean.get(), java.lang.Math.abs(0.001 * expected));
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void extendWithMeanMatchesOffline(long[] values) {
        Assumptions.assumeFalse(values.length == 0);
        double expected = mean(values);
        ExtensibleMean mean1 = new ExtensibleMean();
        ExtensibleMean mean2 = new ExtensibleMean();
        for (int i = 0; i < values.length; i++) {
            if (i % 2 == 0) {
                mean2.extend(values[i]);
            } else {
                mean1.extend(values[i]);
            }
        }
        mean1.extend(mean2);
        Assertions.assertEquals(expected, mean1.get(), java.lang.Math.abs(0.001 * expected));
    }

    static Stream<Arguments> arguments() {
        List<Arguments> arguments = new LinkedList<>();
        long[] values = new long[]{Integer.MIN_VALUE, -200, -2, -1, 0, 1, 3, 100, 99999, Integer.MAX_VALUE};
        arguments.add(Arguments.of(values));
        arguments.add(Arguments.of(new long[]{-200, -2, -1, 0, 1, 3, 100, 99999}));
        arguments.add(Arguments.of(new long[]{-200, -2, -1, 0, 1, 3, 100}));
        arguments.add(Arguments.of(new long[]{-1, 0, 1, 3, 100, 8}));
        arguments.add(Arguments.of(new long[]{100, 8}));
        arguments.add(Arguments.of(new long[]{100, 8, 3}));
        arguments.add(Arguments.of(new long[]{Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE}));
        arguments.add(Arguments.of(new long[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE}));
        for (long value : values) {
            arguments.add(Arguments.of(new long[]{value}));
        }
        return arguments.stream();
    }

    static double mean(long[] values) {
        double result = 0;
        for (long value : values) {
            result += value / (1.0 * values.length);
        }
        return result;
    }

    static void setup(LongConsumer consumer, long[] values) {
        for (long value : values) {
            consumer.accept(value);
        }
    }
}