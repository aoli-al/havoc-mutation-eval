package de.hub.se.jqf.examples.chocopy;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

public class SplitChocoPySemanticGenerator extends Generator<String> {
    public SplitChocoPySemanticGenerator() {
        super(String.class);
    }

    @Override
    public String generate(SourceOfRandomness random, GenerationStatus status) {
        throw new AssertionError("Placeholder method invoked");
    }
}
