package edu.neu.ccs.prl.zeugma.internal.provider;

import edu.neu.ccs.prl.zeugma.internal.util.Math;

public final class PrimitiveReader {
    private final ByteProducer producer;

    public PrimitiveReader(ByteProducer producer) {
        if (producer == null) {
            throw new NullPointerException();
        }
        this.producer = producer;
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public float readFloat(float min, float max) {
        if (max <= min) {
            throw new IllegalArgumentException();
        }
        float result = readFloat();
        if (result >= min && result < max) {
            return result;
        }
        double range = ((double) max) - min;
        result = (Math.floatToIntBits(result) >>> 8) * 0x1.0p-24f;
        result = (float) (result * range + min);
        if (result < min || result >= max) {
            // Handle rounding errors
            return min;
        }
        return result;
    }

    public float readFiniteFloat() {
        float result = readFloat();
        return isFinite(result) ? result : 0;
    }

    public float readProbabilityFloat() {
        float result = readFloat();
        if (!(result >= 0.0f) || !(result < 1.0f)) {
            result = (Math.floatToIntBits(result) >>> 8) * 0x1.0p-24f;  // 1.0f / (1 << 24)
        }
        return result;
    }

    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    public double readFiniteDouble() {
        double result = Double.longBitsToDouble(readLong());
        return isFinite(result) ? result : 0;
    }

    public double readProbabilityDouble() {
        double result = Double.longBitsToDouble(readLong());
        if (result >= 0.0f && result < 1.0f) {
            return result;
        }
        return (Math.doubleToLongBits(result) >>> 11) * 0x1.0p-53;  // 1.0  / (1L << 53)
    }

    public double readDouble(double min, double max) {
        if (max <= min) {
            throw new IllegalArgumentException();
        }
        double result = Double.longBitsToDouble(readLong());
        if (result >= min && result < max) {
            return result;
        }
        double range = max - min;
        result = (Math.doubleToLongBits(result) >>> 11) * 0x1.0p-53;
        result = result * range + min;
        if (result < min || result >= max) {
            // Handle rounding errors
            return min;
        }
        return result;
    }

    public long readLong() {
        return ((((long) producer.next()) << 56) | (((long) producer.next() & 0xff) << 48) |
                (((long) producer.next() & 0xff) << 40) | (((long) producer.next() & 0xff) << 32) |
                (((long) producer.next() & 0xff) << 24) | (((long) producer.next() & 0xff) << 16) |
                (((long) producer.next() & 0xff) << 8) | (((long) producer.next() & 0xff)));
    }

    public long readLong(long min, long max) {
        if (max < min) {
            throw new IllegalArgumentException();
        }
        long result = readLong();
        if (result <= max && result >= min) {
            return result;
        } else if (min == max) {
            // If the range only contains one value, return that value
            return min;
        } else {
            long range = max - min;
            if (((max ^ min) & (max ^ range)) < 0 || range == Long.MAX_VALUE) {
                // The range is larger than Long.MAX_VALUE
                if (result < min) {
                    return ((min - result) - 1) + min;
                } else {
                    return max - ((result - max) - 1);
                }
            } else {
                range = range + 1;
                return result >= 0 ? (result % range) + min : min - (result % range);
            }
        }
    }

    public int readInt() {
        return (((((int) producer.next()) & 0xff) << 24) | ((((int) producer.next()) & 0xff) << 16) |
                ((((int) producer.next()) & 0xff) << 8) | (((int) producer.next()) & 0xff));
    }

    public int readInt(int min, int max) {
        if (max < min) {
            throw new IllegalArgumentException();
        }
        int result = readInt();
        if (result <= max && result >= min) {
            return result;
        } else if (min == max) {
            // If the range only contains one value, return that value
            return min;
        } else {
            long range = ((long) max) - min + 1L;
            long value = ((long) result) & 0xffffffffL;
            return (int) ((value % range) + min);
        }
    }

    public int readInt(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException();
        }
        return (readInt() & 0x7fffffff) % n;
    }

    public boolean readBoolean() {
        return producer.next() > 0;
    }

    public char readChar() {
        return (char) (((((int) producer.next()) & 0xff) << 8) | ((int) producer.next()) & 0xff);
    }

    public char readChar(char min, char max) {
        if (max < min) {
            throw new IllegalArgumentException();
        }
        char result = readChar();
        if (result >= min && result <= max) {
            return result;
        } else if (min == max) {
            // If the range only contains one value, return that value
            return min;
        } else {
            int range = ((int) max) - min + 1;
            return (char) ((result % range) + min);
        }
    }

    public short readShort() {
        return (short) (((((int) producer.next()) & 0xff) << 8) | ((int) producer.next()) & 0xff);
    }

    public short readShort(short min, short max) {
        if (max < min) {
            throw new IllegalArgumentException();
        }
        short result = readShort();
        if (result >= min && result <= max) {
            return result;
        } else if (min == max) {
            // If the range only contains one value, return that value
            return min;
        } else {
            int range = ((int) max) - min + 1;
            int value = ((int) result) & 0xffff;
            return (short) ((value % range) + min);
        }
    }

    public byte readByte() {
        return producer.next();
    }

    public byte readByte(byte min, byte max) {
        if (max < min) {
            throw new IllegalArgumentException();
        }
        byte result = readByte();
        if (result >= min && result <= max) {
            return result;
        } else if (min == max) {
            // If the range only contains one value, return that value
            return min;
        } else {
            int range = ((int) max) - min + 1;
            int value = ((int) result) & 0xff;
            return (byte) ((value % range) + min);
        }
    }

    private static boolean isFinite(float value) {
        return ((value <= 0.0F) ? 0.0F - value : value) <= 0x1.fffffeP+127f; // Float.MAX_VALUE
    }

    private static boolean isFinite(double value) {
        return ((value <= 0.0D) ? 0.0D - value : value) <= 0x1.fffffffffffffP+1023; // Double.MAX_VALUE
    }
}
