package bd.historicalgame.player;

import com.badlogic.gdx.Gdx;
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

    // Level 1 playable area.
    private static final float MIN_X = -28f;
    private static final float MAX_X = 28f;

    private static final float MIN_Z = -18f;
    private static final float MAX_Z = 18f;

    public Player(
        float startX,
        float startY,
        float startZ,
        float speed
    ) {

        this.position =
            new Vector3(
                startX,
                startY,
                startZ
            );

        this.speed = speed;

        clampToWorld();
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

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            moveZ -= 1f;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            moveZ += 1f;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            moveX -= 1f;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
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

        // Apply movement.
        position.x +=
            moveX * speed * delta;

        position.z +=
            moveZ * speed * delta;

        // Keep player inside Level 1.
        clampToWorld();
    }

    /**
     * Keeps the player inside the playable Level 1 area.
     */
    private void clampToWorld() {

        if (position.x < MIN_X) {
            position.x = MIN_X;
        }

        if (position.x > MAX_X) {
            position.x = MAX_X;
        }

        if (position.z < MIN_Z) {
            position.z = MIN_Z;
        }

        if (position.z > MAX_Z) {
            position.z = MAX_Z;
        }
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