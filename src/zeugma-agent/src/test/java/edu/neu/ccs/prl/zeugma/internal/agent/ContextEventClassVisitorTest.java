package edu.neu.ccs.prl.zeugma.internal.agent;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicVerifier;

import java.util.stream.Stream;

class ContextEventClassVisitorTest {
    @ParameterizedTest
    @MethodSource("arguments")
    void instrumentationPassesVerification(ClassNode cn) throws AnalyzerException {
        cn = TestUtil.instrument(cn, ContextEventClassVisitor::new);
        for (MethodNode mn : cn.methods) {
            new Analyzer<>(new BasicVerifier()).analyze(cn.name, mn);
        }
    }

    static Stream<ClassNode> arguments() {
        return TestUtil.classes();
    }
}