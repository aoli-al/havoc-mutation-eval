package edu.neu.ccs.prl.zeugma.internal.util;

import java.util.function.BiFunction;

public final class Math {
    private Math() {
        throw new AssertionError();
    }

    @SuppressWarnings("ManualMinMaxCalculation")
    public static int max(int i1, int i2) {
        return i1 > i2 ? i1 : i2;
    }

    @SuppressWarnings("ManualMinMaxCalculation")
    public static long max(long l1, long l2) {
        return l1 > l2 ? l1 : l2;
    }

    @SuppressWarnings("ManualMinMaxCalculation")
    public static int min(int i1, int i2) {
        return i1 < i2 ? i1 : i2;
    }

    @SuppressWarnings("ManualMinMaxCalculation")
    public static long min(long l1, long l2) {
        return l1 < l2 ? l1 : l2;
    }

    public static long doubleToLongBits(double d) {
        long result = Double.doubleToRawLongBits(d);
        if (((result & 0x7FF0000000000000L) == 0x7FF0000000000000L) && (result & 0x000FFFFFFFFFFFFFL) != 0L) {
            result = 0x7ff8000000000000L;
        }
        return result;
    }

    public static int floatToIntBits(float value) {
        int result = Float.floatToRawIntBits(value);
        if (((result & 0x7F800000) == 0x7F800000) && (result & 0x007FFFFF) != 0) {
            result = 0x7fc00000;
        }
        return result;
    }

    public static int getLevenshteinDistFromString(String s1, String s2) {
        return getLevenshteinDistFrom(s1.length(), s2.length(), (i, j) -> s1.charAt(i) == s2.charAt(j));
    }

    public static int getLevenshteinDistFromByteList(ByteList s1, ByteList s2) {
        return getLevenshteinDistFrom(s1.size(), s2.size(), (i, j) -> s1.get(i) == s2.get(j));
    }

    private static int getLevenshteinDistFrom(int s1Length, int s2Length,
                                              BiFunction<Integer, Integer, Boolean> comparator) {
        int n = s2Length;
        int[] v0 = new int[n + 1];
        int[] v1 = new int[n + 1];
        for (int i = 0; i < s2Length + 1; i++) {
            v0[i] = i;
        }
        for (int i = 0; i < s1Length; i++) {
            v1[0] = i + 1;
            for (int j = 0; j < s2Length; j++) {
                int deletionCost = v0[j + 1] + 1;
                int insertionCost = v1[j] + 1;
                int substitutionCost = 0;
                if (comparator.apply(i, j)) {
                    substitutionCost = v0[j];
                } else {
                    substitutionCost = v0[j] + 1;
                }
                int min = min(deletionCost, insertionCost);
                v1[j + 1] = min(min, substitutionCost);
            }
            // swap
            int[] tmp = v0;
            v0 = v1;
            v1 = tmp;
        }
        return v0[n];
    }
}
