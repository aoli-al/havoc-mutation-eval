/*
 * Based on JQF's JavaClassNameGenerator class.
 * JQF is licensed under the following terms:
 *
 * Copyright (c) 2017-2020 The Regents of the University of California
 * Copyright (c) 2021-2023 Rohan Padhye and JQF Contributors
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.hub.se.jqf.examples.bcel;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.hub.se.jqf.fuzz.junit.quickcheck.NonTrackingSplitGenerationStatus;
import de.hub.se.jqf.fuzz.junit.quickcheck.SplitSourceOfRandomness;

public class SplitJavaClassNameGenerator extends Generator<String> {
    private static final String[] BASIC_CLASS_NAMES = {"java/lang/Object",
            "java/util/List",
            "java/util/Map",
            "java/lang/String",
            "example/A",
            "example/B",
            "java/lang/Throwable",
            "java/lang/RuntimeException"};
    private final Generator<String> identifierGenerator = new SplitJavaIdentifierGenerator();
    private final String delimiter;

    public SplitJavaClassNameGenerator() {
        this("/");
    }

    public SplitJavaClassNameGenerator(String delimiter) {
        super(String.class);
        this.delimiter = delimiter;
    }

    @Override
    public String generate(SourceOfRandomness random, GenerationStatus status) {
        return generate((SplitSourceOfRandomness) random, (NonTrackingSplitGenerationStatus) status);

    }

    public String generate(SplitSourceOfRandomness random, NonTrackingSplitGenerationStatus status) {
        // Impacts flow of control, use the primary input
        if (random.nextBoolean(true)) {
            return random.choose(BASIC_CLASS_NAMES, false);
        }
        // Choosing a count, use the primary input
        String[] parts = new String[random.nextInt(1, 5, true)];
        for (int i = 0; i < parts.length; i++) {
            parts[i] = identifierGenerator.generate(random, status);
        }
        return String.join(delimiter, parts);
    }
}
