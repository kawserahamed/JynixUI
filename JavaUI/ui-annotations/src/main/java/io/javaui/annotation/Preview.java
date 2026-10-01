package io.javaui.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a parameterless UIComponent factory method to be rendered in Android Studio's
 * design layoutlib preview without launching on device or emulator.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Preview {
    String name() default "";
    int widthDp() default 360;
    int heightDp() default 640;
    boolean darkTheme() default false;
}
