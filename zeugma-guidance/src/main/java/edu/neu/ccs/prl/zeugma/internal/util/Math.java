package edu.neu.ccs.prl.zeugma.internal.util;

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
}
