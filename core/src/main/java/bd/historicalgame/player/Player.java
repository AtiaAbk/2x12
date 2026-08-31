package bd.historicalgame.player;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;

/**
 * 3D player controller for Level 1.
 *
 * The player moves on the X/Z plane.
 * Y represents the player's vertical position.
 */
public class Player {

    private final Vector3 position;

    private final float speed;

    public Player(float startX, float startY, float startZ, float speed) {
        this.position = new Vector3(startX, startY, startZ);
        this.speed = speed;
    }

    /**
     * Updates player movement.
     *
     * W = forward
     * S = backward
     * A = left
     * D = right
     */
    public void update(float delta) {

        float moveX = 0f;
        float moveZ = 0f;

        if (com.badlogic.gdx.Gdx.input.isKeyPressed(Input.Keys.W)) {
            moveZ -= 1f;
        }

        if (com.badlogic.gdx.Gdx.input.isKeyPressed(Input.Keys.S)) {
            moveZ += 1f;
        }

        if (com.badlogic.gdx.Gdx.input.isKeyPressed(Input.Keys.A)) {
            moveX -= 1f;
        }

        if (com.badlogic.gdx.Gdx.input.isKeyPressed(Input.Keys.D)) {
            moveX += 1f;
        }

        // Prevent faster diagonal movement.
        if (moveX != 0f && moveZ != 0f) {
            float length =
                (float) Math.sqrt(
                    moveX * moveX +
                    moveZ * moveZ
                );

            moveX /= length;
            moveZ /= length;
        }

        position.x += moveX * speed * delta;
        position.z += moveZ * speed * delta;
    }

    public Vector3 getPosition() {
        return position;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public float getZ() {
        return position.z;
    }
}