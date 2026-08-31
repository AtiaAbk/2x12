package bd.historicalgame.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * 3D player controller for Level 1.
 *
 * Player moves on the X/Z plane.
 * Y represents vertical position.
 */
public class Player {

    private final Vector3 position;

    private final float speed;

    private static final float COLLISION_RADIUS = 0.6f;

    // Level 1 playable boundaries.
    private static final float MIN_X = -28f;
    private static final float MAX_X = 28f;

    private static final float MIN_Z = -18f;
    private static final float MAX_Z = 18f;

    private final Array<Rectangle> collisionObjects;

    public Player(
        float startX,
        float startY,
        float startZ,
        float speed
    ) {

        position = new Vector3(
            startX,
            startY,
            startZ
        );

        this.speed = speed;

        collisionObjects = new Array<>();

        clampToWorld();
    }

    /**
     * Adds a rectangular collision area.
     */
    public void addCollision(
        float x,
        float z,
        float width,
        float depth
    ) {

        collisionObjects.add(
            new Rectangle(
                x,
                z,
                width,
                depth
            )
        );
    }

    /**
     * Removes all collision areas.
     */
    public void clearCollisions() {

        collisionObjects.clear();
    }

    /**
     * Update player movement.
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

        /*
         * Normalize diagonal movement.
         */
        if (moveX != 0f && moveZ != 0f) {

            float length =
                (float) Math.sqrt(
                    moveX * moveX +
                    moveZ * moveZ
                );

            moveX /= length;
            moveZ /= length;
        }

        float deltaX =
            moveX * speed * delta;

        float deltaZ =
            moveZ * speed * delta;

        /*
         * Move separately on X and Z.
         *
         * This allows sliding around buildings.
         */
        tryMoveX(deltaX);
        tryMoveZ(deltaZ);

        clampToWorld();
    }

    private void tryMoveX(float amount) {

        if (amount == 0f) {
            return;
        }

        float newX =
            position.x + amount;

        if (!collides(newX, position.z)) {
            position.x = newX;
        }
    }

    private void tryMoveZ(float amount) {

        if (amount == 0f) {
            return;
        }

        float newZ =
            position.z + amount;

        if (!collides(position.x, newZ)) {
            position.z = newZ;
        }
    }

    /**
     * Collision test.
     */
    private boolean collides(
        float x,
        float z
    ) {

        Rectangle playerBounds =
            new Rectangle(
                x - COLLISION_RADIUS,
                z - COLLISION_RADIUS,
                COLLISION_RADIUS * 2f,
                COLLISION_RADIUS * 2f
            );

        for (Rectangle obstacle :
             collisionObjects) {

            if (playerBounds.overlaps(obstacle)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Keep player inside Level 1.
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