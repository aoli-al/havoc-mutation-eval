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
package edu.berkeley.cs.jqf.examples.closure;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.LogManager;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.common.AsciiStringGenerator;
import edu.berkeley.cs.jqf.examples.js.JavaScriptCodeGenerator;
import edu.berkeley.cs.jqf.examples.js.ReversedJSCodeGenerator;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.apache.commons.io.IOUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JQF.class)
public class CompilerTest {

    static {
        // Disable all logging by Closure passes
        LogManager.getLogManager().reset();
    }

    @Fuzz
    public void testWithInputStream(InputStream in) throws IOException {
        Compiler compiler = new Compiler(new PrintStream(new ByteArrayOutputStream(), false));
        CompilerOptions options = new CompilerOptions();
        compiler.disableThreads();
        options.setPrintConfig(false);
        CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
        SourceFile input = SourceFile.fromCode("input", readStream(in));
        Result result = compiler.compile(SourceFile.fromCode("externs", ""), input, options);
        Assume.assumeTrue(result.success);
    }

    static String readStream(InputStream in) throws IOException {
        in = new BufferedInputStream(in);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int result = in.read(); result != -1; result = in.read()) {
            buffer.write((byte) result);
        }
        return buffer.toString(StandardCharsets.UTF_8.name());
    }


    @Fuzz
    public void testWithString(@From(AsciiStringGenerator.class) String code) throws IOException {
        testWithInputStream(new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8)));
    }

    @Fuzz
    public void debugWithString(@From(AsciiStringGenerator.class) String code) throws IOException {
        testWithString(code);
    }

    @Test
    public void smallTest() throws IOException {
        debugWithString("x <<= Infinity");
    }


    @Fuzz
    public void debugWithInputStream(InputStream in) throws IOException {
        String input = IOUtils.toString(in, StandardCharsets.UTF_8);
        debugWithString(input);
    }

    @Fuzz(repro = "/Users/aoli/tmp/closure-ei-no-havoc-testWithGenerator-results-0/corpus/id_000001")
    public void testWithGenerator(@From(JavaScriptCodeGenerator.class) String code) throws IOException {
        testWithString(code);
    }

    @Fuzz
    public void testWithReversedGenerator(@From(ReversedJSCodeGenerator.class) String code) throws IOException {
        testWithString(code);
    }

    @Fuzz
    public void debugWithGenerator(@From(JavaScriptCodeGenerator.class) String code) throws IOException {
        debugWithString(code);
    }
}
