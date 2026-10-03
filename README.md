# Historical Game (2x12)

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project is a historical game developed using the libGDX framework. It features a comprehensive modular architecture for various game components.

## Content Description

The codebase is organized into modular packages under the core module (`core/src/main/java/bd/historicalgame/`) to handle different aspects of the game:

- **`ai`**: Artificial Intelligence logic for NPCs and enemies.
- **`assets`**: Asset management for loading textures, fonts, and other resources.
- **`audio`**: Audio management for background music and sound effects.
- **`characters`**: Base classes and specific implementations for in-game characters.
- **`cinematics`**: Handles cutscenes and cinematic events.
- **`dialogue`**: System for managing branching dialogues and NPC interactions.
- **`game`**: Core game logic, state management, configuration, and main game loops.
- **`input`**: Input processing for player controls.
- **`inventory`**: Inventory system for managing items, weapons, and resources.
- **`missions`**: Quest and mission tracking systems.
- **`player`**: Player character specific logic, movement, and interactions.
- **`rendering`**: Custom rendering logic and graphics management.
- **`save`**: Serialization and management of game saves.
- **`ui`**: User Interface components, menus, and HUD.
- **`utils`**: Utility classes and helper functions.
- **`world`**: World generation, level management, and spatial logic.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.
