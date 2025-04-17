package edu.neu.ccs.prl.zeugma.internal.runtime.event;

import edu.neu.ccs.prl.zeugma.internal.runtime.model.ClassModel;
import edu.neu.ccs.prl.zeugma.internal.runtime.model.ClassModelBuilder;

public enum CoreMethodInfo {
    CREATE_BUILDER(true, ClassModelBuilder.class, "create", ClassModelBuilder.class, false, String.class, long.class),
    VISIT_METHOD(false,
            ClassModelBuilder.class,
            "visitMethod",
            ClassModelBuilder.class,
            false,
            String.class,
            String.class),
    BUILD(false, ClassModelBuilder.class, "build", ClassModel.class, false, int.class),
    PUBLISH_COVERAGE(true, CoverageEventBroker.class, "covered", void.class, false, ClassModel.class, int.class),
    PUBLISH_ENTERING_METHOD(true, ContextEventBroker.class, "entering", int.class, false, ClassModel.class, int.class),
    PUBLISH_EXITING_METHOD(true, ContextEventBroker.class, "exiting", void.class, false, int.class),
    PUBLISH_RESTORE(true, ContextEventBroker.class, "restore", void.class, false, int.class),
    PUBLISH_METHOD_CALL(true, ContextEventBroker.class, "methodCall", void.class, false, ClassModel.class, int.class);
    private final int opcode;
    private final Class<?> owner;
    private final String name;
    private final Class<?> returnType;
    private final boolean isInterface;
    private final Class<?>[] parameterTypes;

    CoreMethodInfo(boolean isStatic, Class<?> owner, String name, Class<?> returnType, boolean isInterface,
                   Class<?>... parameterTypes) {
        this.opcode = isStatic ? 184 : 182;
        this.owner = owner;
        this.name = name;
        this.returnType = returnType;
        this.isInterface = isInterface;
        this.parameterTypes = parameterTypes;
    }

    /**
     * Returns the opcode of the type instruction associated with the method
     *
     * @return the opcode of the type instruction associated with the method
     */
    public int getOpcode() {
        return opcode;
    }

    /**
     * Returns the class that owns the method.
     *
     * @return the class that owns the method
     */
    public Class<?> getOwner() {
        return owner;
    }

    /**
     * Returns the method's return type.
     *
     * @return the method's return type
     */
    public Class<?> getReturnType() {
        return returnType;
    }

    /**
     * Returns the types of the method's parameters.
     *
     * @return the types of the method's parameters
     */
    public Class<?>[] getParameterTypes() {
        return parameterTypes.clone();
    }

    /**
     * Returns the method's name.
     *
     * @return the method's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether the class that owns the method is an interface.
     *
     * @return whether the class that owns the method is an interface
     */
    public boolean isInterface() {
        return isInterface;
    }
}
