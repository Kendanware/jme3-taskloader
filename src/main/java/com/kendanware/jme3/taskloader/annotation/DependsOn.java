package com.kendanware.jme3.taskloader.annotation;

import com.kendanware.jme3.taskloader.LoadingTask;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for declaring more information about a {@link com.kendanware.jme3.taskloader.LoadingTask}. This is mainly
 * used for when a task should be dependent on another task.
 * <p/>
 * Using the following simple example you can see that SecondLoadingTask is dependent on FirstLoadingTask so even if
 * SecondLoadingTask is picked up first by the loading system, it will be put back on the queue until FirstLoadingTask
 * has been registered as complete.
 * <pre>
 *     public class FirstLoadingTask implements LoadingTask {
 *         // Implementation left out.
 *     }
 *
 *     {@literal @}DependsOn(FirstLoadingTask.class)
 *     public class SecondLoadingTask implements LoadingTask {
 *         // Implementation left out.
 *     }
 * </pre>
 *
 * @author Daniel Johansson
 * @since 2015-03-31
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DependsOn {

    Class<? extends LoadingTask>[] value() default {};
}
