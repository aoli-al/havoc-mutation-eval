package edu.neu.ccs.prl.zeugma.internal.guidance.linked;

import edu.neu.ccs.prl.zeugma.internal.util.Interval;

public final class ParameterRequestVertex extends CallTreeVertex {
    public ParameterRequestVertex(int start, int end) {
        super(new Interval(start, end));
    }

    @Override
    public String toString() {
        return String.format("ParameterRequest: %s", getSourceInterval());
    }

    @Override
    public void accept(CallTreeVertexVisitor visitor) {
        visitor.visit(this);
    }
}
