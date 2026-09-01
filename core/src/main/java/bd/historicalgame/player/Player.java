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
 *
 * Features:
 * - WASD movement
 * - Smooth acceleration
 * - Smooth deceleration
 * - Normalized diagonal movement
 * - Collision detection
 * - World boundaries
 * - Movement direction
 * - Movement state
 */
public class Player {

    // =========================================================
    // POSITION
    // =========================================================

    private final Vector3 position;

    // =========================================================
    // MOVEMENT
    // =========================================================

    private final float speed;

    /*
     * Current movement velocity.
     *
     * Keeping velocity separate from position makes movement
     * feel smoother than instantly changing position.
     */
    private float velocityX = 0f;
    private float velocityZ = 0f;

    /*
     * Movement acceleration/deceleration.
     */
    private static final float ACCELERATION = 18f;
    private static final float DECELERATION = 22f;

    /*
     * Small threshold used to stop tiny residual movement.
     */
    private static final float VELOCITY_EPSILON = 0.01f;

    // =========================================================
    // COLLISION
    // =========================================================

    private static final float COLLISION_RADIUS = 0.6f;

    private final Array<Rectangle> collisionObjects;

    // =========================================================
    // WORLD BOUNDARIES
    // =========================================================

    private static final float MIN_X = -28f;
    private static final float MAX_X = 28f;

    private static final float MIN_Z = -18f;
    private static final float MAX_Z = 18f;

    // =========================================================
    // MOVEMENT STATE
    // =========================================================

    private boolean moving = false;

    /*
     * Last meaningful movement direction.
     *
     * Default direction points toward negative Z,
     * which matches the initial Level 1 movement direction.
     */
    private final Vector3 movementDirection =
        new Vector3(
            0f,
            0f,
            -1f
        );

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public Player(
        float startX,
        float startY,
        float startZ,
        float speed
    ) {

        position =
            new Vector3(
                startX,
                startY,
                startZ
            );

        this.speed = speed;

        collisionObjects =
            new Array<>();

        clampToWorld();
    }

    // =========================================================
    // COLLISION MANAGEMENT
    // =========================================================

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

    // =========================================================
    // UPDATE
    // =========================================================

    /**
     * Updates player movement.
     *
     * W = forward
     * S = backward
     * A = left
     * D = right
     */
    /**
     * Updates player movement using camera-relative horizontal directions.
     * The supplied directions must have no vertical component.
     */
    public void update(
        float delta,
        Vector3 cameraForward,
        Vector3 cameraRight
    ) {

        /*
         * Prevent very large frame jumps.
         */
        if (delta > 0.1f) {
            delta = 0.1f;
        }

        // -----------------------------------------------------
        // INPUT
        // -----------------------------------------------------

        float forwardInput = 0f;
        float rightInput = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            forwardInput += 1f;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            forwardInput -= 1f;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            rightInput -= 1f;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            rightInput += 1f;
        }

        float inputX =
            cameraForward.x * forwardInput +
            cameraRight.x * rightInput;

        float inputZ =
            cameraForward.z * forwardInput +
            cameraRight.z * rightInput;

        float inputLength =
            (float) Math.sqrt(
                inputX * inputX +
                inputZ * inputZ
            );

        if (inputLength > 1f) {
            inputX /= inputLength;
            inputZ /= inputLength;
        }

        // -----------------------------------------------------
        // MOVEMENT STATE
        // -----------------------------------------------------

        moving =
            inputX != 0f ||
            inputZ != 0f;

        // -----------------------------------------------------
        // MOVEMENT DIRECTION
        // -----------------------------------------------------

        if (moving) {

            movementDirection.set(
                inputX,
                0f,
                inputZ
            ).nor();
        }

        // -----------------------------------------------------
        // TARGET VELOCITY
        // -----------------------------------------------------

        float targetVelocityX =
            inputX * speed;

        float targetVelocityZ =
            inputZ * speed;

        // -----------------------------------------------------
        // SMOOTH ACCELERATION
        // -----------------------------------------------------

