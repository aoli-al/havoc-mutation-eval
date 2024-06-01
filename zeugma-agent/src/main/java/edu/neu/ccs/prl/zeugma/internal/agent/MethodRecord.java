package edu.neu.ccs.prl.zeugma.internal.agent;

import edu.neu.ccs.prl.zeugma.internal.runtime.event.CoreMethodInfo;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Stores information needed to add a call to a method.
 */
public enum MethodRecord {
    CREATE_BUILDER(CoreMethodInfo.CREATE_BUILDER),
    VISIT_METHOD(CoreMethodInfo.VISIT_METHOD),
    BUILD(CoreMethodInfo.BUILD),
    PUBLISH_COVERAGE(CoreMethodInfo.PUBLISH_COVERAGE),
    PUBLISH_ENTERING_METHOD(CoreMethodInfo.PUBLISH_ENTERING_METHOD),
    PUBLISH_EXITING_METHOD(CoreMethodInfo.PUBLISH_EXITING_METHOD),
    PUBLISH_RESTORE(CoreMethodInfo.PUBLISH_RESTORE);

    private final int opcode;
    private final String owner;
    private final String name;
    private final String descriptor;
    private final boolean isInterface;

    MethodRecord(CoreMethodInfo info) {
        this(info.getOpcode(),
                info.getOwner(),
                info.getName(),
                info.getReturnType(),
                info.isInterface(),
                info.getParameterTypes());
    }

    MethodRecord(int opcode, Class<?> owner, String name, Class<?> returnType, boolean isInterface,
                 Class<?>... parameterTypes) {
        this(opcode, Type.getInternalName(owner), name, isInterface, createDescriptor(returnType, parameterTypes));
    }

    MethodRecord(int opcode, String owner, String name, boolean isInterface, String descriptor) {
        if (owner == null || name == null || descriptor == null) {
            throw new NullPointerException();
        }
        this.opcode = opcode;
        this.owner = owner;
        this.name = name;
        this.isInterface = isInterface;
        this.descriptor = descriptor;
    }

    /**
     * @return the opcode of the type instruction associated with this method
     */
    public int getOpcode() {
        return opcode;
    }

    /**
     * @return the internal name of this method's owner class
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @return this method's name
     */
    public String getName() {
        return name;
    }

    /**
     * @return this method's descriptor
     */
    public String getDescriptor() {
        return descriptor;
    }

    /**
     * @return true if this method's owner class is an interface
     */
    public boolean isInterface() {
        return isInterface;
    }

    /**
     * Tells the specified method visitor to visit a method instruction for this method.
     *
     * @param methodVisitor the method visitor that should visit this method
     */
    public void delegateVisit(MethodVisitor methodVisitor) {
        methodVisitor.visitMethodInsn(getOpcode(), getOwner(), getName(), getDescriptor(), isInterface());
    }

    public boolean matches(String owner, String name, String desc) {
        return getOwner().equals(owner) && getName().equals(name) && getDescriptor().equals(desc);
    }

    /**
     * Loads the specified value onto the stack.
     *
     * @param delegate the method visitor that should be used to load the specified value onto the stack
     * @param value    the value to be pushed onto the stack
     */
    public static void pushInt(MethodVisitor delegate, int value) {
        if (value >= -1 && value <= 5) {
            delegate.visitInsn(Opcodes.ICONST_0 + value);
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            delegate.visitIntInsn(Opcodes.BIPUSH, value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            delegate.visitIntInsn(Opcodes.SIPUSH, value);
        } else {
            delegate.visitLdcInsn(value);
        }
    }

    public static String createDescriptor(Class<?> returnType, Class<?>... parameterTypes) {
        Type[] parameters = new Type[parameterTypes.length];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = Type.getType(parameterTypes[i]);
        }
        return Type.getMethodDescriptor(Type.getType(returnType), parameters);
    }

    public static void pushIntArray(MethodVisitor mv, int[] a) {
        pushInt(mv, a.length);
        mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
        for (int i = 0; i < a.length; i++) {
            mv.visitInsn(Opcodes.DUP);
            pushInt(mv, i);
            pushInt(mv, a[i]);
            mv.visitInsn(Opcodes.IASTORE);
        }
    }
}
