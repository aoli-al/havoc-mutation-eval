package edu.neu.ccs.prl.zeugma.internal.provider;

import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.Math;

public final class PrimitiveWriter {
    private PrimitiveWriter() {
        throw new AssertionError();
    }

    public static byte write(ByteList list, byte result) {
        list.add(result);
        return result;
    }

    public static char write(ByteList list, char result) {
        list.add((byte) (result >> 8));
        list.add((byte) result);
        return result;
    }

    public static int write(ByteList list, int result) {
        list.add((byte) (result >> 24));
        list.add((byte) (result >> 16));
        list.add((byte) (result >> 8));
        list.add((byte) result);
        return result;
    }

    public static long write(ByteList list, long result) {
        list.add((byte) (result >> 56));
        list.add((byte) (result >> 48));
        list.add((byte) (result >> 40));
        list.add((byte) (result >> 32));
        list.add((byte) (result >> 24));
        list.add((byte) (result >> 16));
        list.add((byte) (result >> 8));
        list.add((byte) result);
        return result;
    }

    public static boolean write(ByteList list, boolean result) {
        byte b = result ? (byte) 1 : (byte) 0;
        list.add(b);
        return result;
    }

    public static short write(ByteList list, short result) {
        list.add((byte) (result >> 8));
        list.add((byte) result);
        return result;
    }

    public static double write(ByteList list, double result) {
        write(list, Math.doubleToLongBits(result));
        return result;
    }

    public static float write(ByteList list, float result) {
        write(list, Math.floatToIntBits(result));
        return result;
    }
}
