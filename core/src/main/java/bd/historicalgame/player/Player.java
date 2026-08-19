package bd.historicalgame.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Main player entity for 2x12.
 */
public class Player {

    private float x;
    private float y;

    private final float speed = 250f;
    private final float size = 40f;

    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;
    }

    public void update(float delta) {

        float dx = 0;
        float dy = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            dy += 1;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            dy -= 1;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            dx -= 1;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            dx += 1;
        }

        // Prevent diagonal movement from being faster.
        if (dx != 0 && dy != 0) {
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            dx /= length;
            dy /= length;
        }

        x += dx * speed * delta;
        y += dy * speed * delta;

        // Keep player inside the screen.
        x = Math.max(0, Math.min(x, 1280 - size));
        y = Math.max(0, Math.min(y, 720 - size));
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getSize() {
        return size;
    }
}
