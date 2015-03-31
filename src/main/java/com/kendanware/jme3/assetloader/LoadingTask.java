package com.kendanware.jme3.assetloader;

import com.jme3.app.Application;

/**
 * @author Daniel Johansson
 * @since 2015-01-17
 */
@FunctionalInterface
public interface LoadingTask {

    void load(final Application application);

}
