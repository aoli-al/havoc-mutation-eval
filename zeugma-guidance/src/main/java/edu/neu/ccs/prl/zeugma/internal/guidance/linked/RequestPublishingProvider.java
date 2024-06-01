package edu.neu.ccs.prl.zeugma.internal.guidance.linked;

import edu.neu.ccs.prl.zeugma.internal.provider.DataProviderFactory;
import edu.neu.ccs.prl.zeugma.internal.provider.RecordingDataProvider;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

class RequestPublishingProvider implements RecordingDataProvider {
    private final RecordingDataProvider delegate;
    private final CallTreeBuilder subscriber;

    private RequestPublishingProvider(RecordingDataProvider delegate, CallTreeBuilder subscriber) {
        if (delegate == null || subscriber == null) {
            throw new NullPointerException();
        }
        this.subscriber = subscriber;
        this.delegate = delegate;
    }

    @Override
    public float nextFloat() {
        int start = delegate.getNumberConsumed();
        float result = delegate.nextFloat();
        int end = delegate.getNumberConsumed();
        subscriber.parameterRequest(start, end);
        return result;
    }

    @Override
    public float nextFloat(float min, float max) {
        int start = delegate.getNumberConsumed();
        float result = delegate.nextFloat(min, max);
        int end = delegate.getNumberConsumed();
        subscriber.parameterRequest(start, end);
        return result;
    }

    @Override
    public float nextFiniteFloat() {
        int start = delegate.getNumberConsumed();
        float result = delegate.nextFiniteFloat();
        int end = delegate.getNumberConsumed();
        subscriber.parameterRequest(start, end);
        return result;
    }

    @Override
    public float nextProbabilityFloat() {
        int start = delegate.getNumberConsumed();
        float result = delegate.nextProbabilityFloat();
        int end = delegate.getNumberConsumed();
        subscriber.parameterRequest(start, end);
        return result;
    }

    @Override
    public double nextDouble() {
        int start = delegate.getNumberConsumed();
        double result = delegate.nextDouble();
        int end = delegate.getNumberConsumed();
        subscriber.parameterRequest(start, end);
        return result;
    }

    @Override
    public double nextDouble(double min, double max) {
        int start = delegate.getNumberConsumed();
        double result = delegate.nextDouble(min, max);
        int end = delegate.getNumberConsumed();
        subscriber.parameterRequest(start, end);
        return result;
    }

    @Override
    public double nextFiniteDouble() {
        int start = delegate.getNumberConsumed();
        double result = delegate.nextFiniteDouble();
        int end = delegate.getNumberConsumed();
        subscriber.parameterRequest(start, end);
        return result;
    }

    @Override
    public double nextProbabilityDouble() {
        int start = delegate.getNumberConsumed();
        double result = delegate.nextProbabilityDouble();
        int end = delegate.getNumberConsumed();
        subscriber.parameterRequest(start, end);
        return result;
    }

    @Override
    public long nextLong() {
        int start = delegate.getNumberConsumed();
        long result = delegate.nextLong();
        int end = delegate.getNumberConsumed();
        subscriber.parameterRequest(start, end);
        return result;
    }

    @Override
    public long nextLong(long min, long max) {
        int start = delegate.getNumberConsumed();
        long result = delegate.nextLong(min, max);
        int end = delegate.getNumberConsumed();
        subscriber.parameterRequest(start, end);
        return result;
    }

    @Override
    public int nextInt() {
        int start = delegate.getNumberConsumed();
        int result = delegate.nextInt();
        int end = delegate.getNumberConsumed();
        subscriber.parameterRequest(start, end);
        return result;
    }

    @Override
    public int nextInt(int min, int max) {
        int start = delegate.getNumberConsumed();
        int result = delegate.nextInt(min, max);
        int end = delegate.getNumberConsumed();
        subscriber.parameterRequest(start, end);
        return result;
    }

    @Override
    public int nextInt(int n) {
        int start = delegate.getNumberConsumed();
        int result = delegate.nextInt(n);
        int end = delegate.getNumberConsumed();
        subscriber.parameterRequest(start, end);
        return result;
    }

    @Override
    public boolean nextBoolean() {
        int start = delegate.getNumberConsumed();
        boolean result = delegate.nextBoolean();
        int end = delegate.getNumberConsumed();
        subscriber.parameterRequest(start, end);
        return result;
    }

    @Override
    public char nextChar() {
        int start = delegate.getNumberConsumed();
        char result = delegate.nextChar();
        int end = delegate.getNumberConsumed();
        subscriber.parameterRequest(start, end);
        return result;
    }

    @Override
    public char nextChar(char min, char max) {
        int start = delegate.getNumberConsumed();
        char result = delegate.nextChar(min, max);
        int end = delegate.getNumberConsumed();
        subscriber.parameterRequest(start, end);
        return result;
    }

    @Override
    public short nextShort() {
        int start = delegate.getNumberConsumed();
        short result = delegate.nextShort();
        int end = delegate.getNumberConsumed();
        subscriber.parameterRequest(start, end);
        return result;
    }

    @Override
    public short nextShort(short min, short max) {
        int start = delegate.getNumberConsumed();
        short result = delegate.nextShort(min, max);
        int end = delegate.getNumberConsumed();
        subscriber.parameterRequest(start, end);
        return result;
    }

    @Override
    public byte nextByte() {
        int start = delegate.getNumberConsumed();
        byte result = delegate.nextByte();
        int end = delegate.getNumberConsumed();
        subscriber.parameterRequest(start, end);
        return result;
    }

    @Override
    public byte nextByte(byte min, byte max) {
        int start = delegate.getNumberConsumed();
        byte result = delegate.nextByte(min, max);
        int end = delegate.getNumberConsumed();
        subscriber.parameterRequest(start, end);
        return result;
    }

    @Override
    public byte[] consumeRemaining() {
        return delegate.consumeRemaining();
    }

    @Override
    public int remainingBytes() {
        return delegate.remainingBytes();
    }

    @Override
    public int getNumberConsumed() {
        return delegate.getNumberConsumed();
    }

    @Override
    public ByteList getRecording() {
        return delegate.getRecording();
    }

    @Override
    public boolean close() {
        delegate.close();
        return subscriber.close();
    }

    static DataProviderFactory attach(DataProviderFactory factory, CallTreeBuilder builder) {
        if (builder == null || factory == null) {
            throw new NullPointerException();
        }
        return values -> new RequestPublishingProvider(factory.create(values), builder);
    }
}
