package edu.neu.ccs.prl.zeugma.examples;

import edu.neu.ccs.prl.zeugma.internal.event.ContextChecker;

@SuppressWarnings("InstantiationOfUtilityClass")
public class ContextExamples {
    public ContextExamples(ContextChecker checker, @SuppressWarnings("unused") int i) {
        // Standard return
        checker.check();
    }

    public ContextExamples(ContextChecker checker, @SuppressWarnings("unused") long l) {
        // Exceptional return
        checker.check();
        try {
            throw new RuntimeException();
        } finally {
            checker.check();
        }
    }

    public ContextExamples(ContextChecker checker, @SuppressWarnings("unused") float f) {
        // Exceptional return from method
        checker.check();
        try {
            exceptionalReturn(checker);
        } finally {
            checker.check();
        }
    }

    public static void outerStandardReturn(ContextChecker checker) {
        checker.check();
        try {
            standardReturn(checker);
        } finally {
            checker.check();
        }
    }

    public static void outerExceptionalReturn(ContextChecker checker) {
        checker.check();
        try {
            exceptionalReturn(checker);
        } catch (Throwable t) {
            //
        } finally {
            checker.check();
        }
    }

    public static void outerInitStandardReturn(ContextChecker checker) {
        checker.check();
        try {
            new ContextExamples(checker, 7);
        } finally {
            checker.check();
        }
    }

    public static void outerInitExceptionalReturn(ContextChecker checker) {
        checker.check();
        try {
            new ContextExamples(checker, 7L);
        } catch (Throwable t) {
            //
        } finally {
            checker.check();
        }
    }

    public static void outerInitExceptionalReturnFromMethod(ContextChecker checker) {
        checker.check();
        try {
            new ContextExamples(checker, 7F);
        } catch (Throwable t) {
            //
        } finally {
            checker.check();
        }
    }

    private static void standardReturn(ContextChecker checker) {
        checker.check();
    }

    private static void exceptionalReturn(ContextChecker checker) {
        checker.check();
        throw new RuntimeException();
    }
}
