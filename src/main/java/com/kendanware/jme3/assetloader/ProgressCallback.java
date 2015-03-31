package com.kendanware.jme3.assetloader;

/**
 * @author Daniel Johansson
 * @since 2015-02-13
 */
@FunctionalInterface
public interface ProgressCallback {

    void progress(final String message, final float progress);
}
