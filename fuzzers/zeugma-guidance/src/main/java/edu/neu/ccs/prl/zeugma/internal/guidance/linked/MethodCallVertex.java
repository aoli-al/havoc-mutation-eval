package edu.neu.ccs.prl.zeugma.internal.guidance.linked;

import edu.neu.ccs.prl.zeugma.internal.runtime.model.MethodIdentifier;

public final class MethodCallVertex extends CallTreeVertex {
    private final MethodIdentifier method;

    MethodCallVertex(MethodIdentifier method) {
        if (method == null) {
            throw new NullPointerException();
        }
        this.method = method;
    }

    @Override
    public void accept(CallTreeVertexVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("%s#%s", method.getOwner().getName(), method.getName());
    }

    public MethodIdentifier getMethod() {
        return method;
    }
}