        if (moving) {

            velocityX =
                moveTowards(
                    velocityX,
                    targetVelocityX,
                    ACCELERATION * delta
                );

            velocityZ =
                moveTowards(
                    velocityZ,
                    targetVelocityZ,
                    ACCELERATION * delta
                );

        } else {

            // -------------------------------------------------
            // SMOOTH DECELERATION
            // -------------------------------------------------

            velocityX =
                moveTowards(
                    velocityX,
                    0f,
                    DECELERATION * delta
                );

            velocityZ =
                moveTowards(
                    velocityZ,
                    0f,
                    DECELERATION * delta
                );
        }

        // -----------------------------------------------------
        // FRAME MOVEMENT
        // -----------------------------------------------------

        float deltaX =
            velocityX * delta;

        float deltaZ =
            velocityZ * delta;

        // -----------------------------------------------------
        // COLLISION-AWARE MOVEMENT
        // -----------------------------------------------------

        tryMoveX(deltaX);

        tryMoveZ(deltaZ);

        // -----------------------------------------------------
        // WORLD BOUNDARY
        // -----------------------------------------------------

        clampToWorld();

        // -----------------------------------------------------
        // CLEAN UP VERY SMALL VELOCITIES
        // -----------------------------------------------------

        if (
            Math.abs(velocityX) <
            VELOCITY_EPSILON
        ) {

            velocityX = 0f;
        }

        if (
            Math.abs(velocityZ) <
            VELOCITY_EPSILON
        ) {

            velocityZ = 0f;
        }
    }

    // =========================================================
    // SMOOTH VALUE MOVEMENT
    // =========================================================

    private float moveTowards(
        float current,
        float target,
        float maxDelta
    ) {

        if (
            Math.abs(
                target - current
            ) <= maxDelta
        ) {

            return target;
        }

        if (current < target) {

            return current + maxDelta;

        } else {

            return current - maxDelta;
        }
    }

    // =========================================================
    // X MOVEMENT
    // =========================================================

    private void tryMoveX(
        float amount
    ) {

        if (amount == 0f) {
            return;
        }

        float newX =
            position.x + amount;

        if (
            !collides(
                newX,
                position.z
            )
        ) {

            position.x = newX;

        } else {

            /*
             * Stop horizontal velocity when
             * hitting an obstacle.
             */
            velocityX = 0f;
        }
    }

    // =========================================================
    // Z MOVEMENT
    // =========================================================

    private void tryMoveZ(
        float amount
    ) {

        if (amount == 0f) {
            return;
        }

        float newZ =
            position.z + amount;

        if (
            !collides(
                position.x,
                newZ
            )
        ) {

            position.z = newZ;

        } else {

            /*
             * Stop forward/backward velocity when
             * hitting an obstacle.
             */
            velocityZ = 0f;
        }
    }

    // =========================================================
    // COLLISION
    // =========================================================

    /**
     * Checks whether the player would collide
     * with any registered collision object.
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

        for (
            Rectangle obstacle :
            collisionObjects
        ) {

            if (
                playerBounds.overlaps(
                    obstacle
                )
            ) {

                return true;
            }
        }

        return false;
    }

    // =========================================================
    // WORLD BOUNDARY
    // =========================================================

    /**
     * Keeps the player inside Level 1.
     */
    private void clampToWorld() {

        if (position.x < MIN_X) {

            position.x = MIN_X;
            velocityX = 0f;
        }

        if (position.x > MAX_X) {

            position.x = MAX_X;
            velocityX = 0f;
        }

        if (position.z < MIN_Z) {

            position.z = MIN_Z;
            velocityZ = 0f;
        }

        if (position.z > MAX_Z) {

            position.z = MAX_Z;
            velocityZ = 0f;
        }
    }

    // =========================================================
    // POSITION
    // =========================================================

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

    // =========================================================
    // MOVEMENT INFORMATION
    // =========================================================

    /**
     * Returns whether the player is currently moving.
     */
    public boolean isMoving() {

        return moving;
    }

    /**
     * Returns current X velocity.
     */
    public float getVelocityX() {

        return velocityX;
    }

    /**
     * Returns current Z velocity.
     */
    public float getVelocityZ() {

        return velocityZ;
    }

    /**
     * Returns the player's current movement direction.
     */
    public Vector3 getMovementDirection() {

        return movementDirection;
    }

    /**
     * Returns current movement speed.
     */
    public float getCurrentSpeed() {

        return (float) Math.sqrt(
            velocityX * velocityX +
            velocityZ * velocityZ
        );
    }
}