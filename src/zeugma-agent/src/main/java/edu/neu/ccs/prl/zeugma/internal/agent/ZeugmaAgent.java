package edu.neu.ccs.prl.zeugma.internal.agent;

import edu.neu.ccs.prl.zeugma.internal.runtime.model.ClassModel;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.instrument.Instrumentation;

public final class ZeugmaAgent {
    /**
     * Prefix of packages that should not be instrumented.
     */
    public static final String INTERNAL_PACKAGE_PREFIX = "edu/neu/ccs/prl/zeugma/internal/";
    /**
     * ASM API version implemented by the class and method visitors used by transformers.
     */
    public static final int ASM_VERSION = Opcodes.ASM9;
    /**
     * Prefix of the names of class members added via instrumentation.
     */
    public static final String ADDED_MEMBER_PREFIX = "$$XEBEC_";
    /**
     * Base name of the model field.
     */
    public static final String MODEL_FIELD_NAME = ADDED_MEMBER_PREFIX + "MODEL";
    /**
     * Access modifiers of the model field.
     */
    public static final int MODEL_FIELD_ACCESS =
            Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL;
    /**
     * Descriptor of the model field.
     */
    public static final String MODEL_FIELD_DESC = Type.getDescriptor(ClassModel.class);
    /**
     * Name of the initialization method.
     */
    public static final String INIT_NAME = ADDED_MEMBER_PREFIX + "INIT";
    /**
     * Descriptor of the initialization method.
     */
    public static final String INIT_DESC = "()" + MODEL_FIELD_DESC;
    /**
     * Access modifiers of the initialization method.
     */
    public static final int INIT_ACCESS = Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC;
    /**
     * Name of the class used to access the field added to {@link Thread}.
     */
    public static final String THREAD_FIELD_ACCESSOR_CLASS_NAME =
            "edu/neu/ccs/prl/zeugma/internal/runtime/event/ThreadFieldAccessor";
    /**
     * Prefix of core packages that need to be packed into the base module or added to the boot class path.
     */
    public static final String RUNTIME_PACKAGE_PREFIX = "edu/neu/ccs/prl/zeugma/internal/runtime/";

    private ZeugmaAgent() {
        throw new AssertionError();
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new ZeugmaTransformer());
    }
}
