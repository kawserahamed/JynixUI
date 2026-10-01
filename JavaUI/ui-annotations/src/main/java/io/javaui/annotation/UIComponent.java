package io.javaui.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a declarative UI component in JavaUI.
 *
 * Requirements:
 * - Must be static or pure instance method without hidden global state.
 * - Accepts a {@code Scope} as its first parameter.
 * - Returns a {@code UI} tree description.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface UIComponent {
    /**
     * Unique stable name for debugging, tooling, and profiling.
     */
    String name() default "";
}
