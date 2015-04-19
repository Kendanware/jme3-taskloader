package com.kendanware.jme3.taskloader;

import com.kendanware.jme3.taskloader.annotation.DependsOn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * @author Daniel Johansson
 * @since 2015-01-17
 */
public class LoaderThread implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoaderThread.class);
    private final LoadingManager loadingManager;
    private final Consumer<LoadingTask> loadingTaskCompletedCallback;

    public LoaderThread(final LoadingManager loadingManager, final Consumer<LoadingTask> loadingTaskCompletedCallback) {
        this.loadingManager = loadingManager;
        this.loadingTaskCompletedCallback = loadingTaskCompletedCallback;
    }

    @Override
    public void run() {
        LoadingTask loadingTask;

        while ((loadingTask = loadingManager.getNextTask()) != null) {
            final DependsOn dependsOn = loadingTask.getClass().getAnnotation(DependsOn.class);

            // If this loading task has an annotation and indicates it depends on something, lets put the task back
            // on the queue and try another task instead.
            if (dependsOn != null && !loadingManager.hasBeenLoaded(dependsOn.value())) {
                loadingManager.registerForLoading(loadingTask);
                continue;
            }

            try {
                loadingTask.load(loadingManager.getApplication());
            } catch (Exception e) {
                LOGGER.error("Exception caught during loading", e);
                loadingManager.getApplication().stop();
            } finally {
                loadingTaskCompletedCallback.accept(loadingTask);
            }
        }
    }
}
