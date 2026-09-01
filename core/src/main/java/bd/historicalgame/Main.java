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
 * UI and gameplay are intentionally separated:
 * - UIManager -> menus, HUD presentation and UI input
 * - World -> 3D world
 * - Player -> movement and camera interaction
 * - GameManager -> game state
 */
public class Main extends ApplicationAdapter {

    private GameManager gameManager;
    private World world;
    private UIManager uiManager;

    private GameState displayedState;

    private int selectedOption = 0;

    private final String[] mainMenuOptions = {
        "PLAY",
        "SETTINGS",
        "EXIT"
    };

    private int pauseOption = 0;

    private final String[] pauseOptions = {
        "RESUME",
        "SETTINGS",
        "EXIT TO MAIN MENU",
        "EXIT GAME"
    };

    private boolean exitConfirmation = false;
    private boolean exitToMainMenu = false;

    @Override
    public void create() {

        gameManager = new GameManager();

        world = null;

        uiManager = new UIManager();

        displayedState = null;

        syncUIState(true);

        System.out.println("================================");
        System.out.println("              2x12");
        System.out.println("      HISTORICAL ADVENTURE");
        System.out.println("================================");
        System.out.println(
            "State: " + gameManager.getCurrentState()
        );
    }

    @Override
    public void render() {

        float delta =
            Math.min(
                Gdx.graphics.getDeltaTime(),
                0.05f
            );

        handleKeyboardInput();

        consumeUIActions();

        GameState state =
            gameManager.getCurrentState();

        syncUIState(false);

        /*
         * Exit confirmation overlay.
         */
        if (
            exitConfirmation ||
            uiManager.isExitConfirmation()
        ) {

            renderBackground(state);

            uiManager.render(delta);

            return;
        }

        /*
         * Settings overlay.
         */
        if (uiManager.isSettingsOpen()) {

            renderBackground(state);

            uiManager.render(delta);

            return;
        }

        switch (state) {

            case MAIN_MENU:

                uiManager.renderBackdrop(true);

                uiManager.render(delta);

                break;

            case LEVEL1_INTRO:

                if (world != null) {
                    world.render();
                }

                uiManager.render(delta);

                break;

            case PLAYING:

                if (world != null) {

                    world.update(delta);

                    world.render();
                }

                uiManager.renderGameplayOverlay(delta);

                break;

            case PAUSED:

                if (world != null) {
                    world.render();
                }

                uiManager.render(delta);

                break;

            default:

                uiManager.renderBackdrop(true);

                uiManager.render(delta);

                break;
        }
    }

    /*
     * =========================================================
     * KEYBOARD INPUT
     * =========================================================
     */

