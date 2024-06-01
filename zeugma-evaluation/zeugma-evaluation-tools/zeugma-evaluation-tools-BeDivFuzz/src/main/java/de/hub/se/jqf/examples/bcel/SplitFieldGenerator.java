/*
 * Based on JQF's FieldGenerator class.
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
import com.pholser.junit.quickcheck.generator.java.lang.StringGenerator;
import de.hub.se.jqf.fuzz.junit.quickcheck.NonTrackingSplitGenerationStatus;
import de.hub.se.jqf.fuzz.junit.quickcheck.SplitSourceOfRandomness;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.AccessFlags;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.Type;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Based on:
 * <a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html">
 * Java Virtual Machine Specification 4
 * </a>
 */
public final class SplitFieldGenerator {
    private static final List<Consumer<? super AccessFlags>> VISIBILITY_SETTERS =
            Arrays.asList((f) -> f.isPrivate(true), (f) -> f.isPublic(true), (f) -> f.isProtected(true), (f) -> {
            });
    private final Generator<String> identifierGenerator = new SplitJavaIdentifierGenerator();
    private final Generator<String> stringConstantGenerator = new StringGenerator();
    private final SplitTypeGenerator typeGenerator;
    private final SplitSourceOfRandomness random;
    private final NonTrackingSplitGenerationStatus status;
    private final ClassGen clazz;

    public SplitFieldGenerator(SplitSourceOfRandomness random, NonTrackingSplitGenerationStatus status,
                               ClassGen clazz) {
        this.random = random;
        this.status = status;
        this.clazz = clazz;
        this.typeGenerator = new SplitTypeGenerator(random, status);
    }

    public Field generate() {
        Type type = typeGenerator.generate();
        String name = identifierGenerator.generate(random, status);
        FieldGen field = new FieldGen(0, type, name, clazz.getConstantPool());
        setAccessFlags(field);
        if (field.isFinal()) {
            setInitValue(type, field);
        }
        return field.getField();
    }

    private void setInitValue(Type type, FieldGen field) {
        if (type instanceof BasicType) {
            // Impacts flow of control, use primary input
            if (random.nextBoolean(true)) {
                switch (type.getType()) {
                    case Const.T_BOOLEAN:
                        field.setInitValue(random.nextBoolean(false));
                        break;
                    case Const.T_BYTE:
                        field.setInitValue(random.nextByte(Byte.MIN_VALUE, Byte.MAX_VALUE, false));
                        break;
                    case Const.T_SHORT:
                        field.setInitValue(random.nextShort(Short.MIN_VALUE, Short.MAX_VALUE, false));
                        break;
                    case Const.T_CHAR:
                        field.setInitValue(random.nextChar(Character.MIN_VALUE, Character.MAX_VALUE, false));
                        break;
                    case Const.T_INT:
                        field.setInitValue(random.nextInt(false));
                        break;
                    case Const.T_LONG:
                        field.setInitValue(random.nextLong(Long.MIN_VALUE, Long.MAX_VALUE, false));
                        break;
                    case Const.T_DOUBLE:
                        field.setInitValue(random.getSecondarySource().nextDouble());
                        break;
                    case Const.T_FLOAT:
                        field.setInitValue(random.getSecondarySource().nextFloat());
                        break;
                }
            }
        } else if (type.equals(Type.STRING)) {
            // Impacts flow of control, use primary input
            if (random.nextBoolean(true)) {
                field.setInitValue(stringConstantGenerator.generate(random, status));
            }
        }
    }

    void setAccessFlags(FieldGen field) {
        // Since the choice of access flags does not impact whether other flags are set, use secondary input
        if (random.nextBoolean(false)) {
            field.isSynthetic(true);
        }
        if (clazz.isInterface()) {
            field.isPublic(true);
            field.isStatic(true);
            field.isFinal(true);
        } else {
            random.choose(VISIBILITY_SETTERS, false).accept(field);
            if (random.nextBoolean(false)) {
                field.isStatic(true);
            }
            if (random.nextBoolean(false)) {
                field.isTransient(true);
            }
            if (random.nextBoolean(false)) {
                field.isEnum(true);
            }
            switch (random.nextInt(3, false)) {
                case 0:
                    field.isFinal(true);
                    break;
                case 1:
                    field.isVolatile(true);
            }
        }
    }
}