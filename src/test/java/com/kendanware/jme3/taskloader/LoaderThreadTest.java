package com.kendanware.jme3.taskloader;

import com.jme3.app.Application;
import com.jme3.asset.AssetNotFoundException;
import com.kendanware.jme3.taskloader.annotation.DependsOn;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.function.Consumer;

import static org.mockito.Mockito.*;

/**
 * @author Daniel Johansson
 * @since 2015-03-31
 */
@RunWith(MockitoJUnitRunner.class)
public class LoaderThreadTest {

    private LoaderThread loaderThread;

    @Mock
    private LoadingManager loadingManager;

    @Mock
    private Consumer<LoadingTask> loadingTaskConsumer;

    @Mock
    private LoadingTask loadingTask;

    @Mock
    private Application application;

    @Before
    public void before() {
        loaderThread = new LoaderThread(loadingManager, loadingTaskConsumer);
        when(loadingManager.getApplication()).thenReturn(application);
    }

    @Test
    public void run_shouldCallLoadingTaskLoadForEachTask() {
        when(loadingManager.getNextTask()).thenReturn(loadingTask).thenReturn(loadingTask).thenReturn(null);

        loaderThread.run();

        verify(loadingManager, never()).registerForLoading(loadingTask);
        verify(loadingManager, times(3)).getNextTask();
        verify(loadingTask, times(2)).load(application);
    }

    @Test
    public void run_shouldCallLoadingManagerRegisterForLoading_whenTaskDependsOnAnotherTask() {
        final LoadingTaskWithDependency loadingTaskWithDependency = new LoadingTaskWithDependency();
        final LoadingTaskWithoutDependency loadingTaskWithoutDependency = new LoadingTaskWithoutDependency();

        when(loadingManager.getNextTask()).thenReturn(loadingTaskWithDependency).thenReturn(loadingTaskWithoutDependency).thenReturn(null);

        loaderThread.run();

        verify(loadingManager).registerForLoading(loadingTaskWithDependency);
        verify(loadingManager, never()).registerForLoading(loadingTaskWithoutDependency);
        verify(loadingManager, times(3)).getNextTask();
    }

    @Test
    public void run_shouldCallApplicationStop_whenExceptionOccursDuringLoadingTaskLoad() {
        when(loadingManager.getNextTask()).thenReturn(loadingTask).thenReturn(null);
        doThrow(new AssetNotFoundException("")).when(loadingTask).load(application);
        loaderThread.run();
        verify(application).stop();
    }

    private static class LoadingTaskWithoutDependency implements LoadingTask {

        @Override
        public void load(Application application) {

        }
    }

    @DependsOn(LoadingTaskWithoutDependency.class)
    private static class LoadingTaskWithDependency implements LoadingTask {

        @Override
        public void load(Application application) {

        }
    }
}
