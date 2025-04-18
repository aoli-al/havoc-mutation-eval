package edu.neu.ccs.prl.zeugma.internal.agent;

import edu.neu.ccs.prl.zeugma.internal.runtime.model.ClassModel;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleSet;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Builds a method for initializing the {@link ClassModel} for a class.
 */
final class InitializationMethodBuilder extends ClassVisitor {
    /**
     * Initialization method being built by this instance.
     */
    private final MethodNode init =
            new MethodNode(ZeugmaAgent.INIT_ACCESS, ZeugmaAgent.INIT_NAME, ZeugmaAgent.INIT_DESC, null, null);
    /**
     * CRC-64 checksum of the clas.
     */
    private final long checksum;
    /**
     * True if at least one method has been visited.
     */
    private boolean hasModel = false;
    /**
     * Number of probes added to the class.
     */
    private int numberOfProbes;

    /**
     * @param api      ASM API version implemented this visitor
     * @param checksum CRC-64 checksum of the class
     */
    InitializationMethodBuilder(int api, long checksum) {
        super(api);
        this.checksum = checksum;
    }

    public MethodNode getInit() {
        return init;
    }

    public boolean hasModel() {
        return hasModel;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        init.visitCode();
        init.visitLdcInsn(name);
        init.visitLdcInsn(checksum);
        MethodRecord.CREATE_BUILDER.delegateVisit(init);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (ZeugmaTransformer.shouldInstrumentMethod(access, name)) {
            hasModel = true;
            init.visitLdcInsn(name);
            init.visitLdcInsn(desc);
            MethodRecord.VISIT_METHOD.delegateVisit(init);
            return new ProbeCountingMethodVisitor();
        }
        return null;
    }

    @Override
    public void visitEnd() {
        MethodRecord.pushInt(init, numberOfProbes);
        MethodRecord.BUILD.delegateVisit(init);
        init.visitInsn(Opcodes.ARETURN);
        init.visitMaxs(-1, -1);
        init.visitEnd();
    }

    private class ProbeCountingMethodVisitor extends MethodVisitor {
        public ProbeCountingMethodVisitor() {
            super(InitializationMethodBuilder.this.api);
        }

        @Override
        public void visitInsn(int opcode) {
            switch (opcode) {
                case Opcodes.LCMP:
                case Opcodes.FCMPG:
                case Opcodes.FCMPL:
                case Opcodes.DCMPG:
                case Opcodes.DCMPL:
                    numberOfProbes += 3;
                    break;
            }
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            if (CoverageEventClassVisitor.isConditionalBranch(opcode)) {
                numberOfProbes += 2;
            }
        }

        @Override
        public void visitTableSwitchInsn(int min, int max, Label defaultLabel, Label... labels) {
            SimpleSet<Label> targets = new SimpleSet<>();
            for (Label label : labels) {
                targets.add(label);
            }
            targets.add(defaultLabel);
            numberOfProbes += targets.size();
        }

        @Override
        public void visitLookupSwitchInsn(Label defaultLabel, int[] keys, Label[] labels) {
            SimpleSet<Label> targets = new SimpleSet<>();
            for (Label label : labels) {
                targets.add(label);
            }
            targets.add(defaultLabel);
            numberOfProbes += targets.size();
        }
    }
}