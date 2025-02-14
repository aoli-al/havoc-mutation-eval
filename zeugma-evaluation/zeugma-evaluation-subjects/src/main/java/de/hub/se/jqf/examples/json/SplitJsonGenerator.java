package de.hub.se.jqf.examples.json;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

public class SplitJsonGenerator extends Generator<String> {
    public SplitJsonGenerator() {
        super(String.class);
    }

    @Override
    public String generate(SourceOfRandomness sourceOfRandomness, GenerationStatus generationStatus) {
        return "";
    }
}
