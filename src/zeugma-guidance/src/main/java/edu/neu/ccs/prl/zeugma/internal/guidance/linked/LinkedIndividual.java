package edu.neu.ccs.prl.zeugma.internal.guidance.linked;

import edu.neu.ccs.prl.zeugma.internal.guidance.FuzzTarget;
import edu.neu.ccs.prl.zeugma.internal.guidance.Individual;
import edu.neu.ccs.prl.zeugma.internal.runtime.model.MethodIdentifier;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleMap;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

public class LinkedIndividual extends Individual {
    private SimpleMap<MethodIdentifier, SimpleList<MethodCallVertex>> donationMap = null;
    private SimpleList<MethodCallVertex> applicationPoints = null;

    public LinkedIndividual(ByteList input, String generatedInput) {
        super(input, generatedInput);
    }

    SimpleList<MethodCallVertex> getApplicationPoints() {
        if (applicationPoints == null) {
            throw new IllegalStateException(getClass().getSimpleName() + " instance has not been initialized.");
        }
        return applicationPoints;
    }

    SimpleMap<MethodIdentifier, SimpleList<MethodCallVertex>> getDonationMap() {
        if (donationMap == null) {
            throw new IllegalStateException(getClass().getSimpleName() + " instance has not been initialized.");
        }
        return donationMap;
    }

    @Override
    public void initialize(FuzzTarget target) {
        CallTreeBuilder builder = new CallTreeBuilder(target);
        CallTree callTree = builder.build(getInput());
        donationMap = new SimpleMap<>();
        applicationPoints = new SimpleList<>();
        callTree.preorderTraverse(new CallTreeVertexVisitor() {
            @Override
            public void visit(MethodCallVertex vertex) {
                MethodIdentifier method = vertex.getMethod();
                if (!donationMap.containsKey(method)) {
                    donationMap.put(method, new SimpleList<>());
                }
                donationMap.get(method).add(vertex);
                if (vertex.getParent().getNumberOfChildren() > 1 && vertex.getNumberOfDescendantLeaves() > 1 &&
                        vertex.getSourceInterval().size() != callTree.getSourceInterval().size()) {
                    applicationPoints.add(vertex);
                }
            }

            @Override
            public void visit(ParameterRequestVertex vertex) {
            }
        });
    }
}
