package edu.neu.ccs.prl.zeugma.internal.agent;

import edu.neu.ccs.prl.zeugma.internal.runtime.model.ClassModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.util.stream.Stream;

class InitializationMethodBuilderTest {
    @ParameterizedTest
    @MethodSource("arguments")
    void test(ClassNode cn) throws ReflectiveOperationException {
        InitializationMethodBuilder builder = new InitializationMethodBuilder(ZeugmaAgent.ASM_VERSION, 77L);
        cn.accept(builder);
        ClassModel model = buildModel(builder.getInit());
        Assertions.assertNotNull(model);
    }

    static ClassModel buildModel(MethodNode init) throws ReflectiveOperationException {
        Class<?> clazz = new ByteArrayClassLoader().createClass(init);
        Method m = clazz.getDeclaredMethod(ZeugmaAgent.INIT_NAME);
        m.setAccessible(true);
        return (ClassModel) m.invoke(null);
    }

    static Stream<ClassNode> arguments() {
        return TestUtil.classes();
    }

    static class ByteArrayClassLoader extends ClassLoader {
        public Class<?> createClass(MethodNode init) {
            ClassNode cn = TestUtil.createClassNode(init);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            cn.accept(cw);
            byte[] bytes = cw.toByteArray();
            return defineClass(null, bytes, 0, bytes.length);
        }
    }
}