    private void handleKeyboardInput() {

        /*
         * EXIT CONFIRMATION
         */
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

        /*
         * SETTINGS
         */
        if (uiManager.isSettingsOpen()) {

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

        if (
            state ==
            GameState.MAIN_MENU
        ) {

            handleMainMenuInput();

        } else if (
            state ==
            GameState.LEVEL1_INTRO
        ) {

            handleIntroInput();

        } else if (
            state ==
            GameState.PLAYING
        ) {

            handlePlayingInput();

        } else if (
            state ==
            GameState.PAUSED
        ) {

            handlePauseInput();
        }
    }

    /*
     * =========================================================
     * MAIN MENU
     * =========================================================
     */

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
                    mainMenuOptions.length
                )
                %
                mainMenuOptions.length;
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
                )
                %
                mainMenuOptions.length;
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ENTER
            )
            ||
            Gdx.input.isKeyJustPressed(
                Input.Keys.SPACE
            )
        ) {

            activateMainMenuOption();
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {

            requestExit(false);
        }
    }

    private void activateMainMenuOption() {

        switch (selectedOption) {

            case 0:

                startLevel1();

                break;

            case 1:

                openSettings();

                break;

            case 2:

                requestExit(false);

                break;

            default:

                break;
        }
    }

    /*
     * =========================================================
     * INTRO
     * =========================================================
     */

    private void handleIntroInput() {

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ENTER
            )
            ||
            Gdx.input.isKeyJustPressed(
                Input.Keys.SPACE
            )
        ) {

            gameManager.startPlaying();

            uiManager.clearOverlayFlags();

            setCursorForGameplay();

            System.out.println(
                "LEVEL 1 STARTED"
            );
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.Q
            )
            ||
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {

            requestExit(false);
        }
    }

    /*
     * =========================================================
     * PLAYING
     * =========================================================
     */

    private void handlePlayingInput() {

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {

            pauseOption = 0;

            gameManager.pauseGame();

            setCursorForUI();
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.Q
            )
        ) {

            requestExit(false);
        }
    }

    /*
     * =========================================================
     * PAUSE
     * =========================================================
     */

    private void handlePauseInput() {

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
                )
                %
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
                )
                %
                pauseOptions.length;
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ENTER
            )
            ||
            Gdx.input.isKeyJustPressed(
                Input.Keys.SPACE
            )
        ) {

            activatePauseOption();
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.Q
            )
        ) {

            requestExit(false);
        }
    }

    private void activatePauseOption() {

        switch (pauseOption) {

            case 0:

                gameManager.resumeGame();

                setCursorForGameplay();

                break;

            case 1:

                openSettings();

                break;

            case 2:

                requestExit(true);

                break;

            case 3:

                requestExit(false);

                break;

            default:

                break;
        }
    }

    /*
     * =========================================================
     * UI ACTIONS
     * =========================================================
     */

    private void consumeUIActions() {

        UIManager.Action action =
            uiManager.consumeAction();

        switch (action) {

            case START_LEVEL1:

                startLevel1();

                break;

            case START_PLAYING:

                gameManager.startPlaying();

                uiManager.clearOverlayFlags();

                setCursorForGameplay();

                break;

            case RESUME:

                gameManager.resumeGame();

                setCursorForGameplay();

                break;

            case SETTINGS:

                openSettings();

                break;

            case CLOSE_SETTINGS:

                closeSettings();

                break;

            case EXIT_TO_MAIN_MENU:

                requestExit(true);

                break;

            case EXIT_GAME:

                requestExit(false);

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

    /*
     * =========================================================
     * STATE / UI
     * =========================================================
     */

    private void syncUIState(boolean force) {

        GameState state =
            gameManager.getCurrentState();

        if (
            force ||
            state != displayedState
        ) {

            displayedState = state;

            uiManager.clearOverlayFlags();

            uiManager.showState(state);

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

    /*
     * =========================================================
     * LEVEL 1
     * =========================================================
     */

    private void startLevel1() {

        System.out.println(
            "Loading Level 1..."
        );

        if (world != null) {

            world.dispose();
        }

        world = new World();

        gameManager.startLevel1();

        displayedState = null;

        syncUIState(true);
    }

    /*
     * =========================================================
     * SETTINGS
     * =========================================================
     */

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

    /*
     * =========================================================
     * EXIT
     * =========================================================
     */

    private void requestExit(
        boolean toMainMenu
    ) {

        exitToMainMenu =
            toMainMenu;

        exitConfirmation = true;

        uiManager.showExitConfirmation(
            gameManager.isMainMenu()
        );

        setCursorForUI();
    }

    private void confirmExit() {

        exitConfirmation = false;

        uiManager.clearOverlayFlags();

        if (exitToMainMenu) {

            if (world != null) {

                world.dispose();

                world = null;
            }

            exitToMainMenu = false;

            gameManager.setState(
                GameState.MAIN_MENU
            );

            displayedState = null;

            syncUIState(true);

            return;
        }

        if (world != null) {

            world.dispose();

            world = null;
        }

        Gdx.app.exit();
    }

    private void cancelExit() {

        exitConfirmation = false;

        exitToMainMenu = false;

        uiManager.clearOverlayFlags();

        syncUIState(true);
    }

    /*
     * =========================================================
     * BACKGROUND
     * =========================================================
     */

    private void renderBackground(
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

        uiManager.renderBackdrop(true);
    }

    /*
     * =========================================================
     * INPUT MODE
     * =========================================================
     */

    private void setCursorForUI() {

        uiManager.setInteractive(true);
    }

    private void setCursorForGameplay() {

        uiManager.setInteractive(false);
    }

    /*
     * =========================================================
     * RESIZE
     * =========================================================
     */

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

    /*
     * =========================================================
     * DISPOSE
     * =========================================================
     */

    @Override
    public void dispose() {

        if (world != null) {

            world.dispose();

            world = null;
        }

        if (uiManager != null) {

            uiManager.dispose();

            uiManager = null;
        }
    }
}