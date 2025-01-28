package edu.neu.ccs.prl.zeugma.internal.guidance;

import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

public class Individual {
    private final ByteList input;
    private final String generatedInput;

    public Individual(ByteList input, String generatedInput) {
        if (input == null) {
            throw new NullPointerException();
        }
        this.input = input;
        this.generatedInput = generatedInput;
    }

    public void initialize(FuzzTarget target) {
    }

    public ByteList getInput() {
        return input;
    }

    public String getGeneratedInput() {
        return generatedInput;
    }

    @Override
    public int hashCode() {
        return input.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Individual)) {
            return false;
        }
        Individual that = (Individual) o;
        return input.equals(that.input);
    }
}