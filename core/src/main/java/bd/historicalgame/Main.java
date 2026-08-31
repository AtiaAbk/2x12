package bd.historicalgame;

import bd.historicalgame.game.GameConfig;
import bd.historicalgame.game.GameManager;
import bd.historicalgame.game.GameState;
import bd.historicalgame.world.World;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Main entry point of 2x12.
 */
public class Main extends ApplicationAdapter {

    private SpriteBatch batch;
    private BitmapFont font;

    private GameManager gameManager;
    private World world;

    private int selectedOption = 0;

    /*
     * Main menu options.
     */
    private final String[] menuOptions = {
        "PLAY",
        "SETTINGS",
        "EXIT"
    };

    /*
     * Pause menu options.
     */
    private int pauseOption = 0;

    private final String[] pauseOptions = {
        "RESUME",
        "EXIT TO MAIN MENU",
        "EXIT GAME"
    };

    /*
     * Exit confirmation.
     */
    private boolean exitConfirmation = false;

    private int exitSelection = 0;

    private final String[] exitOptions = {
        "YES",
        "NO"
    };

    @Override
    public void create() {

        batch = new SpriteBatch();
        font = new BitmapFont();

        gameManager = new GameManager();

        world = null;

        System.out.println("================================");
        System.out.println("              2x12");
        System.out.println("      HISTORICAL ADVENTURE");
        System.out.println("================================");
        System.out.println(
            "State: " +
            gameManager.getCurrentState()
        );
    }

    @Override
    public void render() {

        float delta =
            Gdx.graphics.getDeltaTime();

        handleInput();

        GameState state =
            gameManager.getCurrentState();

        /*
         * If exit confirmation is active,
         * show confirmation over the current screen.
         */
        if (exitConfirmation) {

            renderCurrentBackground(state);
            renderExitConfirmation();

            return;
        }

        switch (state) {

            case MAIN_MENU:
                renderMainMenu();
                break;

            case LEVEL1_INTRO:
                renderLevel1Intro();
                break;

            case PLAYING:

                if (world != null) {

                    world.update(delta);
                    world.render();
                }

                break;

            case PAUSED:

                if (world != null) {
                    world.render();
                }

                renderPause();
                break;

            default:
                renderMainMenu();
                break;
        }
    }

    // =========================================================
    // INPUT
    // =========================================================

    private void handleInput() {

        GameState state =
            gameManager.getCurrentState();

        // =====================================================
        // EXIT CONFIRMATION
        // =====================================================

        if (exitConfirmation) {

            handleExitConfirmation();

            return;
        }

        // =====================================================
        // MAIN MENU
        // =====================================================

        if (state == GameState.MAIN_MENU) {

            if (Gdx.input.isKeyJustPressed(
                Input.Keys.UP
            )) {

                selectedOption--;

                if (selectedOption < 0) {

                    selectedOption =
                        menuOptions.length - 1;
                }
            }

            if (Gdx.input.isKeyJustPressed(
                Input.Keys.DOWN
            )) {

                selectedOption++;

                if (selectedOption >=
                    menuOptions.length) {

                    selectedOption = 0;
                }
            }

            if (Gdx.input.isKeyJustPressed(
                Input.Keys.ENTER
            )) {

                if (selectedOption == 0) {

                    startLevel1();
                }

                else if (selectedOption == 1) {

                    System.out.println(
                        "Settings selected."
                    );
                }

                else if (selectedOption == 2) {

                    requestExit();
                }
            }

            /*
             * Q or ESC from main menu
             * asks for exit confirmation.
             */
            if (Gdx.input.isKeyJustPressed(
                Input.Keys.Q
            ) ||
                Gdx.input.isKeyJustPressed(
                    Input.Keys.ESCAPE
                )) {

                requestExit();
            }
        }

        // =====================================================
        // LEVEL 1 INTRO
        // =====================================================

        else if (state ==
                 GameState.LEVEL1_INTRO) {

            if (Gdx.input.isKeyJustPressed(
                Input.Keys.ENTER
            )) {

                gameManager.startPlaying();

                System.out.println(
                    "LEVEL 1 STARTED"
                );
            }

            /*
             * Q immediately requests exit.
             */
            if (Gdx.input.isKeyJustPressed(
                Input.Keys.Q
            )) {

                requestExit();
            }
        }

        // =====================================================
        // PLAYING
        // =====================================================

        else if (state ==
                 GameState.PLAYING) {

            /*
             * ESC opens pause menu.
             */
            if (Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )) {

                pauseOption = 0;

                gameManager.pauseGame();

                return;
            }

            /*
             * Q asks to exit.
             */
            if (Gdx.input.isKeyJustPressed(
                Input.Keys.Q
            )) {

                requestExit();
            }
        }

