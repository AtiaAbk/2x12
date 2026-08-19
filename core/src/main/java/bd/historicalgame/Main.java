package bd.historicalgame;

import bd.historicalgame.game.GameConfig;
import bd.historicalgame.game.GameManager;
import bd.historicalgame.game.GameState;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {

    private SpriteBatch batch;
    private BitmapFont font;
    private GameManager gameManager;

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
        gameManager.setState(GameState.MAIN_MENU);

        System.out.println("================================");
        System.out.println("          2x12");
        System.out.println("    HISTORICAL ADVENTURE");
        System.out.println("================================");
        System.out.println("State: " + gameManager.getCurrentState());
    }

    @Override
    public void render() {

        handleInput();

        ScreenUtils.clear(0.02f, 0.02f, 0.03f, 1f);

        batch.begin();

        // Title
        font.getData().setScale(3.5f);
        font.draw(
            batch,
            "2x12",
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

            String prefix = (i == selectedOption) ? "> " : "  ";

            font.draw(
                batch,
                prefix + menuOptions[i],
                120,
                startY - (i * 70)
            );
        }

        // Current state
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

    private void handleInput() {

        if (!gameManager.getCurrentState().equals(GameState.MAIN_MENU)) {
            return;
        }

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

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
