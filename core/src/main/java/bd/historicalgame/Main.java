package bd.historicalgame;

import bd.historicalgame.game.GameConfig;
import bd.historicalgame.game.GameManager;
import bd.historicalgame.game.GameState;
import bd.historicalgame.ui.UIManager;
import bd.historicalgame.world.World;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;

/**
 * Main entry point of 2x12.
 *
 * Gameplay input remains polling-based exactly as before. The UI layer only
 * adds visual rendering and mouse hit-testing, so it never replaces the
 * gameplay InputProcessor.
 */
public class Main extends ApplicationAdapter {

    private GameManager gameManager;
    private World world;
    private UIManager ui;

    private int selectedOption = 0;
    private int pauseOption = 0;
    private int exitSelection = 1;

    @Override
    public void create() {
        ui = new UIManager();
        gameManager = new GameManager();

        // Keep the original polling-based gameplay input model.
        Gdx.input.setInputProcessor(null);
        Gdx.input.setCursorCatched(false);

        world = null;

        System.out.println("================================");
        System.out.println("              2x12");
        System.out.println("      HISTORICAL ADVENTURE");
        System.out.println("================================");
        System.out.println("State: " + gameManager.getCurrentState());
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), GameConfig.MAX_DELTA);
        ui.update(delta);
        handleInput();

        GameState state = gameManager.getCurrentState();

        if (ui.isExitConfirmation()) {
            renderBase(state);
            ui.renderExitConfirmation(exitSelection, ui.isExitFromMainMenu());
            return;
        }

        if (ui.isSettingsOpen()) {
            renderBase(state);
            ui.renderSettings();
            return;
        }

        switch (state) {
            case MAIN_MENU:
                ui.renderMainMenu(selectedOption);
                break;
            case LEVEL1_INTRO:
                ui.renderIntro();
                break;
            case PLAYING:
                if (world != null) {
                    world.update(delta);
                    world.render();
                }
                break;
            case PAUSED:
                if (world != null) world.render();
                ui.renderPause(pauseOption);
                break;
            default:
                // Keep legacy fallback behavior.
                ui.renderMainMenu(selectedOption);
                break;
        }
    }

    private void renderBase(GameState state) {
        if (state == GameState.PLAYING || state == GameState.PAUSED) {
            if (world != null) {
                world.render();
                return;
            }
        }

        if (state == GameState.LEVEL1_INTRO) {
            ui.renderIntro();
        } else {
            ui.renderMainMenu(selectedOption);
        }
    }

    // =========================================================
    // INPUT
    // =========================================================

    private void handleInput() {
        GameState state = gameManager.getCurrentState();

        // -----------------------------------------------------
        // EXIT CONFIRMATION
        // -----------------------------------------------------
        if (ui.isExitConfirmation()) {
            int mouseChoice = ui.getExitButtonAtMouse();

            if (mouseChoice >= 0) {
                exitSelection = mouseChoice;
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    confirmExitChoice();
                    return;
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                exitSelection = exitSelection == 0 ? 1 : 0;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                exitSelection = exitSelection == 1 ? 0 : 1;
            }
            if (isConfirmPressed()) {
                confirmExitChoice();
                return;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                ui.closeOverlay();
            }
            return;
        }

        // -----------------------------------------------------
        // SETTINGS
        // -----------------------------------------------------
        if (ui.isSettingsOpen()) {
            if (ui.isSettingsBackAtMouse() &&
                Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                ui.closeOverlay();
                return;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
                Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
                ui.closeOverlay();
            }
            return;
        }

        // -----------------------------------------------------
        // MAIN MENU
        // -----------------------------------------------------
        if (state == GameState.MAIN_MENU) {
            int mouseIndex = ui.getMainButtonAtMouse();
            if (mouseIndex >= 0) {
                selectedOption = mouseIndex;
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    activateMainOption(selectedOption);
                    return;
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                selectedOption = (selectedOption + 2) % 3;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                selectedOption = (selectedOption + 1) % 3;
            }
            if (isConfirmPressed()) {
                activateMainOption(selectedOption);
                return;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.Q) ||
                Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                requestExit(true);
            }
            return;
        }

        // -----------------------------------------------------
        // LEVEL 1 INTRO
        // -----------------------------------------------------
        if (state == GameState.LEVEL1_INTRO) {
            if (isConfirmPressed()) {
                Gdx.input.setCursorCatched(true);
                gameManager.startPlaying();
                return;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.Q) ||
                Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                requestExit(false);
            }
            return;
        }

        // -----------------------------------------------------
        // PLAYING
        // -----------------------------------------------------
        if (state == GameState.PLAYING) {
            // Preserve existing behavior: ESC pauses, Q asks to exit.
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                pauseOption = 0;
                Gdx.input.setCursorCatched(false);
                gameManager.pauseGame();
                return;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
                requestExit(false);
            }
            return;
        }

        // -----------------------------------------------------
        // PAUSED
        // -----------------------------------------------------
        if (state == GameState.PAUSED) {
            int mouseIndex = ui.getPauseButtonAtMouse();
            if (mouseIndex >= 0) {
                pauseOption = mouseIndex;
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    handlePauseSelection();
                    return;
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                Gdx.input.setCursorCatched(true);
                gameManager.resumeGame();
                return;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                pauseOption = (pauseOption + 3) % 4;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                pauseOption = (pauseOption + 1) % 4;
            }
            if (isConfirmPressed()) {
                handlePauseSelection();
            }
        }
    }

    private void activateMainOption(int option) {
        if (option == 0) {
            startLevel1();
        } else if (option == 1) {
            ui.setSettingsContext(false);
        } else {
            requestExit(true);
        }
    }

    private void handlePauseSelection() {
        if (pauseOption == 0) {
            Gdx.input.setCursorCatched(true);
            gameManager.resumeGame();
        } else if (pauseOption == 1) {
            ui.setSettingsContext(true);
            Gdx.input.setCursorCatched(false);
        } else if (pauseOption == 2) {
            requestExitFromPauseToMainMenu();
        } else if (pauseOption == 3) {
            requestExit(false);
        }
    }

    private void requestExit(boolean fromMainMenu) {
        exitSelection = 1;
        ui.setExitContext(fromMainMenu);
        Gdx.input.setCursorCatched(false);
    }

    private void requestExitFromPauseToMainMenu() {
        exitSelection = 1;
        // The distinction is handled by Main after confirmation.
        ui.setExitContext(false);
        // A paused exit-to-menu is tracked by a dedicated field below.
        exitToMainMenuAfterConfirm = true;
        Gdx.input.setCursorCatched(false);
    }

    private boolean exitToMainMenuAfterConfirm = false;

    private void confirmExitChoice() {
        if (exitSelection == 1) {
            exitToMainMenuAfterConfirm = false;
            ui.closeOverlay();
            return;
        }

        if (exitToMainMenuAfterConfirm) {
            exitToMainMenuAfterConfirm = false;
            if (world != null) {
                world.dispose();
                world = null;
            }
            gameManager.setState(GameState.MAIN_MENU);
            selectedOption = 0;
            Gdx.input.setCursorCatched(false);
            ui.closeOverlay();
            return;
        }

        exitGame();
    }

    private void startLevel1() {
        System.out.println("Loading Level 1...");

        if (world != null) world.dispose();
        world = new World();

        Gdx.input.setCursorCatched(false);
        Gdx.input.setInputProcessor(null);
        gameManager.startLevel1();
    }

    private void exitGame() {
        System.out.println("Exiting 2x12...");
        if (world != null) {
            world.dispose();
            world = null;
        }
        Gdx.app.exit();
    }

    private boolean isConfirmPressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
    }

    @Override
    public void dispose() {
        if (world != null) {
            world.dispose();
            world = null;
        }
        if (ui != null) ui.dispose();
    }
}
