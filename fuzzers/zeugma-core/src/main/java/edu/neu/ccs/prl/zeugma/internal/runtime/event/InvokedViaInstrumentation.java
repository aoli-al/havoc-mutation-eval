package edu.neu.ccs.prl.zeugma.internal.runtime.event;

import java.lang.annotation.*;

/**
 * Indicates that calls to a method can be added during the instrumentation of classes.
 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface InvokedViaInstrumentation {
    CoreMethodInfo info();
}