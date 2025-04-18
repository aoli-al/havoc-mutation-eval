package edu.neu.ccs.prl.zeugma.internal.agent;

import edu.neu.ccs.prl.zeugma.internal.runtime.event.CoverageEventBroker;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.Stack;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleMap;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FrameNode;

/**
 * Adds code to publish coverage events to the {@link CoverageEventBroker}.
 * <p>
 * Adds an intermediate jump target for all branch jump edges. Adds code after these intermediate jump targets and
 * before the target of fall edges (the edge between the conditional branch and its successor) to publish the
 * coverage event.
 */
final class CoverageEventClassVisitor extends ClassVisitor {
    /**
     * Name of the class being visited.
     */
    private String className;
    /**
     * Index of the next probe to be visited.
     */
    private int nextProbeIndex = 0;

    CoverageEventClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return ZeugmaTransformer.shouldInstrumentMethod(access, name) ? new BranchCoverageEventClassVisitor(mv) : mv;
    }

    static boolean isConditionalBranch(int opcode) {
        return opcode == Opcodes.IFNULL || opcode == Opcodes.IFNONNULL ||
                (opcode >= Opcodes.IFEQ && opcode <= Opcodes.IF_ACMPNE);
    }

    private class BranchCoverageEventClassVisitor extends MethodVisitor {
        private final SimpleMap<Label, FrameNode> labelFrameMap = new SimpleMap<>();
        private final Stack<Label> visitedLabels = new Stack<>();
        private final SimpleList<Label> intermediates = new SimpleList<>();
        private final SimpleList<Label> originals = new SimpleList<>();

        public BranchCoverageEventClassVisitor(MethodVisitor mv) {
            super(CoverageEventClassVisitor.this.api, mv);
        }

        private void pushModel() {
            super.visitFieldInsn(Opcodes.GETSTATIC,
                    className,
                    ZeugmaAgent.MODEL_FIELD_NAME,
                    ZeugmaAgent.MODEL_FIELD_DESC);
        }

        private void addPublishCall() {
            // Push the model field
            pushModel();
            // Push the index of the branch edge
            MethodRecord.pushInt(mv, nextProbeIndex++);
            // Publish the event
            MethodRecord.PUBLISH_COVERAGE.delegateVisit(mv);
        }

        @Override
        public void visitInsn(int opcode) {
            super.visitInsn(opcode);
            switch (opcode) {
                case Opcodes.LCMP:
                case Opcodes.FCMPG:
                case Opcodes.FCMPL:
                case Opcodes.DCMPG:
                case Opcodes.DCMPL:
                    // -1, 0, or 1
                    // Copy the result
                    super.visitInsn(Opcodes.DUP);
                    //Push the model and swap with the result
                    pushModel();
                    super.visitInsn(Opcodes.SWAP);
                    // Add 1 + nextProbeIndex
                    MethodRecord.pushInt(mv, nextProbeIndex + 1);
                    super.visitInsn(Opcodes.IADD);
                    // Publish the event
                    MethodRecord.PUBLISH_COVERAGE.delegateVisit(mv);
                    // Increment the next probe index by 3 for the 3 possible outcomes.
                    nextProbeIndex += 3;
                    break;
            }
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            if (isConditionalBranch(opcode)) {
                // Create an intermediate for the original jump target
                Label intermediate = createIntermediate(label);
                // Visit the conditional branch instruction
                super.visitJumpInsn(opcode, intermediate);
                // Publish the branch edge execution event for the fall edge
                addPublishCall();
            } else {
                super.visitJumpInsn(opcode, label);
            }
        }

        @Override
        public void visitLabel(Label label) {
            visitedLabels.push(label);
            super.visitLabel(label);
        }

        @Override
        public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
            FrameNode frame = new FrameNode(type, numLocal, local, numStack, stack);
            super.visitFrame(type, numLocal, local, numStack, stack);
            while (!visitedLabels.isEmpty()) {
                labelFrameMap.put(visitedLabels.pop(), frame);
            }
        }

        @Override
        public void visitTableSwitchInsn(int min, int max, Label defaultLabel, Label... labels) {
            SimpleMap<Label, Label> targetMap = createIntermediates(defaultLabel, labels);
            super.visitTableSwitchInsn(min, max, targetMap.get(defaultLabel), remapTargets(targetMap, labels));
        }

        @Override
        public void visitLookupSwitchInsn(Label defaultLabel, int[] keys, Label[] labels) {
            SimpleMap<Label, Label> targetMap = createIntermediates(defaultLabel, labels);
            super.visitLookupSwitchInsn(targetMap.get(defaultLabel), keys, remapTargets(targetMap, labels));
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            // Add the intermediate jump targets
            for (int i = 0; i < intermediates.size(); i++) {
                Label intermediate = intermediates.get(i);
                Label original = originals.get(i);
                FrameNode frame = labelFrameMap.get(original);
                // Visit the intermediate jump target
                super.visitLabel(intermediate);
                // If there was a frame for the original target, visit a copy of that frame
                if (frame != null) {
                    frame.accept(this.mv);
                }
                // Publish the branch edge execution event
                addPublishCall();
                // Jump to the original target
                super.visitJumpInsn(Opcodes.GOTO, original);
            }
            super.visitMaxs(maxStack, maxLocals);
        }

        private Label createIntermediate(Label original) {
            Label intermediate = new Label();
            // Record the original target for the intermediate
            intermediates.add(intermediate);
            originals.add(original);
            return intermediate;
        }

        private Label[] remapTargets(SimpleMap<Label, Label> labelMap, Label[] originals) {
            // Replace the original jump targets with the intermediates
            Label[] result = new Label[originals.length];
            for (int i = 0; i < originals.length; i++) {
                result[i] = labelMap.get(originals[i]);
            }
            return result;
        }

        private SimpleMap<Label, Label> createIntermediates(Label defaultLabel, Label[] labels) {
            // Create an intermediate target for each unique jump target
            SimpleMap<Label, Label> targetMap = new SimpleMap<>();
            targetMap.put(defaultLabel, createIntermediate(defaultLabel));
            for (Label label : labels) {
                if (!targetMap.containsKey(label)) {
                    targetMap.put(label, createIntermediate(label));
                }
            }
            return targetMap;
        }
    }
}