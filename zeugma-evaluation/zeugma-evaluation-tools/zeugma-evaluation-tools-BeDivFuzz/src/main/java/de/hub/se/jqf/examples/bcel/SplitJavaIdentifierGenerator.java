/*
 * Based on JQF's JavaIdentifierGenerator class.
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
import com.pholser.junit.quickcheck.generator.Size;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.hub.se.jqf.fuzz.junit.quickcheck.SplitSourceOfRandomness;

import static com.pholser.junit.quickcheck.internal.Ranges.Type.INTEGRAL;
import static com.pholser.junit.quickcheck.internal.Ranges.checkRange;

public final class SplitJavaIdentifierGenerator extends Generator<String> {
    private static final char MIN_VALID_CHAR = '$';
    private static final char MAX_VALID_CHAR = 'z';
    private int minSize = 1;
    private int maxSize = 30;

    public SplitJavaIdentifierGenerator() {
        super(String.class);
    }

    @Override
    public String generate(SourceOfRandomness random, GenerationStatus status) {
        return generate((SplitSourceOfRandomness) random);

    }

    @SuppressWarnings("unused")
    public void configure(Size size) {
        if (size.min() <= 0) {
            throw new IllegalArgumentException("Minimum size must be positive");
        }
        checkRange(INTEGRAL, size.min(), size.max());
        minSize = size.min();
        maxSize = size.max();
    }

    public String generate(SplitSourceOfRandomness random) {
        // Choosing a count, use the primary input
        int size = random.nextInt(minSize, maxSize, true);
        char[] values = new char[size];
        values[0] = generateJavaIdentifierStart(random);
        for (int i = 1; i < values.length; i++) {
            values[i] = generateJavaIdentifierPart(random);
        }
        return new String(values);
    }

    static char generateJavaIdentifierStart(SplitSourceOfRandomness random) {
        // Use secondary input for char data
        char c = random.nextChar(MIN_VALID_CHAR, MAX_VALID_CHAR, false);
        if (!Character.isJavaIdentifierStart(c)) {
            return mapToAlpha(c);
        }
        return c;
    }

    static char generateJavaIdentifierPart(SplitSourceOfRandomness random) {
        // Use secondary input for char data
        char c = random.nextChar(MIN_VALID_CHAR, MAX_VALID_CHAR, false);
        if (!Character.isJavaIdentifierPart(c)) {
            return mapToAlpha(c);
        }
        return c;
    }

    static char mapToAlpha(char c) {
        char min = 'a';
        char max = 'z';
        int range = max - min + 1;
        return (char) ((c % range) + min);
    }
}
