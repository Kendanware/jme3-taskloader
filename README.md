jMonkeyEngine Task Loader [![Build Status](https://travis-ci.org/Kendanware/jme3-taskloader.svg?branch=master)](https://travis-ci.org/Kendanware/jme3-taskloader)
==========================

__Requires JDK 1.8 or newer__

The task loader provides a mechanism for performing tasks and hooking up a callback to potentially render a loading screen.

- - -

# Simple Usage

Lets create a loading app state which will be in control of the tasks that need to be performed before the game can proceed
to the next state.

```java
// Hint: This class could also implement something like a nifty gui ScreenController and provide access to GUI elements.
public class LoadingAppState extends AbstractAppState implements ProgressCallback {

    private LoadingManager loadingManager;
    private AppStateManager stateManager;
    private final AppState nextApplicationState;

    public LoadingAppState(final AppState nextApplicationState) {
        this.nextApplicationState = nextApplicationState;
    }

    @Override
    public void initialize(final AppStateManager stateManager, final Application application) {
        super.initialize(stateManager, application);
        this.stateManager = stateManager;

        // Create a LoadingManager and register tasks with it.
        loadingManager = new LoadingManager(application, this);

        // Hint: It's nicer to extract loading tasks into separate class files instead of using lambdas for cleaner, more organized code.

        loadingManager.registerForLoading(application1 -> {
            // Load the terrain data
        });

        loadingManager.registerForLoading(application1 -> {
            // Load weather app state
        });

        loadingManager.registerForLoading(application1 -> {
            // Create input mappings.
        });

        loadingManager.registerForLoading(application1 -> {
            // Set up camera position etc.
        });

        // Tell the LoadingManager to start loading tasks.
        loadingManager.start();
    }

    @Override
    public void progress(final String message, final boolean loadingCompleted, final float progress) {
        // Some gui element which can take the progress value.
        progressBar.setProgress(progress);

        if (loadingCompleted) {
            stateManager.detach(this);
            stateManager.attach(nextApplicationState);
            return;
        }
    }
}
```

- - -

# LoadingManager

This is the heart of the loading process. It takes care of creating the worker threads and is where you will register the
tasks you want performed and it will let you know when tasks complete. The constructor of LoadingManager requires that you
provide it with a reference to the JME application and a progress callback. Optionally you can also provide a value for
the number of threads you would like it to use for running your loading tasks. By default this will be set to the available
amount of processors as determined by the Java runtime so if you have a quad core CPU it's likely to create 4 threads.

- - -

# Loading Tasks

A loading task is defined as a small unit of isolated work that needs to be performed. This means that usually your tasks
will be really short in lines of code. Perhaps it will load an asset like a model and setup a couple of extra attributes
on it. Perhaps the task will load your game world. Perhaps it will initialise your post-process filters. The list goes on.

Remember that the task loading is fully asynchronous, this means that loading tasks will executed on their own thread. Even
if you decide to only use one thread for loading the tasks they are still executed asynchronously to the main rendering
thread in JME. In order to talk nicely to the rendering thread you will have to ensure that any code as described by
the JME threading model document is executed through a Callable on the rendering thread. See
[jMonkeyEngine Wiki - Multi Threading](http://wiki.jmonkeyengine.org/doku.php/jme3:advanced:multithreading) for more information.

Below is a simple task which loads a model and sets it's local translation to 10, 0, 10 and then uses the application.enqueue
method to attach it to it's parent node. How this task is provided the parentNode is up to you of course, feel free to have
a play.

```java
public class MyLoadingTask implements LoadingTask {

    private final Node parentNode;

    public MyLoadingTask(final Node parentNode) {
        this.parentNode = parentNode;
    }

    @Override
    public void load(final Application application) {
        final Spatial model = application.getAssetManager().loadModel("Models/some-model.j3o");
        model.setLocalTranslation(10, 0, 10);

        application.enqueue(() -> {
            parentNode.attachChild(model);
            return null;
        });
    }
}
```

## Loading Task Dependencies (@DependsOn)

There might be situations where you are trying to load something and you realise it depends on something else which is also
in the loading pipeline. This is where the @DependsOn annotation comes in. Simply add it to the task which needs to wait on
another task and tell it which task(s) it should wait for.

Bare in mind that it will only wait for the other task(s) to complete not the application.enqueue calls that might have been
issued during their execution.

```java
@DependsOn(MyLoadingTask.class)
public DependentLoadingTask implements LoadingTask {

    @Override
    public void load(final Application application) {
        // Implementation omitted, you have to come up with this part.
    }
}
```

A LoadingTask can depend on __multiple__ tasks but be careful not to create a situation where there is a chain which can
never be fulfilled.

- - - 

# Maven

This should speak for itself.

```xml
    <repository>
        <id>oss-libs-release</id>
        <url>http://oss.jfrog.org/artifactory/libs-release/</url>
    </repository>

    <repository>
        <id>oss-libs-snapshots</id>
        <url>http://oss.jfrog.org/artifactory/libs-snapshot/</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>

    <dependency>
        <groupId>com.kendanware.jme3</groupId>
        <artifactId>taskloader</artifactId>
        <version>${jme3taskloader.version}</version>
    </dependency>
```
