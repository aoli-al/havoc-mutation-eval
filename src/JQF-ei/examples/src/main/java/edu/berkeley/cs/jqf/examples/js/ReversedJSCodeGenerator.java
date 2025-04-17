/*
 * Copyright (c) 2017-2018 The Regents of the University of California
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
package edu.berkeley.cs.jqf.examples.js;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.berkeley.cs.jqf.examples.common.AsciiStringGenerator;

import static edu.berkeley.cs.jqf.examples.js.JavaScriptCodeGenerator.*;
import static java.lang.Math.*;

/**
 * @author Rohan Padhye
 */
public class ReversedJSCodeGenerator extends Generator<String> {
    public ReversedJSCodeGenerator() {
        super(String.class);
    }

    private GenerationStatus status;

    private static Set<String> identifiers;
    private int statementDepth;
    private int expressionDepth;


    private static final String[] UNARY_TOKENS = {
            "!", "++", "--", "~",
            "delete", "new", "typeof"
    };

    private static final String[] BINARY_TOKENS = {
            "!=", "!==", "%", "%=", "&", "&&", "&=", "*", "*=", "+", "+=", ",",
            "-", "-=", "/", "/=", "<", "<<", ">>=", "<=", "=", "==", "===",
            ">", ">=", ">>", ">>=", ">>>", ">>>=", "^", "^=", "|", "|=", "||",
            "in", "instanceof"
    };

    @Override
    public String generate(SourceOfRandomness random, GenerationStatus status) {
        this.status = status;
        this.identifiers = new HashSet<>();
        this.statementDepth = 0;
        this.expressionDepth = 0;
        return generateStatement(random).toString();
    }

    private static int sampleGeometric(SourceOfRandomness random, double mean) {
        double p = 1 / mean;
        double uniform = random.nextDouble();
        return (int) ceil(log(1 - uniform) / log(1 - p));
    }

