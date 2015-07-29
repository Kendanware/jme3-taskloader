package com.kendanware.jme3.taskloader;

import com.jme3.app.Application;

/**
 * @author Daniel Johansson
 * @since 2015-01-17
 */
@FunctionalInterface
public interface LoadingTask<T extends Application> {

    void load(final T t);

}
