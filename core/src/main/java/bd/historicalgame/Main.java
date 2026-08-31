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

    private final String[] menuOptions = {
        "PLAY",
        "SETTINGS",
        "EXIT"
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

        // -----------------------------------------------------
        // MAIN MENU
        // -----------------------------------------------------

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

                    Gdx.app.exit();
                }
            }
        }

        // -----------------------------------------------------
        // LEVEL 1 INTRO
        // -----------------------------------------------------

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
        }

        // -----------------------------------------------------
        // PLAYING
        // -----------------------------------------------------

        else if (state ==
                 GameState.PLAYING) {

            if (Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )) {

                gameManager.pauseGame();
            }
        }

        // -----------------------------------------------------
        // PAUSED
        // -----------------------------------------------------

        else if (state ==
                 GameState.PAUSED) {

            if (Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )) {

                gameManager.resumeGame();
            }
        }
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

        batch.end();
    }

    // =========================================================
    // PAUSE
    // =========================================================

    private void renderPause() {

        Gdx.gl.glEnable(GL20.GL_BLEND);

        batch.begin();

        font.getData().setScale(3f);

        font.draw(
            batch,
            "PAUSED",
            GameConfig.WIDTH / 2f - 80,
            GameConfig.HEIGHT / 2f + 30
        );

        font.getData().setScale(1.2f);

        font.draw(
            batch,
            "Press ESC to resume",
            GameConfig.WIDTH / 2f - 100,
            GameConfig.HEIGHT / 2f - 30
        );

        batch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void dispose() {

        if (world != null) {
            world.dispose();
        }

        batch.dispose();
        font.dispose();
    }
}
