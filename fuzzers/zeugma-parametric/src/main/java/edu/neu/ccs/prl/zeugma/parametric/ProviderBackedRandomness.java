package edu.neu.ccs.prl.zeugma.parametric;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.neu.ccs.prl.zeugma.internal.parametric.InputSizeException;
import edu.neu.ccs.prl.zeugma.internal.guidance.DataProvider;

import java.util.Random;

public final class ProviderBackedRandomness extends SourceOfRandomness {
    private final ProviderBackedRandom random;
    private final DataProvider provider;

    public ProviderBackedRandomness(DataProvider provider, int maxInputSize) {
        this(provider, new ProviderBackedRandom(provider, maxInputSize));
    }

    private ProviderBackedRandomness(DataProvider provider, ProviderBackedRandom random) {
        super(random);
        this.provider = provider;
        this.random = random;
        random.initialized = true;
    }

    @Override
    public Random toJDKRandom() {
        return random;
    }

    @Override
    public byte nextByte(byte min, byte max) {
        random.checkSize(1);
        return provider.nextByte(min, max);
    }

    @Override
    public char nextChar(char min, char max) {
        random.checkSize(2);
        return provider.nextChar(min, max);
    }

    @Override
    public double nextDouble(double min, double max) {
        random.checkSize(8);
        return provider.nextDouble(min, max);
    }

    @Override
    public float nextFloat(float min, float max) {
        random.checkSize(4);
        return provider.nextFloat(min, max);
    }

    @Override
    public int nextInt(int min, int max) {
        random.checkSize(4);
        return provider.nextInt(min, max);
    }

    @Override
    public long nextLong(long min, long max) {
        random.checkSize(8);
        return provider.nextLong(min, max);
    }

    @Override
    public short nextShort(short min, short max) {
        random.checkSize(2);
        return provider.nextShort(min, max);
    }

    private static class ProviderBackedRandom extends Random {
        private static final long serialVersionUID = 969013351457464770L;
        private final DataProvider provider;
        private final int maxInputSize;
        private boolean initialized = false;

        private ProviderBackedRandom(DataProvider provider, int maxInputSize) {
            if (provider == null) {
                throw new NullPointerException();
            }
            if (maxInputSize < 0) {
                throw new IllegalArgumentException();
            }
            this.provider = provider;
            this.maxInputSize = maxInputSize;
        }

        @Override
        protected int next(int bits) {
            checkSize(4);
            return provider.nextInt();
        }

        @Override
        public void nextBytes(byte[] bytes) {
            checkSize(bytes.length);
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = provider.nextByte();
            }
        }

        @Override
        public int nextInt() {
            checkSize(4);
            return provider.nextInt();
        }

        @Override
        public int nextInt(int bound) {
            checkSize(4);
            return provider.nextInt(0, bound - 1);
        }

        @Override
        public long nextLong() {
            if (!initialized) {
                return 0;
            }
            checkSize(8);
            return provider.nextLong();
        }

        @Override
        public boolean nextBoolean() {
            checkSize(1);
            return provider.nextBoolean();
        }

        @Override
        public float nextFloat() {
            checkSize(4);
            return provider.nextProbabilityFloat();
        }

        @Override
        public double nextDouble() {
            checkSize(8);
            return provider.nextProbabilityDouble();
        }

        public void checkSize(int size) {
            if (provider.getNumberConsumed() + size > maxInputSize) {
                throw new InputSizeException("Exceeded maximum input size");
            }
        }
    }
}

