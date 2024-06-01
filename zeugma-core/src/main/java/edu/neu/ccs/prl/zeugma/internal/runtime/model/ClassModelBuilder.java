package edu.neu.ccs.prl.zeugma.internal.runtime.model;

import edu.neu.ccs.prl.zeugma.internal.runtime.event.CoreMethodInfo;
import edu.neu.ccs.prl.zeugma.internal.runtime.event.InvokedViaInstrumentation;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;

public final class ClassModelBuilder {
    private final ClassIdentifier classIdentifier;
    private final SimpleList<MethodIdentifier> methods = new SimpleList<>();

    private ClassModelBuilder(String className, long checksum) {
        this.classIdentifier = new ClassIdentifier(className, checksum);
    }

    @InvokedViaInstrumentation(info = CoreMethodInfo.VISIT_METHOD)
    public ClassModelBuilder visitMethod(String name, String desc) {
        methods.add(new MethodIdentifier(classIdentifier, name, desc));
        return this;
    }

    @InvokedViaInstrumentation(info = CoreMethodInfo.BUILD)
    public ClassModel build(int numberOfProbes) {
        return new ClassModel(classIdentifier, methods.toArray(new MethodIdentifier[methods.size()]), numberOfProbes);
    }

    @InvokedViaInstrumentation(info = CoreMethodInfo.CREATE_BUILDER)
    public static ClassModelBuilder create(String className, long checksum) {
        return new ClassModelBuilder(className, checksum);
    }
}