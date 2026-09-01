package bd.historicalgame;

import bd.historicalgame.game.GameManager;
import bd.historicalgame.game.GameState;
import bd.historicalgame.ui.UIManager;
import bd.historicalgame.world.World;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Main application entry point for 2x12.
 *
 * Gameplay/world logic remains in the existing World/Level1World classes.
 * UIManager owns the presentation layer and menu mouse interaction.
 */
public class Main extends ApplicationAdapter {

    private GameManager gameManager;

    private World world;

    private UIManager uiManager;

    private GameState displayedState;

    // =========================================================
    // MAIN MENU
    // =========================================================

    private int selectedOption = 0;

    private final String[] menuOptions = {
        "PLAY",
        "SETTINGS",
        "EXIT"
    };

    // =========================================================
    // PAUSE MENU
    // =========================================================

    private int pauseOption = 0;

    private final String[] pauseOptions = {
        "RESUME",
        "EXIT TO MAIN MENU",
        "EXIT GAME"
    };

    // =========================================================
    // EXIT CONFIRMATION
    // =========================================================

    private boolean exitConfirmation =
        false;

    private boolean exitToMainMenu =
        false;

    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public void create() {

        gameManager =
            new GameManager();

        world =
            null;

        uiManager =
            new UIManager();

        displayedState =
            null;

        syncUIState(true);

        System.out.println(
            "================================"
        );

        System.out.println(
            "              2x12"
        );

        System.out.println(
            "      HISTORICAL ADVENTURE"
        );

        System.out.println(
            "================================"
        );

        System.out.println(
            "State: " +
            gameManager.getCurrentState()
        );
    }

    // =========================================================
    // RENDER
    // =========================================================

    @Override
    public void render() {

        float delta =
            Math.min(
                Gdx.graphics.getDeltaTime(),
                0.05f
            );

        handleInput();

        consumeUIActions();

        GameState state =
            gameManager.getCurrentState();

        syncUIState(false);

        // -----------------------------------------------------
        // EXIT CONFIRMATION
        // -----------------------------------------------------

        if (
            exitConfirmation ||
            uiManager.isExitConfirmation()
        ) {

            renderCurrentBackground(
                state
            );

            uiManager.render(
                0.016f
            );

            return;
        }

        // -----------------------------------------------------
        // SETTINGS
        // -----------------------------------------------------

        if (
            uiManager.isSettingsOpen()
        ) {

            renderCurrentBackground(
                state
            );

            uiManager.render(
                delta
            );

            return;
        }

        // -----------------------------------------------------
        // GAME STATE
        // -----------------------------------------------------

        switch (state) {

            case MAIN_MENU:

                uiManager.renderBackdrop(
                    true
                );

                uiManager.render(
                    delta
                );

                break;

            case LEVEL1_INTRO:

                /*
                 * The already-created Level 1 world is shown
                 * behind the cinematic introduction panel.
                 */
                if (world != null) {

                    world.render();
                }

                uiManager.render(
                    delta
                );

                break;

            case PLAYING:

                if (world != null) {

                    world.update(
                        delta
                    );

                    world.render();
                }

                /*
                 * Reserved for future gameplay values.
                 * The existing Level1World HUD remains active.
                 */
                uiManager.renderGameplayOverlay(
                    delta
                );

                break;

            case PAUSED:

                /*
                 * Keep the 3D world visible behind the pause UI.
                 */
                if (world != null) {

                    world.render();
                }

                uiManager.render(
                    delta
                );

                break;

            default:

                uiManager.renderBackdrop(
                    true
                );

                uiManager.render(
                    delta
                );

                break;
        }
    }

    // =========================================================
    // INPUT
    // =========================================================