        // =====================================================
        // PAUSED
        // =====================================================

        else if (state ==
                 GameState.PAUSED) {

            /*
             * ESC resumes the game.
             */
            if (Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )) {

                gameManager.resumeGame();

                return;
            }

            /*
             * UP.
             */
            if (Gdx.input.isKeyJustPressed(
                Input.Keys.UP
            )) {

                pauseOption--;

                if (pauseOption < 0) {

                    pauseOption =
                        pauseOptions.length - 1;
                }
            }

            /*
             * DOWN.
             */
            if (Gdx.input.isKeyJustPressed(
                Input.Keys.DOWN
            )) {

                pauseOption++;

                if (pauseOption >=
                    pauseOptions.length) {

                    pauseOption = 0;
                }
            }

            /*
             * ENTER.
             */
            if (Gdx.input.isKeyJustPressed(
                Input.Keys.ENTER
            )) {

                handlePauseSelection();
            }

            /*
             * Q asks for exit.
             */
            if (Gdx.input.isKeyJustPressed(
                Input.Keys.Q
            )) {

                requestExit();
            }
        }
    }

    // =========================================================
    // PAUSE MENU SELECTION
    // =========================================================

    private void handlePauseSelection() {

        /*
         * RESUME
         */
        if (pauseOption == 0) {

            gameManager.resumeGame();
        }

        /*
         * EXIT TO MAIN MENU
         */
        else if (pauseOption == 1) {

            requestExitToMainMenu();
        }

        /*
         * EXIT GAME
         */
        else if (pauseOption == 2) {

            requestExit();
        }
    }

    // =========================================================
    // EXIT SYSTEM
    // =========================================================

    private void requestExit() {

        exitConfirmation = true;
        exitSelection = 1;

        System.out.println(
            "Exit confirmation opened."
        );
    }

    private void requestExitToMainMenu() {

        exitConfirmation = true;
        exitSelection = 1;

        System.out.println(
            "Main menu confirmation opened."
        );
    }

    private void handleExitConfirmation() {

        /*
         * LEFT / RIGHT
         */
        if (Gdx.input.isKeyJustPressed(
            Input.Keys.LEFT
        )) {

            exitSelection--;

            if (exitSelection < 0) {
                exitSelection =
                    exitOptions.length - 1;
            }
        }

        if (Gdx.input.isKeyJustPressed(
            Input.Keys.RIGHT
        )) {

            exitSelection++;

            if (exitSelection >=
                exitOptions.length) {

                exitSelection = 0;
            }
        }

        /*
         * ENTER
         */
        if (Gdx.input.isKeyJustPressed(
            Input.Keys.ENTER
        )) {

            /*
             * YES
             */
            if (exitSelection == 0) {

                exitGame();
            }

            /*
             * NO
             */
            else {

                exitConfirmation = false;
            }
        }

        /*
         * ESC cancels confirmation.
         */
        if (Gdx.input.isKeyJustPressed(
            Input.Keys.ESCAPE
        )) {

            exitConfirmation = false;
        }
    }

    private void exitGame() {

        System.out.println(
            "Exiting 2x12..."
        );

        if (world != null) {

            world.dispose();
            world = null;
        }

        Gdx.app.exit();
    }

    // =========================================================
    // START LEVEL 1
    // =========================================================

    private void startLevel1() {

        System.out.println(
            "Loading Level 1..."
        );

        if (world != null) {

            world.dispose();
        }

        world = new World();

        gameManager.startLevel1();
    }

    // =========================================================
    // BACKGROUND
    // =========================================================

    private void renderCurrentBackground(
        GameState state
    ) {

        if (state == GameState.PLAYING ||
            state == GameState.PAUSED) {

            if (world != null) {

                world.render();
                return;
            }
        }

        if (state == GameState.LEVEL1_INTRO) {

            renderLevel1Intro();
            return;
        }

        renderMainMenu();
    }

    // =========================================================
    // MAIN MENU
    // =========================================================

    private void renderMainMenu() {

        ScreenUtils.clear(
            0.02f,
            0.02f,
            0.03f,
            1f
        );

        batch.begin();

        font.getData().setScale(4f);

        font.draw(
            batch,
            GameConfig.GAME_NAME,
            100,
            GameConfig.HEIGHT - 100
        );

        font.getData().setScale(1.3f);

        font.draw(
            batch,
            "HISTORICAL ADVENTURE",
            105,
            GameConfig.HEIGHT - 150
        );

        font.getData().setScale(1.6f);

        float startY =
            GameConfig.HEIGHT - 280;

        for (int i = 0;
             i < menuOptions.length;
             i++) {

            String prefix =
                i == selectedOption
                ? "> "
                : "  ";

            font.draw(
                batch,
                prefix + menuOptions[i],
                120,
                startY - i * 70
            );
        }

        font.getData().setScale(1f);

        font.draw(
            batch,
            "UP / DOWN  Select",
            100,
            70
        );

        font.draw(
            batch,
            "ENTER  Confirm",
            100,
            40
        );

        font.draw(
            batch,
            "ESC / Q  Exit",
            100,
            15
        );

        batch.end();
    }

    // =========================================================
    // LEVEL 1 INTRO
    // =========================================================

    private void renderLevel1Intro() {

        ScreenUtils.clear(
            0.04f,
            0.05f,
            0.06f,
            1f
        );

        batch.begin();

        font.getData().setScale(3f);

        font.draw(
            batch,
            "LEVEL 1",
            100,
            GameConfig.HEIGHT - 100
        );

        font.getData().setScale(2f);

        font.draw(
            batch,
            "WHERE IT ALL BEGAN",
            100,
            GameConfig.HEIGHT - 170
        );

        font.getData().setScale(1.3f);

        font.draw(
            batch,
            "Mid-June 2024",
            100,
            GameConfig.HEIGHT - 260
        );

        font.getData().setScale(1.1f);

        font.draw(
            batch,
            "A normal day is about to change.",
            100,
            GameConfig.HEIGHT - 320
        );

        font.draw(
            batch,
            "Explore the campus and discover what is happening.",
            100,
            GameConfig.HEIGHT - 355
        );

        font.getData().setScale(1f);

        font.draw(
            batch,
            "Press ENTER to begin",
            100,
            100
        );

        font.draw(
            batch,
            "Press Q to exit",
            100,
            60
        );

        batch.end();
    }

    // =========================================================
    // PAUSE
    // =========================================================

    private void renderPause() {

        /*
         * Dark transparent overlay.
         */
        Gdx.gl.glEnable(GL20.GL_BLEND);

        batch.begin();

        font.getData().setScale(3f);

        font.draw(
            batch,
            "PAUSED",
            GameConfig.WIDTH / 2f - 80,
            GameConfig.HEIGHT / 2f + 150
        );

        font.getData().setScale(1.5f);

        for (int i = 0;
             i < pauseOptions.length;
             i++) {

            String prefix =
                i == pauseOption
                ? "> "
                : "  ";

            font.draw(
                batch,
                prefix + pauseOptions[i],
                GameConfig.WIDTH / 2f - 130,
                GameConfig.HEIGHT / 2f + 50 - i * 60
            );
        }

        font.getData().setScale(1f);

        font.draw(
            batch,
            "UP / DOWN  Select",
            GameConfig.WIDTH / 2f - 100,
            80
        );

        font.draw(
            batch,
            "ENTER  Confirm",
            GameConfig.WIDTH / 2f - 100,
            50
        );

        font.draw(
            batch,
            "ESC  Resume",
            GameConfig.WIDTH / 2f - 100,
            20
        );

        batch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    // =========================================================
    // EXIT CONFIRMATION
    // =========================================================

    private void renderExitConfirmation() {

        Gdx.gl.glEnable(GL20.GL_BLEND);

        batch.begin();

        font.getData().setScale(2.5f);

        font.draw(
            batch,
            "EXIT GAME?",
            GameConfig.WIDTH / 2f - 100,
            GameConfig.HEIGHT / 2f + 100
        );

        font.getData().setScale(1.3f);

        font.draw(
            batch,
            "Are you sure you want to exit?",
            GameConfig.WIDTH / 2f - 160,
            GameConfig.HEIGHT / 2f + 40
        );

        font.getData().setScale(1.6f);

        String yes =
            exitSelection == 0
            ? "> YES"
            : "  YES";

        String no =
            exitSelection == 1
            ? "> NO"
            : "  NO";

        font.draw(
            batch,
            yes,
            GameConfig.WIDTH / 2f - 100,
            GameConfig.HEIGHT / 2f - 50
        );

        font.draw(
            batch,
            no,
            GameConfig.WIDTH / 2f + 30,
            GameConfig.HEIGHT / 2f - 50
        );

        font.getData().setScale(1f);

        font.draw(
            batch,
            "LEFT / RIGHT  Select",
            GameConfig.WIDTH / 2f - 100,
            70
        );

        font.draw(
            batch,
            "ENTER  Confirm",
            GameConfig.WIDTH / 2f - 100,
            40
        );

        font.draw(
            batch,
            "ESC  Cancel",
            GameConfig.WIDTH / 2f - 100,
            15
        );

        batch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    // =========================================================
    // DISPOSE
    // =========================================================

    @Override
    public void dispose() {

        if (world != null) {

            world.dispose();
            world = null;
        }

        if (batch != null) {
            batch.dispose();
        }

        if (font != null) {
            font.dispose();
        }
    }
}