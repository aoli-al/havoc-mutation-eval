package edu.neu.ccs.prl.zeugma.internal.agent;

import edu.neu.ccs.prl.zeugma.internal.runtime.event.ThreadFieldAccessor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * Replaces the method body of {@link ThreadFieldAccessor}'s {@code get} method to
 * return the value of the field added to {@link Thread} instances for the {@link Thread} passed as an
 * argument.
 * <p>
 * Replaces the method body of {@link ThreadFieldAccessor}'s {@code set} method to
 * set the value of the field added to {@link Thread} instances for the {@link Thread} passed as an argument.
 */
final class ThreadAccessorClassVisitor extends ClassVisitor {
    ThreadAccessorClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv_ = super.visitMethod(access, name, desc, signature, exceptions);
        if ("set".equals(name)) {
            return new MethodVisitor(api) {
                @Override
                public void visitCode() {
                    mv_.visitCode();
                    mv_.visitVarInsn(ALOAD, 0);
                    mv_.visitVarInsn(ALOAD, 1);
                    mv_.visitFieldInsn(PUTFIELD,
                            ThreadClassVisitor.TARGET_CLASS_NAME,
                            ThreadClassVisitor.FIELD_NAME,
                            ThreadClassVisitor.FIELD_DESC);
                    mv_.visitInsn(RETURN);
                    mv_.visitMaxs(0, 0);
                    mv_.visitEnd();
                }
            };
        } else if ("get".equals(name)) {
            return new MethodVisitor(api) {
                @Override
                public void visitCode() {
                    mv_.visitCode();
                    mv_.visitVarInsn(ALOAD, 0);
                    mv_.visitFieldInsn(GETFIELD,
                            ThreadClassVisitor.TARGET_CLASS_NAME,
                            ThreadClassVisitor.FIELD_NAME,
                            ThreadClassVisitor.FIELD_DESC);
                    mv_.visitInsn(ARETURN);
                    mv_.visitMaxs(0, 0);
                    mv_.visitEnd();
                }
            };
        }
        return mv_;
    }

    static boolean isApplicable(String className) {
        return ZeugmaAgent.THREAD_FIELD_ACCESSOR_CLASS_NAME.equals(className);
    }
}