    private void handleInput() {

        // -----------------------------------------------------
        // EXIT CONFIRMATION
        // -----------------------------------------------------

        if (
            exitConfirmation ||
            uiManager.isExitConfirmation()
        ) {

            if (
                Gdx.input.isKeyJustPressed(
                    Input.Keys.ENTER
                )
            ) {

                confirmExit();

            } else if (
                Gdx.input.isKeyJustPressed(
                    Input.Keys.ESCAPE
                )
            ) {

                cancelExit();
            }

            return;
        }

        // -----------------------------------------------------
        // SETTINGS
        // -----------------------------------------------------

        if (
            uiManager.isSettingsOpen()
        ) {

            if (
                Gdx.input.isKeyJustPressed(
                    Input.Keys.ESCAPE
                )
            ) {

                closeSettings();
            }

            return;
        }

        GameState state =
            gameManager.getCurrentState();

        // -----------------------------------------------------
        // MAIN MENU
        // -----------------------------------------------------

        if (
            state ==
            GameState.MAIN_MENU
        ) {

            handleMainMenuInput();

        }

        // -----------------------------------------------------
        // LEVEL 1 INTRO
        // -----------------------------------------------------

        else if (
            state ==
            GameState.LEVEL1_INTRO
        ) {

            handleIntroInput();

        }

        // -----------------------------------------------------
        // PLAYING
        // -----------------------------------------------------

        else if (
            state ==
            GameState.PLAYING
        ) {

            handlePlayingInput();

        }

        // -----------------------------------------------------
        // PAUSED
        // -----------------------------------------------------

        else if (
            state ==
            GameState.PAUSED
        ) {

            handlePauseInput();
        }
    }

    // =========================================================
    // MAIN MENU INPUT
    // =========================================================

