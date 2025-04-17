package de.hub.se.jqf.examples.bcel;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import org.apache.bcel.classfile.JavaClass;

/**
 * Placeholder excluded from final JAR.
 */
public class SplitJavaClassGenerator extends Generator<JavaClass> {
    public SplitJavaClassGenerator() {
        super(JavaClass.class);
    }

    @Override
    public JavaClass generate(SourceOfRandomness random, GenerationStatus status) {
        throw new AssertionError("Placeholder method invoked");
    }
}
