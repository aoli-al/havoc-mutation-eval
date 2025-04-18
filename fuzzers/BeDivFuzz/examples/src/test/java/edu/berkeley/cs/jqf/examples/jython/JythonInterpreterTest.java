package edu.berkeley.cs.jqf.examples.jython;


import com.pholser.junit.quickcheck.From;
import de.hub.se.jqf.examples.python.SplitPythonGenerator;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;
import org.python.antlr.base.mod;
import org.python.compiler.LegacyCompiler;
import org.python.core.CompileMode;
import org.python.core.CompilerFlags;
import org.python.core.ParserFacade;
import org.python.core.Py;
import org.python.core.PythonCodeBundle;
import org.python.core.PythonCompiler;

@RunWith(JQF.class)
public class JythonInterpreterTest {

    @Fuzz
    public void testWithSplitGenerator(@From(SplitPythonGenerator.class) String code) throws Exception {
        PythonCompiler compiler = new LegacyCompiler();
        if (code.contains("\0")) {
            throw Py.TypeError("compile() expected string without null bytes");
        }
        CompilerFlags cflags = new CompilerFlags();
        CompileMode mode = CompileMode.exec;
        String filename = "<String>";
        mod node = ParserFacade.parse(code, mode, filename, cflags);
        PythonCodeBundle bundle = compiler.compile(node, "org.python.pycode._pyx", filename,
                true, true, cflags);
    }
}

