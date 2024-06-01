package edu.neu.ccs.prl.zeugma.internal.agent;

import edu.neu.ccs.prl.zeugma.internal.runtime.event.CoverageEventBroker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicVerifier;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class CoverageEventClassVisitorTest {
    @ParameterizedTest
    @MethodSource("arguments")
    void instrumentationPassesVerification(ClassNode cn) throws AnalyzerException {
        cn = TestUtil.instrument(cn, CoverageEventClassVisitor::new);
        for (MethodNode mn : cn.methods) {
            new Analyzer<>(new BasicVerifier()).analyze(cn.name, mn);
        }
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void probeIndicesAreUniqueRange(ClassNode cn) {
        cn = TestUtil.instrument(cn, CoverageEventClassVisitor::new);
        List<AbstractInsnNode> publishCalls = TestUtil.collect(cn, CoverageEventClassVisitorTest::isPublishCall);
        List<Integer> indices = publishCalls.stream()
                .map(CoverageEventClassVisitorTest::extractProbeIndices)
                .flatMapToInt(IntStream::of)
                .boxed()
                .sorted()
                .collect(Collectors.toList());
        List<AbstractInsnNode> primitiveCompares =
                TestUtil.collect(cn, CoverageEventClassVisitorTest::isPrimitiveCompare);
        Assertions.assertEquals(IntStream.range(0, publishCalls.size() + 2 * primitiveCompares.size())
                .boxed()
                .collect(Collectors.toList()), indices);
    }

    static int[] extractProbeIndices(AbstractInsnNode in) {
        if (in.getPrevious().getOpcode() == Opcodes.IADD) {
            // Primitive Comparison
            int mid = TestUtil.getValue(in.getPrevious().getPrevious());
            return new int[]{mid - 1, mid, mid + 1};
        } else {
            return new int[]{TestUtil.getValue(in.getPrevious())};
        }
    }

    static boolean isPrimitiveCompare(AbstractInsnNode in) {
        switch (in.getOpcode()) {
            case Opcodes.FCMPG:
            case Opcodes.FCMPL:
            case Opcodes.LCMP:
            case Opcodes.DCMPG:
            case Opcodes.DCMPL:
                return true;
            default:
                return false;
        }
    }

    static boolean isPublishCall(AbstractInsnNode in) {
        return in instanceof MethodInsnNode &&
                Type.getInternalName(CoverageEventBroker.class).equals(((MethodInsnNode) in).owner);
    }

    static Stream<ClassNode> arguments() {
        return TestUtil.classes();
    }
}