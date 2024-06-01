package edu.neu.ccs.prl.zeugma.internal.parametric;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.neu.ccs.prl.zeugma.internal.guidance.GuidanceBuilder;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JqfRunner extends BlockJUnit4ClassRunner {
    private final GuidanceBuilder builder;
    private final FrameworkMethod method;
    private final List<Class<?>> expectedExceptions;
    private final String descriptor;
    private Object[] group;
    private Throwable error;

    public JqfRunner(Class<?> testClass, String testMethodName, GuidanceBuilder builder) throws InitializationError {
        super(testClass);
        if (testClass == null || testMethodName == null || builder == null) {
            throw new NullPointerException();
        } else if (!testClass.isAnnotationPresent(RunWith.class) ||
                !JQF.class.equals(testClass.getAnnotation(RunWith.class).value())) {
            throw new IllegalArgumentException(testClass + " is not a JQF test");
        }
        this.descriptor = testClass.getName() + "#" + testMethodName;
        this.builder = builder;
        this.method = new TestClass(testClass).getAnnotatedMethods(Fuzz.class)
                .stream()
                .filter(m -> m.getName().equals(testMethodName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unable to find test method"));
        this.expectedExceptions = Arrays.asList(method.getMethod().getExceptionTypes());
    }

    @Override
    protected Statement childrenInvoker(RunNotifier notifier) {
        ParametricFuzzTarget target =
                new ParametricFuzzTarget(this, notifier, descriptor, new StructuredInputGenerator(method.getMethod()));
        return new Statement() {
            @Override
            public void evaluate() {
                try {
                    builder.build(target).fuzz();
                } catch (Throwable t) {
                    JqfRunner.this.error = t;
                }
            }
        };
    }

    public void run() {
        run(new RunNotifier());
    }

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
        if (error instanceof RuntimeException) {
            throw (RuntimeException) error;
        } else if (error != null) {
            // Wrap checked exceptions
            throw new RuntimeException(error);
        }
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        return Collections.singletonList(method);
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    method.invokeExplosively(test, group);
                } catch (Throwable e) {
                    if (!isExpectedException(e.getClass())) {
                        throw e;
                    }
                }
            }
        };
    }

    void execute(RunNotifier notifier, Object[] group) {
        this.group = group;
        Statement statement = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                methodBlock(method).evaluate();
            }
        };
        runLeaf(statement, describeChild(method), notifier);
    }

    private boolean isExpectedException(Class<? extends Throwable> e) {
        for (Class<?> expectedException : expectedExceptions) {
            if (expectedException.isAssignableFrom(e)) {
                return true;
            }
        }
        return false;
    }

    public static JqfRunner createInstance(String testClassName, String testMethodName, ClassLoader classLoader,
                                           GuidanceBuilder builder) throws ClassNotFoundException, InitializationError {
        Class<?> testClass = Class.forName(testClassName, true, classLoader);
        return new JqfRunner(testClass, testMethodName, builder);
    }
}
