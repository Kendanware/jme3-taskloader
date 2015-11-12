package com.kendanware.jme3.taskloader;

/**
 * A callback interface will be notified when a task has been loaded with the message containing the description if provided,
 * otherwise an empty string, for the task completed as well as the total loading progress and loadingComplete as true if
 * all tasks have been completed.
 *
 * @author Daniel Johansson
 * @since 2015-02-13
 */
@FunctionalInterface
public interface ProgressCallback {

    /**
     * Will be called once a task completes with updated information regarding the {@link LoadingManager}'s state. A float
     * value is provided to give a percentage of how much has been loaded, do not use this value to determine if loading
     * is complete. Use the the loadingCompleted argument for that instead due to precision issues with float.
     *
     * @param message an optional message provided by the loading task which was just processed.
     * @param loadingCompleted true if all tasks have now finished loading.
     * @param progress The overall progress as a float value between 0.0 (not loaded) and 1.0 (100% loaded).
     */
    void progress(final String message, final boolean loadingCompleted, final float progress);
}
