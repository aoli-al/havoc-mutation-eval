/*
 * Based on JQF's TypeGenerator class.
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

import com.pholser.junit.quickcheck.generator.Generator;
import de.hub.se.jqf.fuzz.junit.quickcheck.NonTrackingSplitGenerationStatus;
import de.hub.se.jqf.fuzz.junit.quickcheck.SplitSourceOfRandomness;
import org.apache.bcel.generic.*;

import java.util.Arrays;
import java.util.List;

public class SplitTypeGenerator {
    private final List<ObjectType> COMMON_TYPES =
            Arrays.asList(Type.OBJECT, Type.CLASS, Type.STRING, Type.STRINGBUFFER, Type.THROWABLE);
    private final List<BasicType> PRIMITIVE_TYPES =
            Arrays.asList(Type.BOOLEAN, Type.INT, Type.SHORT, Type.BYTE, Type.LONG, Type.DOUBLE, Type.FLOAT, Type.CHAR);
    private final Generator<String> classNameGenerator = new SplitJavaClassNameGenerator("/");
    private final SplitSourceOfRandomness random;
    private final NonTrackingSplitGenerationStatus status;

    public SplitTypeGenerator(SplitSourceOfRandomness random, NonTrackingSplitGenerationStatus status) {
        this.random = random;
        this.status = status;
    }

    public Type generate() {
        switch (random.nextInt(3, true)) {
            case 0:
                return generateArrayType();
            case 1:
                return generateObjectType();
            default:
                return generatePrimitiveType();
        }
    }

    public BasicType generatePrimitiveType() {
        return random.choose(PRIMITIVE_TYPES, false);
    }

    public ArrayType generateArrayType() {
        Type type = random.nextBoolean(true) ? generatePrimitiveType() : generateObjectType();
        return new ArrayType(type, random.nextInt(1, 10, false));
    }

    public ObjectType generateObjectType() {
        if (random.nextBoolean(true)) {
            return random.choose(COMMON_TYPES, false);
        } else {
            return new ObjectType(classNameGenerator.generate(random, status));
        }
    }

    public ReferenceType generateReferenceType() {
        return random.nextBoolean(true) ? generateArrayType() : generateObjectType();
    }
}
