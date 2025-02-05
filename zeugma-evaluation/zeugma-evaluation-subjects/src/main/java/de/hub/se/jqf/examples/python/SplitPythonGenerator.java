package de.hub.se.jqf.examples.python;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import org.w3c.dom.Document;

public class SplitPythonGenerator extends Generator<String> {
    public SplitPythonGenerator() {
        super(String.class);
    }
    @Override
    public String generate(SourceOfRandomness random, GenerationStatus status) {
        throw new AssertionError("Placeholder method invoked");
    }

}