    private static <T> List<T> generateItems(Function<SourceOfRandomness, T> generator, SourceOfRandomness random,
                                             double mean) {
        int len = sampleGeometric(random, mean);
        List<T> items = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            items.add(0, generator.apply(random));
        }
        return items;
    }

    private String generateExpression(SourceOfRandomness random) {
        expressionDepth++;
        // Choose between terminal or non-terminal
        String result;
        if (expressionDepth >= MAX_EXPRESSION_DEPTH || random.nextBoolean()) {
            result = random.choose(Arrays.<Function<SourceOfRandomness, String>>asList(
                    this::generateLiteralNode,
                    this::generateIdentNode
            )).apply(random);
        } else {
            result = random.choose(Arrays.<Function<SourceOfRandomness, String>>asList(
                    this::generateBinaryNode,
                    this::generateUnaryNode,
                    this::generateTernaryNode,
                    this::generateCallNode,
                    this::generateFunctionNode,
                    this::generatePropertyNode,
                    this::generateIndexNode,
                    this::generateArrowFunctionNode
            )).apply(random);
        }
        expressionDepth--;
        return "(" + result + ")";
    }

    private String generateStatement(SourceOfRandomness random) {
        statementDepth++;
        String result;
        if (statementDepth >= MAX_STATEMENT_DEPTH || random.nextBoolean()) {
            result = random.choose(Arrays.<Function<SourceOfRandomness, String>>asList(
                    this::generateExpressionStatement,
                    this::generateBreakNode,
                    this::generateContinueNode,
                    this::generateReturnNode,
                    this::generateThrowNode,
                    this::generateVarNode,
                    this::generateEmptyNode
            )).apply(random);
        } else {
            result = random.choose(Arrays.<Function<SourceOfRandomness, String>>asList(
                    this::generateIfNode,
                    this::generateForNode,
                    this::generateWhileNode,
                    this::generateNamedFunctionNode,
                    this::generateSwitchNode,
                    this::generateTryNode,
                    this::generateBlockStatement
            )).apply(random);
        }
        statementDepth--;
        return result;
    }


    private String generateBinaryNode(SourceOfRandomness random) {
        String rhs = generateExpression(random);
        String token = random.choose(BINARY_TOKENS);
        String lhs = generateExpression(random);

        return lhs + " " + token + " " + rhs;
    }

    private String generateBlock(SourceOfRandomness random) {
        return "{ " + String.join(";", generateItems(this::generateStatement, random, 4)) + " }";
    }

    private String generateBlockStatement(SourceOfRandomness random) {
        return generateBlock(random);
    }

    private String generateBreakNode(SourceOfRandomness random) {
        return "break";
    }

    private String generateCallNode(SourceOfRandomness random) {
        String args = String.join(",", generateItems(this::generateExpression, random, 3));
        String func = generateExpression(random);

        String call = func + "(" + args + ")";
        if (random.nextBoolean()) {
            return call;
        } else {
            return "new " + call;
        }
    }

    private String generateCaseNode(SourceOfRandomness random) {
        String block = generateBlock(random);
        String expr = generateExpression(random);
        return "case " + expr + ": " +  block;
    }

    private String generateCatchNode(SourceOfRandomness random) {
        String block = generateBlock(random);
        String iden = generateIdentNode(random);
        return "catch (" + iden + ") " + block;
    }

    private String generateContinueNode(SourceOfRandomness random) {
        return "continue";
    }

    private String generateEmptyNode(SourceOfRandomness random) {
        return "";
    }

    private String generateExpressionStatement(SourceOfRandomness random) {
        return generateExpression(random);
    }

    private String generateForNode(SourceOfRandomness random) {
        String block = generateBlock(random);
        boolean hasExit = random.nextBoolean();
        String exit = "";
        if (hasExit) {
            exit = generateExpression(random);
        }
        String update = "";
        boolean hasUpdate = random.nextBoolean();
        if (hasUpdate) {
            update = generateExpression(random);
        }
        boolean hasInit = random.nextBoolean();
        String init = "";
        if (hasInit) {
            init = generateExpression(random);
        }

        String s = "for(" + init + ";" + update + ";" + exit + ")" + block;
        return s;
    }

    private String generateFunctionNode(SourceOfRandomness random) {
        String block = generateBlock(random);
        String params = String.join(", ", generateItems(this::generateIdentNode, random, 5));
        return "function(" + params + ")" + block;
    }

    private String generateNamedFunctionNode(SourceOfRandomness random) {
        String block = generateBlock(random);
        String params = String.join(", ", generateItems(this::generateIdentNode, random, 5));
        String name = generateIdentNode(random);
        return "function " + name + "(" + params + ")" + block;
    }

    private String generateArrowFunctionNode(SourceOfRandomness random) {
        String impl;
        if (random.nextBoolean()) {
            impl = generateBlock(random);
        } else {
            impl = generateExpression(random);
        }
        String params = "(" + String.join(", ", generateItems(this::generateIdentNode, random, 3)) + ")";
        return params + " => " + impl;

    }

    private String generateIdentNode(SourceOfRandomness random) {
        // Either generate a new identifier or use an existing one
        String identifier;
        if (identifiers.isEmpty() || (identifiers.size() < MAX_IDENTIFIERS && random.nextBoolean())) {
            identifier = random.nextChar('a', 'z') + "_" + identifiers.size();
            identifiers.add(identifier);
        } else {
            identifier = random.choose(identifiers);
        }

        return identifier;
    }

    private String generateIfNode(SourceOfRandomness random) {
        String elseBlock = (random.nextBoolean() ? " else " + generateBlock(random) : "");
        String block = generateBlock(random);
        String expr = generateExpression(random);
        return "if (" + expr + ") " + block + elseBlock;
    }

    private String generateIndexNode(SourceOfRandomness random) {
        String index = generateExpression(random);
        String obj = generateExpression(random);
        return obj + "[" + index + "]";
    }

    private String generateObjectProperty(SourceOfRandomness random) {
        String prop = generateExpression(random);
        String obj = generateIdentNode(random);
        return obj + ": " + prop;
    }

    private String generateLiteralNode(SourceOfRandomness random) {
        if (expressionDepth < MAX_EXPRESSION_DEPTH && random.nextBoolean()) {
            if (random.nextBoolean()) {
                // Array literal
                return "[" + String.join(", ", generateItems(this::generateExpression, random, 3)) + "]";
            } else {
                // Object literal
                return "{" + String.join(", ", generateItems(this::generateObjectProperty, random, 3)) + "}";

            }
        } else {
            return random.choose(Arrays.<Supplier<String>>asList(
                    () -> String.valueOf(random.nextInt(-10, 1000)),
                    () -> String.valueOf(random.nextBoolean()),
                    () -> '"' + new AsciiStringGenerator().generate(random, status) + '"',
                    () -> "undefined",
                    () -> "null",
                    () -> "this"
            )).get();
        }
    }

    private String generatePropertyNode(SourceOfRandomness random) {
        String prop = generateIdentNode(random);
        String obj = generateExpression(random);
        return  obj + "." + prop;
    }

    private String generateReturnNode(SourceOfRandomness random) {
        return "return " + generateExpression(random);
    }

    private String generateSwitchNode(SourceOfRandomness random) {
        String conds = String.join(" ", generateItems(this::generateCaseNode, random, 2));
        String expr = generateExpression(random);

        return "switch(" + expr + ") {"
                + conds + "}";
    }

    private String generateTernaryNode(SourceOfRandomness random) {
        String cond2 = generateExpression(random);
        String cond1 = generateExpression(random);
        String expr = generateExpression(random);
        return  expr + " ? " + cond1 + " : " + cond2;
    }

    private String generateThrowNode(SourceOfRandomness random) {
        return "throw " + generateExpression(random);
    }

    private String generateTryNode(SourceOfRandomness random) {
        String catchBlock = generateCatchNode(random);
        String tryBlock = generateBlock(random);
        return "try " + tryBlock + catchBlock;
    }

    private String generateUnaryNode(SourceOfRandomness random) {
        String expr = generateExpression(random);
        String token = random.choose(UNARY_TOKENS);
        return token + " " + expr;
    }

    private String generateVarNode(SourceOfRandomness random) {
        return "var " + generateIdentNode(random);
    }

    private String generateWhileNode(SourceOfRandomness random) {
        String expr = generateExpression(random);
        String block = generateBlock(random);
        return "while (" + expr + ")" + block;
    }
}
