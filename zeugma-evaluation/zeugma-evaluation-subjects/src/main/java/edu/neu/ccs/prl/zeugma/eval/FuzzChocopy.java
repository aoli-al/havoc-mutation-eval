package edu.neu.ccs.prl.zeugma.eval;

import chocopy.common.astnodes.Program;
import chocopy.reference.RefAnalysis;
import chocopy.reference.RefParser;
import com.pholser.junit.quickcheck.From;
import de.hub.se.jqf.examples.chocopy.SplitChocoPySemanticGenerator;
import de.hub.se.jqf.examples.js.SplitJavaScriptCodeGenerator;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.neu.ccs.prl.zeugma.generators.ChocoPySemanticGenerator;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assume.assumeFalse;

@RunWith(JQF.class)
public class FuzzChocopy {

    /** Entry point for fuzzing reference ChocoPy semantic analysis with ChocoPy code generator */
    @Fuzz
    public void testWithGenerator(@From(ChocoPySemanticGenerator.class) String code) {
        Program program = RefParser.process(code, false);
        assumeFalse(program.hasErrors());
        Program refTypedProgram = RefAnalysis.process(program);
        assumeFalse(refTypedProgram.hasErrors());
    }

    @Fuzz
    public void testWithSplitGenerator(@From(SplitChocoPySemanticGenerator.class) String code) throws IOException {
        Program program = RefParser.process(code, false);
        assumeFalse(program.hasErrors());
        Program refTypedProgram = RefAnalysis.process(program);
        assumeFalse(refTypedProgram.hasErrors());
    }

}
