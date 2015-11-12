package com.kendanware.jme3.taskloader;

import com.jme3.app.Application;

/**
 * A loading task is meant to represent a small unit of work that needs doing during the loading process.
 *
 * @author Daniel Johansson
 * @since 2015-01-17
 */
@FunctionalInterface
public interface LoadingTask<T extends Application> {

    /**
     * This method will be called by the {@link LoaderThread} and will potentially be executed in parallel to another
     * LoadingTask. This method will be provided with the application instance in order to provide an api access point
     * to JME.
     *
     * @param t the application instance provided to the {@link LoadingManager}.
     */
    void load(final T t);

}
