package bd.historicalgame.player;

import bd.historicalgame.game.GameConfig;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Main player entity for 2x12.
 */
public class Player {

    private float x;
    private float y;

    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;
    }

    public void update(float delta) {

        float moveX = 0f;
        float moveY = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.W)
                || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            moveY += 1f;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.S)
                || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            moveY -= 1f;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)
                || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveX -= 1f;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)
                || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveX += 1f;
        }

        x += moveX * GameConfig.PLAYER_SPEED * delta;
        y += moveY * GameConfig.PLAYER_SPEED * delta;

        // Keep player inside the world.
        x = Math.max(0f, Math.min(x, GameConfig.WORLD_WIDTH));
        y = Math.max(0f, Math.min(y, GameConfig.WORLD_HEIGHT));
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
