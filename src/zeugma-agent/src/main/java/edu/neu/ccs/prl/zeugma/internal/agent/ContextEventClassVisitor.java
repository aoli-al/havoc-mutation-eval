package edu.neu.ccs.prl.zeugma.internal.agent;

import edu.neu.ccs.prl.zeugma.internal.runtime.event.ContextEventBroker;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleSet;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.*;

/**
 * Adds code to publish events to the {@link ContextEventBroker}.
 */
final class ContextEventClassVisitor extends ClassVisitor {
    /**
     * Name of the class being visited.
     */
    private String className;
    /**
     * Index of the next method to be visited.
     */
    private int nextMethodIndex = 0;

    ContextEventClassVisitor(int api, ClassVisitor classVisitor) {
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
        if (ZeugmaTransformer.shouldInstrumentMethod(access, name)) {
            ContextEventMethodVisitor contextMv = new ContextEventMethodVisitor(api, mv, access, name, desc);
            mv = new MethodNode(api, access, name, desc, signature, exceptions) {
                private boolean hasFrames = false;

                @Override
                public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
                    super.visitFrame(type, numLocal, local, numStack, stack);
                    hasFrames = true;
                }

                @Override
                public void visitEnd() {
                    super.visitEnd();
                    contextMv.setNumberOfTryCatchBlocks(this.tryCatchBlocks.size());
                    contextMv.hasFrames(hasFrames);
                    this.accept(contextMv);
                }
            };
        }
        return mv;
    }

    private final class ContextEventMethodVisitor extends LocalVariablesSorter {
        /**
         * True if the method being visited is an instance initialization method.
         */
        private final boolean isInitializer;
        /**
         * Label marking the start of the scope of the exception handler to be added to the method being visited.
         */
        private final Label scopeStart = new Label();
        /**
         * Label marking the end of the scope and start of the code for the exception handler to be added to the method
         * being visited.
         */
        private final Label scopeEnd = new Label();
        /**
         * Set of labels that mark the start of exception handlers.
         */
        private final SimpleSet<Label> handlers = new SimpleSet<>();
        /**
         * Label marking the end of the level local variable's scope.
         */
        private final Label levelEnd = new Label();
        /**
         * Total number of try-catch blocks to be visited.
         */
        private int totalBlocks;
        /**
         * Number of try-catch blocks that have been visited so far.
         */
        private int visitedBlocks = 0;
        private int levelVar;
        private boolean hasFrames;
        private boolean publishHandlerAfterNextFrame = false;

        ContextEventMethodVisitor(int api, MethodVisitor mv, int access, String name, String desc) {
            super(api, access, desc, mv);
            this.isInitializer = "<init>".equals(name);
        }

        private void onExit() {
            mv.visitVarInsn(Opcodes.ILOAD, levelVar);
            MethodRecord.PUBLISH_EXITING_METHOD.delegateVisit(mv);
        }

        private void onEnter() {
            // Push the model field
            super.visitFieldInsn(Opcodes.GETSTATIC,
                    className,
                    ZeugmaAgent.MODEL_FIELD_NAME,
                    ZeugmaAgent.MODEL_FIELD_DESC);
            MethodRecord.pushInt(mv, nextMethodIndex++);
            MethodRecord.PUBLISH_ENTERING_METHOD.delegateVisit(mv);
            mv.visitVarInsn(Opcodes.ISTORE, levelVar);
        }

        private void callRestore() {
            mv.visitVarInsn(Opcodes.ILOAD, levelVar);
            MethodRecord.PUBLISH_RESTORE.delegateVisit(mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            Label levelStart = new Label();
            super.visitLabel(levelStart);
            levelVar = newLocal(org.objectweb.asm.Type.INT_TYPE);
            mv.visitLocalVariable(ZeugmaAgent.ADDED_MEMBER_PREFIX + "level", "I", null, levelStart, levelEnd, levelVar);
            onEnter();
            checkHandlers();
        }

        @Override
        public void visitInsn(int opcode) {
            if (IRETURN <= opcode && opcode <= RETURN) {
                // Note: ATHROW instructions are handled by the added exception handler
                onExit();
            }
            super.visitInsn(opcode);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            callRestore();
        }

        @Override
        public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle,
                                           Object... bootstrapMethodArguments) {
            super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
            callRestore();
        }

        @Override
        public void visitLabel(Label label) {
            super.visitLabel(label);
            if (hasFrames) {
                publishHandlerAfterNextFrame |= handlers.contains(label);
            } else if (handlers.contains(label)) {
                callRestore();
            }
        }

        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            super.visitTryCatchBlock(start, end, handler, type);
            handlers.add(handler);
            visitedBlocks++;
            checkHandlers();
        }

        private void checkHandlers() {
            if (totalBlocks == visitedBlocks && !isInitializer) {
                super.visitTryCatchBlock(scopeStart, scopeEnd, scopeEnd, null);
                super.visitLabel(scopeStart);
            }
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            if (!isInitializer) {
                super.visitLabel(scopeEnd);
                // Allow LocalVariablesSorter to fix the frame to include the level local variable
                super.visitFrame(F_NEW, 0, new Object[0], 1, new Object[]{"java/lang/Throwable"});
                onExit();
                super.visitInsn(ATHROW);
            }
            super.visitLabel(levelEnd);
            super.visitMaxs(maxStack, maxLocals);
        }

        @Override
        public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
            super.visitFrame(type, numLocal, local, numStack, stack);
            if (publishHandlerAfterNextFrame) {
                callRestore();
                publishHandlerAfterNextFrame = false;
            }
        }

        public void setNumberOfTryCatchBlocks(int size) {
            totalBlocks = size;
        }

        public void hasFrames(boolean hasFrames) {
            this.hasFrames = hasFrames;
        }
    }
}
