package com.kendanware.jme3.taskloader;

import com.jme3.app.Application;
import com.kendanware.jme3.taskloader.annotation.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * @author Daniel Johansson
 * @since 2015-03-31
 */
@RunWith(MockitoJUnitRunner.class)
public class LoadingManagerTest {

    private LoadingManager loadingManager;

    @Mock
    private Application application;

    @Mock
    private ProgressCallback progressCallback;

    @Before
    public void before() {
        loadingManager = new LoadingManager(application, progressCallback);
    }

    @Test
    public void accept_shouldAddLoadingTaskWithoutAnnotationToLoadedTasksListByClassName() {
        loadingManager.accept(new LoadingTaskWithoutAnnotation());
        assertThat(loadingManager.hasBeenLoaded(LoadingTaskWithoutAnnotation.class)).isTrue();
    }

    @Test
    public void accept_shouldAddLoadingTaskWithAnnotationToLoadedTasksListByAnnotationIdVariable() {
        loadingManager.accept(new LoadingTaskWithAnnotation());
        assertThat(loadingManager.hasBeenLoaded(LoadingTaskWithAnnotation.class)).isTrue();
    }

    @Test
    public void accept_shouldCallProgressCallbackWithCorrectMessage() {
        loadingManager.accept(new LoadingTaskWithAnnotation());

        verify(progressCallback).progress("Annotated Task", false, 0.0f);
    }

    @Test
    public void start_shouldSetLoadingCompleteAndProgressTo1_whenThereAreNoTasksInQueue() {
        loadingManager.start();

        assertThat(loadingManager.isLoadingComplete()).isTrue();
        assertThat(loadingManager.getProgress()).isEqualTo(1.0f);
        assertThat(loadingManager.getProgressPercentage()).isEqualTo(100.0f);
    }

    private static class LoadingTaskWithoutAnnotation implements LoadingTask {

        @Override
        public void load(Application application) {

        }
    }

    @Description("Annotated Task")
    private static class LoadingTaskWithAnnotation implements LoadingTask {

        @Override
        public void load(Application application) {

        }
    }
}
