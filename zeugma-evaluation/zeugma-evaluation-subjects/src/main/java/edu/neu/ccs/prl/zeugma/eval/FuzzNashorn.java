/*
 * Based on BeDivFuzz's CompilerTest which is available at:
 * https://github.com/hub-se/BeDivFuzz/blob/main/examples/src/test/java/edu/berkeley/cs/jqf/examples/nashorn
 * /CompilerTest.java
 *
 * BeDivFuzz is licensed under the following terms:
 *
 * Copyright (c) 2017-2020 The Regents of the University of California
 * Copyright (c) 2021-2022 Rohan Padhye and JQF Contributors
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
package edu.neu.ccs.prl.zeugma.eval;

import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.js.JavaScriptCodeGenerator;
import de.hub.se.jqf.examples.js.SplitJavaScriptCodeGenerator;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.Assume;
import org.junit.runner.RunWith;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

@RunWith(JQF.class)
public class FuzzNashorn {
    private static final ScriptEngineManager factory = new ScriptEngineManager();
    private static final ScriptEngine engine = factory.getEngineByName("nashorn");

    public void testWithReader(Reader reader) {
        try {
            ((Compilable) engine).compile(reader);
        } catch (ScriptException e) {
            Assume.assumeNoException(e);
        }
    }

    @Fuzz
    public void testWithInputStream(InputStream in) {
        testWithReader(new InputStreamReader(in));
    }

    @Fuzz
    public void testWithGenerator(@From(JavaScriptCodeGenerator.class) String s) {
        testWithReader(new StringReader(s));
    }

    @Fuzz
    public void testWithSplitGenerator(@From(SplitJavaScriptCodeGenerator.class) String s) {
        testWithReader(new StringReader(s));
    }
}