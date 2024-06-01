/*
 * Based on JQF's WebXmlTest which is available at:
 * https://github.com/rohanpadhye/JQF/blob/master/examples/src/test/java/edu/berkeley/cs/jqf/examples/tomcat
 * /WebXmlTest.java
 *
 * WebXmlTest is licensed under the following terms:
 *
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
package edu.neu.ccs.prl.zeugma.eval;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.generator.Size;
import edu.berkeley.cs.jqf.examples.common.Dictionary;
import de.hub.se.jqf.examples.xml.SplitXmlDocumentGenerator;
import edu.berkeley.cs.jqf.examples.xml.XmlDocumentGenerator;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.apache.tomcat.util.descriptor.web.WebXml;
import org.apache.tomcat.util.descriptor.web.WebXmlParser;
import org.junit.Assume;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.PrintStream;

@RunWith(JQF.class)
public class FuzzTomcat {
    @Fuzz
    public void testWithInputStream(InputStream in) {
        PrintStream err = FuzzUtil.suppressStandardErr();
        try {
            WebXml webXml = new WebXml();
            WebXmlParser parser = new WebXmlParser(false, false, true);
            Assume.assumeTrue(parser.parseWebXml(new InputSource(in), webXml, false));
        } finally {
            System.setErr(err);
        }
    }

    @Fuzz
    public void testWithGenerator(@From(XmlDocumentGenerator.class)
                                  @Size(max = 10)
                                  @Dictionary("dictionaries/tomcat.dict")
                                  Document document) {
        testWithInputStream(FuzzUtil.toInputStream(document));
    }

    @Fuzz
    public void testWithSplitGenerator(
            @From(SplitXmlDocumentGenerator.class)
            @Size(max = 10)
            @Dictionary("dictionaries/tomcat.dict")
            Document document) {
        testWithInputStream(FuzzUtil.toInputStream(document));
    }
}