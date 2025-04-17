package edu.neu.ccs.prl.zeugma.internal.agent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.tree.ClassNode;

import java.util.Arrays;
import java.util.stream.Stream;

class MethodRecordTest {
    @ParameterizedTest
    @MethodSource("records")
    void isMethodForRecord(MethodRecord record) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(record.getOwner().replace("/", "."));
        ClassNode cn = TestUtil.getClassNode(clazz);
        Assertions.assertTrue(cn.methods.stream().anyMatch(mn -> record.matches(cn.name, mn.name, mn.desc)),
                "Missing method for " + record);
    }

    private static Stream<MethodRecord> records() {
        return Arrays.stream(MethodRecord.values());
    }
}