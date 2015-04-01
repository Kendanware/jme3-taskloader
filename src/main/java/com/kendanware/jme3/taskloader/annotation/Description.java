package com.kendanware.jme3.taskloader.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for declaring a message to use for a loading task. This message will be sent in {@link com.kendanware.jme3.taskloader.ProgressCallback#progress(String, boolean, float)}
 * which you provide to the {@link com.kendanware.jme3.taskloader.LoadingManager}.
 *
 * @author Daniel Johansson
 * @since 2015-03-31
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Description {

    String value();
}
