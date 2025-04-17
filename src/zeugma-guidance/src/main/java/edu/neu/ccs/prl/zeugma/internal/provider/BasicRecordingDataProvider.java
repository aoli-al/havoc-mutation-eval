package edu.neu.ccs.prl.zeugma.internal.provider;

import edu.neu.ccs.prl.zeugma.internal.util.Math;
import edu.neu.ccs.prl.zeugma.internal.util.*;

import java.util.Random;

public final class BasicRecordingDataProvider implements RecordingDataProvider {
    private final PrimitiveReader reader;
    private final int size;
    private final ByteList recording = SynchronizedByteList.of(new ByteArrayList());

    public BasicRecordingDataProvider(ByteProducer producer, int size) {
        this.reader = new PrimitiveReader(producer);
        this.size = size;
    }

    @Override
    public ByteList getRecording() {
        return UnmodifiableByteList.of(recording);
    }

    @Override
    public int remainingBytes() {
        return Math.max(0, size - getNumberConsumed());
    }

    @Override
    public boolean close() {
        return false;
    }

    @Override
    public int getNumberConsumed() {
        return recording.size();
    }

    @Override
    public float nextFloat() {
        return PrimitiveWriter.write(recording, reader.readFloat());
    }

    @Override
    public float nextFloat(float min, float max) {
        return PrimitiveWriter.write(recording, reader.readFloat(min, max));
    }

    @Override
    public float nextFiniteFloat() {
        return PrimitiveWriter.write(recording, reader.readFiniteFloat());
    }

    @Override
    public float nextProbabilityFloat() {
        return PrimitiveWriter.write(recording, reader.readProbabilityFloat());
    }

    @Override
    public double nextDouble() {
        return PrimitiveWriter.write(recording, reader.readDouble());
    }

    @Override
    public double nextDouble(double min, double max) {
        return PrimitiveWriter.write(recording, reader.readDouble(min, max));
    }

    @Override
    public double nextFiniteDouble() {
        return PrimitiveWriter.write(recording, reader.readFiniteDouble());
    }

    @Override
    public double nextProbabilityDouble() {
        return PrimitiveWriter.write(recording, reader.readProbabilityDouble());
    }

    @Override
    public long nextLong() {
        return PrimitiveWriter.write(recording, reader.readLong());
    }

    @Override
    public long nextLong(long min, long max) {
        return PrimitiveWriter.write(recording, reader.readLong(min, max));
    }

    @Override
    public int nextInt() {
        return PrimitiveWriter.write(recording, reader.readInt());
    }

    @Override
    public int nextInt(int min, int max) {
        return PrimitiveWriter.write(recording, reader.readInt(min, max));
    }

    @Override
    public int nextInt(int n) {
        return PrimitiveWriter.write(recording, reader.readInt(n));
    }

    @Override
    public boolean nextBoolean() {
        return PrimitiveWriter.write(recording, reader.readBoolean());
    }

    @Override
    public char nextChar() {
        return PrimitiveWriter.write(recording, reader.readChar());
    }

    @Override
    public char nextChar(char min, char max) {
        return PrimitiveWriter.write(recording, reader.readChar(min, max));
    }

    @Override
    public short nextShort() {
        return PrimitiveWriter.write(recording, reader.readShort());
    }

    @Override
    public short nextShort(short min, short max) {
        return PrimitiveWriter.write(recording, reader.readShort(min, max));
    }

    @Override
    public byte nextByte() {
        return PrimitiveWriter.write(recording, reader.readByte());
    }

    @Override
    public byte nextByte(byte min, byte max) {
        return PrimitiveWriter.write(recording, reader.readByte(min, max));
    }

    @Override
    public byte[] consumeRemaining() {
        ByteList result = new ByteArrayList();
        while (remainingBytes() > 0) {
            result.add(nextByte());
        }
        return result.toArray();
    }

    public static DataProviderFactory createFactory(Random random, int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException();
        }
        return values -> {
            if (values.size() > maxSize) {
                values = values.trim(maxSize);
            }
            return new BasicRecordingDataProvider(new ExpandingByteProducer(random, maxSize, values), values.size());
        };
    }
}
