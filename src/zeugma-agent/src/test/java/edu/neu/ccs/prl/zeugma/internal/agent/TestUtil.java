package edu.neu.ccs.prl.zeugma.internal.agent;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.objectweb.asm.Opcodes.*;

@SuppressWarnings("CommentedOutCode")
public class TestUtil {
    public static final String OWNER = Type.getInternalName(TestUtil.class);

    public static MethodNode tryCatchLongTops() {
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "write", "(J)V", null, null);
        mn.visitCode();
        Label l0 = new Label();
        Label l1 = new Label();
        mn.visitTryCatchBlock(l0, l1, l1, "java/io/IOException");
        mn.visitLabel(l0);
        mn.visitVarInsn(LLOAD, 0);
        mn.visitMethodInsn(INVOKESTATIC, "Example", "consume", "(J)V", false);
        mn.visitInsn(RETURN);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 2, new Object[]{TOP, TOP}, 1, new Object[]{"java/io/IOException"});
        mn.visitInsn(ATHROW);
        mn.visitMaxs(3, 2);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode forLoop() {
        //        int count = 0;
        //        for (int i = 0; i < 10; i++) {
        //            count += 20;
        //        }
        //        example(count);
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "forLoop", "()V", null, null);
        mn.visitCode();
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 0);
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 1);
        Label l0 = new Label();
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 2, new Object[]{INTEGER, INTEGER}, 0, new Object[0]);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitIntInsn(BIPUSH, 10);
        Label l1 = new Label();
        mn.visitJumpInsn(IF_ICMPGE, l1);
        mn.visitIincInsn(0, 20);
        mn.visitIincInsn(1, 1);
        mn.visitJumpInsn(GOTO, l0);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitVarInsn(ILOAD, 0);
        mn.visitMethodInsn(INVOKESTATIC, OWNER, "example", "(I)V", false);
        mn.visitInsn(RETURN);
        mn.visitMaxs(2, 2);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode ifElse() {
        //        if (b) {
        //            example(1);
        //        } else {
        //            example(2);
        //        }
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "ifElse", "(Z)V", null, null);
        mn.visitCode();
        mn.visitVarInsn(ILOAD, 0);
        Label l0 = new Label();
        mn.visitJumpInsn(IFEQ, l0);
        mn.visitInsn(ICONST_1);
        mn.visitMethodInsn(INVOKESTATIC, OWNER, "example", "(I)V", false);
        Label l1 = new Label();
        mn.visitJumpInsn(GOTO, l1);
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitInsn(ICONST_2);
        mn.visitMethodInsn(INVOKESTATIC, OWNER, "example", "(I)V", false);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(1, 1);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode ifStatement() {
        //        if (b) {
        //            example(1);
        //        }
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "ifStatement", "(Z)V", null, null);
        mn.visitCode();
        mn.visitVarInsn(ILOAD, 0);
        Label l0 = new Label();
        mn.visitJumpInsn(IFEQ, l0);
        mn.visitInsn(ICONST_1);
        mn.visitMethodInsn(INVOKESTATIC, OWNER, "example", "(I)V", false);
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(1, 1);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode nestedTryCatch() {
        //        try {
        //            try {
        //                example(1);
        //            } catch (RuntimeException e) {
        //                example(2);
        //            }
        //        } catch (Exception e) {
        //            example(3);
        //        }
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "nestedTryCatch", "()V", null, null);
        mn.visitCode();
        Label l0 = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mn.visitTryCatchBlock(l0, l1, l2, "java/lang/RuntimeException");
        Label l3 = new Label();
        Label l4 = new Label();
        mn.visitTryCatchBlock(l0, l3, l4, "java/lang/Exception");
        mn.visitLabel(l0);
        mn.visitInsn(ICONST_1);
        mn.visitMethodInsn(INVOKESTATIC, OWNER, "example", "(I)V", false);
        mn.visitLabel(l1);
        mn.visitJumpInsn(GOTO, l3);
        mn.visitLabel(l2);
        mn.visitFrame(F_NEW, 0, new Object[0], 1, new Object[]{"java/lang/RuntimeException"});
        mn.visitVarInsn(ASTORE, 0);
        mn.visitInsn(ICONST_2);
        mn.visitMethodInsn(INVOKESTATIC, OWNER, "example", "(I)V", false);
        mn.visitLabel(l3);
        mn.visitFrame(F_SAME, 0, null, 0, null);
        Label l5 = new Label();
        mn.visitJumpInsn(GOTO, l5);
        mn.visitLabel(l4);
        mn.visitFrame(F_NEW, 0, new Object[0], 1, new Object[]{"java/lang/Exception"});
        mn.visitVarInsn(ASTORE, 0);
        mn.visitInsn(ICONST_3);
        mn.visitMethodInsn(INVOKESTATIC, OWNER, "example", "(I)V", false);
        mn.visitLabel(l5);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(1, 1);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode multipleCatchBlocks() {
        //        try {
        //            example(1);
        //        } catch (RuntimeException e) {
        //            example(2);
        //        } catch (Exception e) {
        //            example(3);
        //        }
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "multipleCatchBlocks", "()V", null, null);
        mn.visitCode();
        Label l0 = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mn.visitTryCatchBlock(l0, l1, l2, "java/lang/RuntimeException");
        Label l3 = new Label();
        mn.visitTryCatchBlock(l0, l1, l3, "java/lang/Exception");
        mn.visitLabel(l0);
        mn.visitInsn(ICONST_1);
        mn.visitMethodInsn(INVOKESTATIC, OWNER, "example", "(I)V", false);
        mn.visitLabel(l1);
        Label l4 = new Label();
        mn.visitJumpInsn(GOTO, l4);
        mn.visitLabel(l2);
        mn.visitFrame(F_NEW, 0, new Object[0], 1, new Object[]{"java/lang/RuntimeException"});
        mn.visitVarInsn(ASTORE, 0);
        mn.visitInsn(ICONST_2);
        mn.visitMethodInsn(INVOKESTATIC, OWNER, "example", "(I)V", false);
        mn.visitJumpInsn(GOTO, l4);
        mn.visitLabel(l3);
        mn.visitFrame(F_NEW, 0, new Object[0], 1, new Object[]{"java/lang/Exception"});
        mn.visitVarInsn(ASTORE, 0);
        mn.visitInsn(ICONST_3);
        mn.visitMethodInsn(INVOKESTATIC, OWNER, "example", "(I)V", false);
        mn.visitLabel(l4);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(1, 1);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode instanceInitializerWithJumpBeforeSuper() {
        //        ProbePlacerTestMethods(int[] a, boolean b) {
        //            super(b ? a[0] : a[1]);
        //        }
        MethodNode mn = new MethodNode(0, "<init>", "([IZ)V", null, null);
        mn.visitCode();
        mn.visitVarInsn(ALOAD, 0);
        mn.visitVarInsn(ILOAD, 2);
        Label l0 = new Label();
        mn.visitJumpInsn(IFEQ, l0);
        mn.visitVarInsn(ALOAD, 1);
        mn.visitInsn(ICONST_0);
        mn.visitInsn(IALOAD);
        Label l1 = new Label();
        mn.visitJumpInsn(GOTO, l1);
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 3, new Object[]{UNINITIALIZED_THIS, "[I", INTEGER}, 1, new Object[]{UNINITIALIZED_THIS});
        mn.visitVarInsn(ALOAD, 1);
        mn.visitInsn(ICONST_1);
        mn.visitInsn(IALOAD);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW,
                3,
                new Object[]{UNINITIALIZED_THIS, "[I", INTEGER},
                2,
                new Object[]{UNINITIALIZED_THIS, INTEGER});
        mn.visitMethodInsn(INVOKESPECIAL, "edu/neu/ccs/Parent", "<init>", "(I)V", false);
        mn.visitInsn(RETURN);
        mn.visitMaxs(3, 3);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode example() {
        //        if (b) {
        //            example(22);
        //        }
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "example", "()V", null, null);
        mn.visitCode();
        mn.visitFieldInsn(GETSTATIC, OWNER, "b", "Z");
        Label l0 = new Label();
        mn.visitJumpInsn(IFEQ, l0);
        mn.visitIntInsn(BIPUSH, 22);
        mn.visitMethodInsn(INVOKESTATIC, OWNER, "example", "(I)V", false);
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(1, 0);
        mn.visitEnd();
        return mn;
    }

    static ClassNode createClassNode(MethodNode mn) {
        ClassNode cn = new ClassNode(ZeugmaAgent.ASM_VERSION);
        cn.visit(V1_8, ACC_PUBLIC + ACC_SUPER, OWNER, null, "java/lang/Object", null);
        cn.methods.add(mn);
        return cn;
    }

    static Stream<ClassNode> classes() {
        List<ClassNode> result = new LinkedList<>();
        TestUtil.methods().map(TestUtil::createClassNode).forEach(result::add);
        ClassNode cn = TestUtil.createClassNode(TestUtil.example());
        cn.methods.add(TestUtil.ifElse());
        result.add(cn);
        Stream.of(String.class,
                HashMap.class,
                Arrays.class,
                StringBuilder.class,
                StringBuffer.class,
                GregorianCalendar.class).map(TestUtil::getClassNode).forEach(result::add);
        return result.stream();
    }

    static ClassNode getClassNode(Class<?> clazz) {
        try {
            ClassNode cn = new ClassNode();
            new ClassReader(clazz.getName()).accept(cn, ClassReader.EXPAND_FRAMES);
            return cn;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Stream<MethodNode> methods() {
        return Stream.of(forLoop(),
                ifElse(),
                ifStatement(),
                nestedTryCatch(),
                multipleCatchBlocks(),
                instanceInitializerWithJumpBeforeSuper(),
                example(),
                tryCatchLongTops());
    }

    static ClassNode expandFramesAndComputeMaxStack(ClassNode cn) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cn.accept(cw);
        ClassReader cr = new ClassReader(cw.toByteArray());
        ClassNode result = new ClassNode();
        cr.accept(result, ClassReader.EXPAND_FRAMES);
        return result;
    }

    static ClassNode instrument(ClassNode cn, BiFunction<Integer, ClassVisitor, ClassVisitor> f) {
        ClassNode result = new ClassNode();
        cn = expandFramesAndComputeMaxStack(cn);
        cn.accept(f.apply(ZeugmaAgent.ASM_VERSION, result));
        return expandFramesAndComputeMaxStack(result);
    }

    static int getValue(AbstractInsnNode in) {
        if (in instanceof InsnNode) {
            switch (in.getOpcode()) {
                case ICONST_M1:
                    return -1;
                case ICONST_0:
                    return 0;
                case ICONST_1:
                    return 1;
                case ICONST_2:
                    return 2;
                case ICONST_3:
                    return 3;
                case ICONST_4:
                    return 4;
                case ICONST_5:
                    return 5;
                default:
                    throw new IllegalArgumentException();
            }
        } else if (in instanceof IntInsnNode) {
            return ((IntInsnNode) in).operand;
        } else if (in instanceof LdcInsnNode) {
            return (Integer) ((LdcInsnNode) in).cst;
        }
        throw new IllegalArgumentException();
    }

    static List<AbstractInsnNode> collect(ClassNode cn, Predicate<AbstractInsnNode> predicate) {
        List<AbstractInsnNode> result = new LinkedList<>();
        for (MethodNode mn : cn.methods) {
            if (ZeugmaTransformer.shouldInstrumentMethod(mn.access, mn.name)) {
                for (AbstractInsnNode in : mn.instructions) {
                    if (predicate.test(in)) {
                        result.add(in);
                    }
                }
            }
        }
        return result;
    }
}
