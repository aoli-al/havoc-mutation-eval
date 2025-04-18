package edu.berkeley.cs.jqf.examples.jython;


import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.chocopy.ChocoPySemanticGenerator;
import edu.berkeley.cs.jqf.examples.python.PythonGenerator;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.berkeley.cs.jqf.fuzz.guidance.TimeoutException;
import org.junit.runner.RunWith;
import org.python.antlr.base.mod;
import org.python.compiler.LegacyCompiler;
import org.python.core.CompileMode;
import org.python.core.CompilerFlags;
import org.python.core.ParserFacade;
import org.python.core.PyException;
import org.python.core.Py;
import org.python.core.PythonCodeBundle;
import org.python.core.PythonCompiler;
import org.python.util.PythonInterpreter;

@RunWith(JQF.class)
public class JythonInterpreterTest {
    @Fuzz
    public void testWithGenerator(@From(PythonGenerator.class) String code) throws Throwable {
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
