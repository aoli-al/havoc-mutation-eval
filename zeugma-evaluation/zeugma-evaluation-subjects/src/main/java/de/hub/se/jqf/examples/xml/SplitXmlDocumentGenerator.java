package de.hub.se.jqf.examples.xml;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import org.w3c.dom.Document;

/**
 * Placeholder excluded from final JAR.
 */
public class SplitXmlDocumentGenerator extends Generator<String> {
    public SplitXmlDocumentGenerator() {
        super(String.class);
    }

    @Override
    public String generate(SourceOfRandomness random, GenerationStatus status) {
        throw new AssertionError("Placeholder method invoked");
    }
}