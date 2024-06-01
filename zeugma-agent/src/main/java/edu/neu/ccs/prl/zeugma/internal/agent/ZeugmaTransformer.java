package edu.neu.ccs.prl.zeugma.internal.agent;

import edu.neu.ccs.prl.zeugma.internal.runtime.event.LoadEventBroker;
import org.jacoco.core.internal.data.CRC64;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public final class ZeugmaTransformer implements ClassFileTransformer {
    private static final String ANNOTATION_DESC = Type.getDescriptor(ZeugmaInstrumented.class);
    private static final int api = ZeugmaAgent.ASM_VERSION;

    private static final ExclusionList instrumentationExclusions = new ExclusionList("zeugma.exclusions",
            // JVM uses hard-coded offsets into constant pool
            "java/lang/Object",
            "java/lang/Boolean",
            "java/lang/Character",
            "java/lang/Byte",
            "java/lang/Short",
            "java/lang/Number",
            "java/lang/ref/Reference",
            "java/lang/ref/FinalReference",
            "java/lang/ref/SoftReference",
            "jdk/internal/misc/UnsafeConstants",
            // Skip dynamic language support classes
            "java/lang/invoke/",
            "jdk/internal/reflect/",
            "sun/reflect/",
            // Skip internal zeugma classes
            ZeugmaAgent.INTERNAL_PACKAGE_PREFIX);

    private static final ExclusionList classLoadExclusions = new ExclusionList("zeugma.exclusions");

    private byte[] instrument(ClassReader cr, byte[] classFileBuffer) {
        ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.EXPAND_FRAMES);
        if (isAnnotated(cn)) {
            // This class has already been instrumented; return null to indicate that the class was unchanged
            return null;
        }
        // Add an annotation indicating that the class has been instrumented
        cn.visitAnnotation(ANNOTATION_DESC, false);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        if (ThreadAccessorClassVisitor.isApplicable(cn.name)) {
            cn.accept(new ThreadAccessorClassVisitor(api, cw));
        } else {
            ClassVisitor cv = applyBaseEventInstrumentation(cw, cn, CRC64.classId(classFileBuffer));
            if (ThreadClassVisitor.isApplicable(cn.name)) {
                cv = new ThreadClassVisitor(api, cv);
            }
            cn.accept(cv);
        }
        return cw.toByteArray();
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        if (shouldDynamicallyInstrument(className, classBeingRedefined)) {
            try {
                return transform(classFileBuffer);
            } catch (Throwable t) {
                // Print the stack trace for the error to prevent it from being silently swallowed by the JVM
                t.printStackTrace();
                throw t;
            }
        }
        return null;
    }

    public byte[] transform(byte[] classFileBuffer) {
        ClassReader cr = new ClassReader(classFileBuffer);
        if (!classLoadExclusions.isExcluded(cr.getClassName())) {
            LoadEventBroker.classLoaded();
        }
        if (shouldInstrumentClass(cr.getClassName())) {
            try {
                return instrument(cr, classFileBuffer);
            } catch (ClassTooLargeException | MethodTooLargeException e) {
                return null;
            }
        }
        return null;
    }

    private static ClassVisitor applyBaseEventInstrumentation(ClassVisitor cv, ClassNode cn, long checksum) {
        InitializationMethodBuilder initBuilder = new InitializationMethodBuilder(api, checksum);
        cn.accept(initBuilder);
        if (initBuilder.hasModel()) {
            cn.methods.add(initBuilder.getInit());
            cv = new InitializingClassVisitor(api, cv);
            cv = new CoverageEventClassVisitor(api, cv);
            cv = new ContextEventClassVisitor(api, cv);
        }
        return cv;
    }

    private static boolean isAnnotated(ClassNode cn) {
        if (cn.invisibleAnnotations != null) {
            for (AnnotationNode a : cn.invisibleAnnotations) {
                if (ANNOTATION_DESC.equals(a.desc)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean shouldDynamicallyInstrument(String className, Class<?> classBeingRedefined) {
        return classBeingRedefined == null // Class is being loaded and not redefined or retransformed
                // Class is not a dynamically generated accessor for reflection
                && (className == null || !ExclusionList.startsWith(className, "sun") ||
                ExclusionList.startsWith(className, "sun/nio"));
    }

    private static boolean shouldInstrumentClass(String className) {
        if (ThreadAccessorClassVisitor.isApplicable(className)) {
            return true;
        }
        return !className.equals("java/lang/reflect/Proxy") && !className.contains("$$Lambda$") // Ignore lambda classes
                && !instrumentationExclusions.isExcluded(className);
    }

    public static boolean shouldInstrumentMethod(int access, String name) {
        // Method is concrete, non-native, not a class initialization method, and not the model initialization
        // method
        return (access & Opcodes.ACC_ABSTRACT) == 0 && (access & Opcodes.ACC_NATIVE) == 0 && !"<clinit>".equals(name) &&
                !ZeugmaAgent.INIT_NAME.equals(name);
    }
}
