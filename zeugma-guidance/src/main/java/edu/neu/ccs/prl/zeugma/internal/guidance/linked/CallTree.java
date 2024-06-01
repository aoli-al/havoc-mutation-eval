package edu.neu.ccs.prl.zeugma.internal.guidance.linked;

/**
 * Dummy vertex used to represent the root of a call tree.
 */
public final class CallTree extends CallTreeVertex {
    CallTree() {
        super();
    }

    @Override
    public void accept(CallTreeVertexVisitor visitor) {
    }

    @Override
    public String toString() {
        return "ROOT";
    }
}