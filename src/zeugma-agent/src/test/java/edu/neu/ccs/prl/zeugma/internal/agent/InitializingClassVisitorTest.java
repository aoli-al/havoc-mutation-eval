package edu.neu.ccs.prl.zeugma.internal.agent;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicVerifier;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InitializingClassVisitorTest {
    @ParameterizedTest
    @MethodSource("arguments")
    void instrumentationPassesVerification(ClassNode cn) throws AnalyzerException {
        cn = TestUtil.instrument(cn, InitializingClassVisitor::new);
        for (MethodNode mn : cn.methods) {
            new Analyzer<>(new BasicVerifier()).analyze(cn.name, mn);
        }
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void classInitializerEnsured(ClassNode cn) {
        cn = TestUtil.instrument(cn, InitializingClassVisitor::new);
        boolean foundClassInitializer = false;
        for (MethodNode mn : cn.methods) {
            if (mn.name.equals("<clinit>")) {
                foundClassInitializer = true;
                break;
            }
        }
        assertTrue(foundClassInitializer, "Expected class to have a class initializer");
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void fieldAddedToClass(ClassNode cn) {
        cn = TestUtil.instrument(cn, InitializingClassVisitor::new);
        boolean found = false;
        for (FieldNode field : cn.fields) {
            if (field.name.equals(ZeugmaAgent.MODEL_FIELD_NAME)) {
                found = true;
                assertEquals(field.access, ZeugmaAgent.MODEL_FIELD_ACCESS);
                assertEquals(field.desc, ZeugmaAgent.MODEL_FIELD_DESC);
                break;
            }
        }
        assertTrue(found, "Expected" + ZeugmaAgent.MODEL_FIELD_NAME + " field to be added to class");
    }

    static Stream<ClassNode> arguments() {
        return TestUtil.classes();
    }
}