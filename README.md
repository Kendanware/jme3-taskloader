jMonkeyEngine Task Loader [![Build Status](https://travis-ci.org/Kendanware/jme3-taskloader.svg?branch=master)](https://travis-ci.org/Kendanware/jme3-taskloader)
==========================

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

# Maven

You will need to configure a repository in your pom.xml first.

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