    private void handleMainMenuInput() {

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.UP
            )
        ) {

            selectedOption =
                (
                    selectedOption -
                    1 +
                    menuOptions.length
                ) %
                menuOptions.length;
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.DOWN
            )
        ) {

            selectedOption =
                (
                    selectedOption +
                    1
                ) %
                menuOptions.length;
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ENTER
            )
        ) {

            if (
                selectedOption ==
                0
            ) {

                startLevel1();

            } else if (
                selectedOption ==
                1
            ) {

                openSettings();

            } else {

                requestExit(
                    false
                );
            }
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            ) ||
            Gdx.input.isKeyJustPressed(
                Input.Keys.Q
            )
        ) {

            requestExit(
                false
            );
        }
    }

    // =========================================================
    // INTRO INPUT
    // =========================================================

    private void handleIntroInput() {

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ENTER
            )
        ) {

            gameManager.startPlaying();

            uiManager.clearOverlayFlags();

            System.out.println(
                "LEVEL 1 STARTED"
            );
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.Q
            ) ||
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {

            requestExit(
                false
            );
        }
    }

    // =========================================================
    // PLAYING INPUT
    // =========================================================

    private void handlePlayingInput() {

        /*
         * ESC opens the cinematic pause menu.
         */
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {

            pauseOption =
                0;

            gameManager.pauseGame();

            setCursorForUI();

            return;
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.Q
            )
        ) {

            requestExit(
                false
            );
        }
    }

    // =========================================================
    // PAUSE INPUT
    // =========================================================

    private void handlePauseInput() {

        /*
         * ESC resumes the game.
         */
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {

            gameManager.resumeGame();

            setCursorForGameplay();

            return;
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.UP
            )
        ) {

            pauseOption =
                (
                    pauseOption -
                    1 +
                    pauseOptions.length
                ) %
                pauseOptions.length;
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.DOWN
            )
        ) {

            pauseOption =
                (
                    pauseOption +
                    1
                ) %
                pauseOptions.length;
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ENTER
            )
        ) {

            if (
                pauseOption ==
                0
            ) {

                gameManager.resumeGame();

                setCursorForGameplay();

            } else if (
                pauseOption ==
                1
            ) {

                requestExit(
                    true
                );

            } else {

                requestExit(
                    false
                );
            }
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.Q
            )
        ) {

            requestExit(
                false
            );
        }
    }

    // =========================================================
    // UI ACTIONS
    // =========================================================

    private void consumeUIActions() {

        UIManager.Action action =
            uiManager.consumeAction();

        switch (action) {

            case START_LEVEL1:

                if (
                    gameManager.isMainMenu()
                ) {

                    startLevel1();
                }

                break;

            case START_PLAYING:

                if (
                    gameManager.isLevel1Intro()
                ) {

                    gameManager.startPlaying();

                    uiManager.clearOverlayFlags();
                }

                break;

            case RESUME:

                if (
                    gameManager.isPaused()
                ) {

                    gameManager.resumeGame();

                    setCursorForGameplay();
                }

                break;

            case EXIT_TO_MAIN_MENU:

                requestExit(
                    true
                );

                break;

            case EXIT_GAME:

                requestExit(
                    false
                );

                break;

            case SETTINGS:

                openSettings();

                break;

            case CLOSE_SETTINGS:

                closeSettings();

                break;

            case CONFIRM_EXIT:

                confirmExit();

                break;

            case CANCEL_EXIT:

                cancelExit();

                break;

            case NONE:

            default:

                break;
        }
    }

    // =========================================================
    // UI STATE SYNCHRONIZATION
    // =========================================================

    private void syncUIState(
        boolean force
    ) {

        GameState state =
            gameManager.getCurrentState();

        if (
            force ||
            state != displayedState
        ) {

            displayedState =
                state;

            uiManager.clearOverlayFlags();

            uiManager.showState(
                state
            );

            if (
                state ==
                GameState.PLAYING
            ) {

                setCursorForGameplay();

            } else {

                setCursorForUI();
            }
        }
    }

    // =========================================================
    // LEVEL 1
    // =========================================================

    private void startLevel1() {

        System.out.println(
            "Loading Level 1..."
        );

        if (world != null) {

            world.dispose();
        }

        /*
         * Preserve the existing World/Level1World implementation.
         */
        world =
            new World();

        gameManager.startLevel1();

        displayedState =
            null;

        syncUIState(
            true
        );
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    private void openSettings() {

        uiManager.showSettings();

        setCursorForUI();
    }

    private void closeSettings() {

        uiManager.clearOverlayFlags();

        uiManager.showState(
            gameManager.getCurrentState()
        );

        setCursorForUI();
    }

    // =========================================================
    // EXIT REQUEST
    // =========================================================

    private void requestExit(
        boolean toMainMenu
    ) {

        exitToMainMenu =
            toMainMenu;

        exitConfirmation =
            true;

        uiManager.showExitConfirmation(
            gameManager.isMainMenu()
        );

        setCursorForUI();
    }

    // =========================================================
    // CONFIRM EXIT
    // =========================================================

    private void confirmExit() {

        exitConfirmation =
            false;

        uiManager.clearOverlayFlags();

        // -----------------------------------------------------
        // EXIT TO MAIN MENU
        // -----------------------------------------------------

        if (exitToMainMenu) {

            if (world != null) {

                world.dispose();

                world =
                    null;
            }

            exitToMainMenu =
                false;

            gameManager.setState(
                GameState.MAIN_MENU
            );

            displayedState =
                null;

            syncUIState(
                true
            );

            return;
        }

        // -----------------------------------------------------
        // EXIT APPLICATION
        // -----------------------------------------------------

        if (world != null) {

            world.dispose();

            world =
                null;
        }

        Gdx.app.exit();
    }

    // =========================================================
    // CANCEL EXIT
    // =========================================================

    private void cancelExit() {

        exitConfirmation =
            false;

        exitToMainMenu =
            false;

        uiManager.clearOverlayFlags();

        syncUIState(
            true
        );
    }

    // =========================================================
    // BACKGROUND
    // =========================================================

    private void renderCurrentBackground(
        GameState state
    ) {

        if (
            (
                state ==
                GameState.PLAYING
                ||
                state ==
                GameState.PAUSED
                ||
                state ==
                GameState.LEVEL1_INTRO
            )
            &&
            world != null
        ) {

            world.render();

            return;
        }

        uiManager.renderBackdrop(
            true
        );
    }

    // =========================================================
    // CURSOR / INPUT MODE
    // =========================================================

    private void setCursorForUI() {

        uiManager.setInteractive(
            true
        );
    }

    private void setCursorForGameplay() {

        uiManager.setInteractive(
            false
        );
    }

    // =========================================================
    // RESIZE
    // =========================================================

    @Override
    public void resize(
        int width,
        int height
    ) {

        if (uiManager != null) {

            uiManager
                .getStage()
                .getViewport()
                .update(
                    width,
                    height,
                    true
                );
        }
    }

    // =========================================================
    // DISPOSE
    // =========================================================

    @Override
    public void dispose() {

        if (world != null) {

            world.dispose();

            world =
                null;
        }

        if (uiManager != null) {

            uiManager.dispose();

            uiManager =
                null;
        }
    }
}