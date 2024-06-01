package edu.neu.ccs.prl.zeugma.internal.agent;

import edu.neu.ccs.prl.zeugma.internal.runtime.model.ClassModel;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Adds a class initialization method to a class if it does not already have one.
 * Adds a {@link ClassModel} field to the class.
 * Adds code to initialize the class' {@link ClassModel} field to the beginning of its class initialization method.
 */
final class InitializingClassVisitor extends ClassVisitor {
    /**
     * True if a class initialization method has been visited.
     */
    private boolean visitedClassInitializer;
    /**
     * Name of the class being visited.
     */
    private String className;
    /**
     * True if the class being visited is an interface.
     */
    private boolean isInterface;

    InitializingClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
        this.className = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if ("<clinit>".equals(name)) {
            visitedClassInitializer = true;
            mv = new InitializingMethodVisitor(api, mv, isInterface);
        }
        return mv;
    }

    @Override
    public void visitEnd() {
        // Add a class initialization method if one was not visited
        if (!visitedClassInitializer) {
            MethodVisitor mv =
                    super.visitMethod(Opcodes.ACC_SYNTHETIC | Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            mv = new InitializingMethodVisitor(api, mv, isInterface);
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        // Add the field
        super.visitField(ZeugmaAgent.MODEL_FIELD_ACCESS,
                ZeugmaAgent.MODEL_FIELD_NAME,
                ZeugmaAgent.MODEL_FIELD_DESC,
                null,
                null);
        super.visitEnd();
    }

    private final class InitializingMethodVisitor extends MethodVisitor {
        private final boolean isInterface;

        private InitializingMethodVisitor(int api, MethodVisitor mv, boolean isInterface) {
            super(api, mv);
            this.isInterface = isInterface;
        }

        @Override
        public void visitCode() {
            super.visitCode();
            // Call the initialization method
            super.visitMethodInsn(Opcodes.INVOKESTATIC,
                    className,
                    ZeugmaAgent.INIT_NAME,
                    ZeugmaAgent.INIT_DESC,
                    isInterface);
            // Assign the result to the field
            super.visitFieldInsn(Opcodes.PUTSTATIC,
                    className,
                    ZeugmaAgent.MODEL_FIELD_NAME,
                    ZeugmaAgent.MODEL_FIELD_DESC);
        }
    }
}
