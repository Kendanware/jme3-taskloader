package com.kendanware.jme3.taskloader;

/**
 * A callback interface will be notified when a task has been loaded with the message containing the description (if provided)
 * for the task completed as well as the total loading progress and loadingComplete as true if all tasks have been completed.
 *
 * @author Daniel Johansson
 * @since 2015-02-13
 */
@FunctionalInterface
public interface ProgressCallback {

    void progress(final String message, final boolean loadingCompleted, final float progress);
}
