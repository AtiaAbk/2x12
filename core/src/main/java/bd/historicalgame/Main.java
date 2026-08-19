package bd.historicalgame;

import bd.historicalgame.game.GameConfig;
import bd.historicalgame.game.GameManager;
import bd.historicalgame.game.GameState;
import bd.historicalgame.player.Player;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {

    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;

    private GameManager gameManager;
    private Player player;

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
        shapeRenderer = new ShapeRenderer();

        gameManager = new GameManager();

        player = new Player(
            GameConfig.WIDTH / 2f - 20,
            GameConfig.HEIGHT / 2f - 20
        );

        gameManager.setState(GameState.MAIN_MENU);

        System.out.println("================================");
        System.out.println("          2x12");
        System.out.println("    HISTORICAL ADVENTURE");
        System.out.println("================================");
        System.out.println("State: " + gameManager.getCurrentState());
    }

    @Override
    public void render() {

        float delta = Gdx.graphics.getDeltaTime();

        handleInput();

        GameState state = gameManager.getCurrentState();

        if (state == GameState.MAIN_MENU) {
            renderMainMenu();
        }

        else if (state == GameState.PLAYING) {
            updateGame(delta);
            renderGame();
        }

        else if (state == GameState.PAUSED) {
            renderGame();
            renderPauseScreen();
        }
    }

    // =========================================================
    // INPUT
    // =========================================================

    private void handleInput() {

        GameState state = gameManager.getCurrentState();

        // -------------------------
        // MAIN MENU
        // -------------------------

        if (state == GameState.MAIN_MENU) {

            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {

                selectedOption--;

                if (selectedOption < 0) {
                    selectedOption = menuOptions.length - 1;
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {

                selectedOption++;

                if (selectedOption >= menuOptions.length) {
                    selectedOption = 0;
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {

                switch (selectedOption) {

                    case 0:
                        gameManager.setState(GameState.PLAYING);
                        System.out.println("Starting 2x12...");
                        break;

                    case 1:
                        System.out.println("Settings selected.");
                        break;

                    case 2:
                        System.out.println("Exit selected.");
                        Gdx.app.exit();
                        break;
                }
            }
        }

        // -------------------------
        // PLAYING
        // -------------------------

        else if (state == GameState.PLAYING) {

            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {

                gameManager.setState(GameState.PAUSED);

                System.out.println("Game paused.");
            }
        }

        // -------------------------
        // PAUSED
        // -------------------------

        else if (state == GameState.PAUSED) {

            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {

                gameManager.setState(GameState.PLAYING);

                System.out.println("Game resumed.");
            }
        }
    }

    // =========================================================
    // GAME UPDATE
    // =========================================================

    private void updateGame(float delta) {

        player.update(delta);
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

        // Title
        font.getData().setScale(3.5f);

        font.draw(
            batch,
            GameConfig.GAME_NAME,
            100,
            GameConfig.HEIGHT - 100
        );

        // Subtitle
        font.getData().setScale(1.2f);

        font.draw(
            batch,
            "HISTORICAL ADVENTURE",
            105,
            GameConfig.HEIGHT - 145
        );

        // Menu
        font.getData().setScale(1.5f);

        float startY = GameConfig.HEIGHT - 280;

        for (int i = 0; i < menuOptions.length; i++) {

            String prefix =
                (i == selectedOption)
                    ? "> "
                    : "  ";

            font.draw(
                batch,
                prefix + menuOptions[i],
                120,
                startY - (i * 70)
            );
        }

        // State
        font.getData().setScale(1f);

        font.draw(
            batch,
            "STATE: " + gameManager.getCurrentState(),
            100,
            80
        );

        font.draw(
            batch,
            "UP / DOWN  Select     ENTER  Confirm",
            100,
            45
        );

        batch.end();
    }

    // =========================================================
    // GAME WORLD
    // =========================================================

    private void renderGame() {

        ScreenUtils.clear(
            0.08f,
            0.12f,
            0.08f,
            1f
        );

        // World grid
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(
            0.15f,
            0.25f,
            0.15f,
            1f
        );

        for (int x = 0; x <= GameConfig.WIDTH; x += 80) {

            shapeRenderer.line(
                x,
                0,
                x,
                GameConfig.HEIGHT
            );
        }

        for (int y = 0; y <= GameConfig.HEIGHT; y += 80) {

            shapeRenderer.line(
                0,
                y,
                GameConfig.WIDTH,
                y
            );
        }

        shapeRenderer.end();

        // Player
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(Color.WHITE);

        shapeRenderer.rect(
            player.getX(),
            player.getY(),
            player.getSize(),
            player.getSize()
        );

        shapeRenderer.end();

        // HUD
        batch.begin();

        font.getData().setScale(1.3f);

        font.draw(
            batch,
            "2x12",
            30,
            GameConfig.HEIGHT - 30
        );

        font.getData().setScale(1f);

        font.draw(
            batch,
            "W A S D  -  MOVE",
            30,
            35
        );

        font.draw(
            batch,
            "ESC  -  PAUSE",
            GameConfig.WIDTH - 150,
            35
        );

        font.draw(
            batch,
            "STATE: PLAYING",
            GameConfig.WIDTH - 170,
            GameConfig.HEIGHT - 30
        );

        batch.end();
    }

    // =========================================================
    // PAUSE
    // =========================================================

    private void renderPauseScreen() {

        // Dark overlay
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(
            0f,
            0f,
            0f,
            0.65f
        );

        shapeRenderer.rect(
            0,
            0,
            GameConfig.WIDTH,
            GameConfig.HEIGHT
        );

        shapeRenderer.end();

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
            GameConfig.WIDTH / 2f - 95,
            GameConfig.HEIGHT / 2f - 30
        );

        batch.end();
    }

    @Override
    public void dispose() {

        batch.dispose();
        font.dispose();
        shapeRenderer.dispose();
    }
}
