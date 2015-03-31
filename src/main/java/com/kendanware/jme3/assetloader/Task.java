package com.kendanware.jme3.assetloader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Daniel Johansson
 * @since 2015-03-31
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Task {

    String id();
    String[] dependsOn() default {};
    String description() default "";
}
