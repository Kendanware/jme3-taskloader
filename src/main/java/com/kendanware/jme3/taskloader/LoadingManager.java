package com.kendanware.jme3.taskloader;

import com.google.common.util.concurrent.AtomicDouble;
import com.jme3.app.Application;
import com.kendanware.jme3.taskloader.annotation.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * The LoadingManager is the brain of the async asset loading system. It will coordinate tasks for loading and create
 * the threads to consume the tasks. It will report any progress back to your code using a {@link com.kendanware.jme3.taskloader.ProgressCallback}.
 *
 * @author Daniel Johansson
 * @since 2015-01-17
 */
public class LoadingManager<T extends Application> implements Consumer<LoadingTask> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadingManager.class);

    private final T application;
    private final Queue<LoadingTask> loadingTasks = new ConcurrentLinkedQueue<>();
    private final int threads;
    private boolean loadingStarted = false;
    private boolean loadingComplete = false;
    private long loadingStartedAt;
    private final ProgressCallback progressCallback;
    private float progress;
    private Queue<Class<? extends LoadingTask>> loadedTasks = new ConcurrentLinkedQueue<>();
    private final AtomicDouble totalProgress = new AtomicDouble();
    private double progressPerAsset;
    private int tasksToLoad;

    /**
     * The LoadingManager is responsible for coordinating the loading work, starting threads and reporting back when loading
     * completes. This constructor will default the number threads to use to {@link Runtime#availableProcessors()}.
     *
     * @param application      the JME3 application class.
     * @param progressCallback a callback to call when loading progress changes.
     */
    public LoadingManager(final T application, final ProgressCallback progressCallback) {
        this(application, Runtime.getRuntime().availableProcessors(), progressCallback);
    }

    /**
     * The LoadingManager is responsible for coordinating the loading work, starting threads and reporting back when loading
     * completes.
     *
     * @param application      the JME3 application class.
     * @param numberOfThreads  number of threads to use for consuming the task queue.
     * @param progressCallback a callback to call when loading progress changes.
     */
    public LoadingManager(final T application, final int numberOfThreads, final ProgressCallback progressCallback) {
        if (progressCallback == null) {
            throw new IllegalArgumentException("progressCallback is required");
        }

        this.application = application;
        this.progressCallback = progressCallback;
        this.threads = numberOfThreads;
    }

    /**
     * Registers a task for loading, it will be added to a queue and consumed by a {@link com.kendanware.jme3.taskloader.LoaderThread}.
     *
     * @param loadingTask the task to register for loading. Hint: Can be a lambda as {@link com.kendanware.jme3.taskloader.LoadingTask} is a functional interface.
     */
    public void registerForLoading(final LoadingTask loadingTask) {
        loadingTasks.add(loadingTask);
    }

    /**
     * Checks if all provided tasks have been loaded. This is used for determining task dependency. In order to use this
     * in a meaningful way a {@link com.kendanware.jme3.taskloader.LoadingTask} need to be annotated with {@link com.kendanware.jme3.taskloader.annotation.DependsOn}.
     *
     * @param dependencies an array of string identifiers for dependencies to check against. These are case sensitive.
     * @return true if all the provided dependencies have been loaded, otherwise false.
     * @see com.kendanware.jme3.taskloader.annotation.DependsOn
     */
    public boolean hasBeenLoaded(final Class<? extends LoadingTask>... dependencies) {
        for (final Class<? extends LoadingTask> loadingTaskClass : dependencies) {
            if (!loadedTasks.contains(loadingTaskClass)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Takes the next task to be loaded out of the queue and returns it. This is mainly used for {@link com.kendanware.jme3.taskloader.LoadingTask}
     * to be able to poll for new tasks but can easily be used for implementing your own loading tasks.
     *
     * @return the next task in the queue to be loaded or null if there are no more tasks.
     */
    public LoadingTask getNextTask() {
        return loadingTasks.poll();
    }

    /**
     * Tell the loader to start loading assets that have been registered using {@link #registerForLoading(LoadingTask)}.
     * <p>
     * If this is called multiple times subsequent calls will be ignored and a warning logged.
     * </p>
     * <p>
     * If this is called without any tasks having been registered a warning will be logged and loading will immediately be
     * set to complete.
     * </p>
     */
    public void start() {
        if (loadingComplete) {
            LOGGER.warn("Asked to load assets but loading has already been done, are you calling start() twice?");
            return;
        }

        if (loadingTasks.isEmpty()) {
            LOGGER.warn("Asked to load assets but none have been registered for loading, have you called registerForLoading() with a task?");
            loadingComplete = true;
            progress = 1.0f;
            return;
        }

        loadingStarted = true;
        loadingStartedAt = System.nanoTime();

        tasksToLoad = loadingTasks.size();
        progressPerAsset = 1.0f / tasksToLoad;

        // Fire up threads to load stuff
        for (int i = 0; i < threads; i++) {
            final LoaderThread loaderThread = new LoaderThread(this, this);
            LOGGER.debug("Created LoaderThread {} of {}", i + 1, threads);

            final Thread thread = new Thread(loaderThread, "LoaderThread " + i);
            thread.start();
        }
    }

    public T getApplication() {
        return application;
    }

    /**
     * Returns the current progress as a value between 0.0 and 1.0 where 1.0 is max progress.
     *
     * @return a value between 0.0 and 1.0
     */
    public float getProgress() {
        return progress;
    }

    /**
     * Returns the progress in percent from 0.0 to 100.0
     *
     * @return a value between 0.0 and 100.0
     */
    public float getProgressPercentage() {
        return progress * 100;
    }

    /**
     * Returns the time in nano seconds since start() was called, will return 0 if start() has not yet been called.
     * To check if loading has started you should use {@link #isLoadingStarted()}.
     *
     * @return time in nano seconds since start() was called.
     */
    public long getLoadingStartedAt() {
        return loadingStartedAt;
    }

    /**
     * Returns true if loading has started.
     *
     * @return true if loading has started, otherwise false.
     */
    public boolean isLoadingStarted() {
        return loadingStarted;
    }

    /**
     * Returns true if loading has completed. This can only be true if {@link #isLoadingStarted()} is also true.
     *
     * @return true if loading has completed, otherwise false.
     */
    public boolean isLoadingComplete() {
        return loadingComplete;
    }

    @Override
    public void accept(final LoadingTask loadingTask) {
        // Add this loading task to the list of loaded tasks.
        loadedTasks.add(loadingTask.getClass());
        LOGGER.debug("Added task {} to list of loaded tasks", loadingTask.getClass().getSimpleName());

        LOGGER.debug("Loaded tasks: {}, tasks to load: {}", loadedTasks.size(), tasksToLoad);

        if (loadingTasks.isEmpty() && loadedTasks.size() == tasksToLoad && !loadingComplete) {
            loadingComplete = true;
            LOGGER.debug("Completed loading {} tasks in {} ms using {} threads", loadedTasks.size(), TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - loadingStartedAt), threads);
        }

        String message = "";
        final Description description = loadingTask.getClass().getAnnotation(Description.class);

        if (description != null) {
            message = description.value();
        }

        progress = (float) totalProgress.addAndGet(progressPerAsset);
        progressCallback.progress(message, loadingComplete, progress);
    }
}
