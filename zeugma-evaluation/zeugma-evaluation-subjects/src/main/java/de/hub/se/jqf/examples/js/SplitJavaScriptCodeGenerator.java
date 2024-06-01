package de.hub.se.jqf.examples.js;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

/**
 * Placeholder excluded from final JAR.
 */
public class SplitJavaScriptCodeGenerator extends Generator<String> {
    public SplitJavaScriptCodeGenerator() {
        super(String.class);
    }

    @Override
    public String generate(SourceOfRandomness random, GenerationStatus status) {
        throw new AssertionError("Placeholder method invoked");
    }
}