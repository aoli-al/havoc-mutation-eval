package edu.neu.ccs.prl.zeugma.internal.eval.heritability;

import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

public final class TimestampedInput {
    private final ByteList input;
    private final long time;

    public TimestampedInput(ByteList input, long time) {
        if (input == null) {
            throw new NullPointerException();
        }
        this.input = input;
        this.time = time;
    }

    public ByteList getInput() {
        return input;
    }

    public long getTime() {
        return time;
    }
}
