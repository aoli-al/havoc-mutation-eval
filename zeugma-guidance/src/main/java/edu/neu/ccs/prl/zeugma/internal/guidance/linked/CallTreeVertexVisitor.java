package edu.neu.ccs.prl.zeugma.internal.guidance.linked;

public interface CallTreeVertexVisitor {
    void visit(MethodCallVertex vertex);

    void visit(ParameterRequestVertex vertex);
}
