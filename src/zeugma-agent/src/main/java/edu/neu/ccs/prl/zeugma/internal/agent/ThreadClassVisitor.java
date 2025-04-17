package edu.neu.ccs.prl.zeugma.internal.agent;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Adds an {@link Object} field to {@link Thread} instances.
 */
final class ThreadClassVisitor extends ClassVisitor {
    /**
     * Name of the class targeted by instances of this class.
     */
    static final String TARGET_CLASS_NAME = "java/lang/Thread";
    /**
     * Access modifiers of the field to be added.
     */
    static final int FIELD_ACCESS = Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PUBLIC;
    /**
     * Descriptor of the field to be added.
     */
    static final String FIELD_DESC = "Ljava/lang/Object;";
    /**
     * Name of the field to be added.
     */
    static final String FIELD_NAME = ZeugmaAgent.ADDED_MEMBER_PREFIX + "FLAG";

    public ThreadClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visitEnd() {
        super.visitField(FIELD_ACCESS, FIELD_NAME, FIELD_DESC, null, null);
        super.visitEnd();
    }

    static boolean isApplicable(String className) {
        return TARGET_CLASS_NAME.equals(className);
    }
}
