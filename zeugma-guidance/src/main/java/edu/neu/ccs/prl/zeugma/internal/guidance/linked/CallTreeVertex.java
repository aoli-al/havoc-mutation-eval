package edu.neu.ccs.prl.zeugma.internal.guidance.linked;

import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.util.Interval;

public abstract class CallTreeVertex {
    private final SimpleList<CallTreeVertex> children = new SimpleList<>();
    private Interval sourceInterval;
    private CallTreeVertex parent;
    private int numberOfDescendantLeaves = -1;

    CallTreeVertex(Interval sourceInterval) {
        this.sourceInterval = sourceInterval;
    }

    CallTreeVertex() {
    }

    public CallTreeVertex getParent() {
        return parent;
    }

    void addChild(CallTreeVertex child) {
        if (child.parent != null) {
            throw new IllegalStateException();
        }
        child.parent = this;
        children.add(child);
    }

    public CallTreeVertex getChild(int index) {
        return children.get(index);
    }

    public int getNumberOfChildren() {
        return children.size();
    }

    public abstract void accept(CallTreeVertexVisitor visitor);

    public Interval getSourceInterval() {
        if (sourceInterval == null && !children.isEmpty()) {
            int start = getChild(0).getSourceInterval().getStart();
            int end = getChild(children.size() - 1).getSourceInterval().getEnd();
            sourceInterval = new Interval(start, end);
        }
        return sourceInterval;
    }

    public void preorderTraverse(CallTreeVertexVisitor visitor) {
        // Visits the subtree rooted at this vertex in preorder (parent visited first then children)
        accept(visitor);
        for (int i = 0; i < children.size(); i++) {
            children.get(i).preorderTraverse(visitor);
        }
    }

    public int getNumberOfDescendantLeaves() {
        if (numberOfDescendantLeaves == -1) {
            // Count this vertex if it is a leaf
            numberOfDescendantLeaves = children.isEmpty() ? 1 : 0;
            for (int i = 0; i < children.size(); i++) {
                numberOfDescendantLeaves += children.get(i).getNumberOfDescendantLeaves();
            }
        }
        return numberOfDescendantLeaves;
    }
}
