package bd.historicalgame.player;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * Camera-relative 3D player controller for Level 1.
 * Movement is kept independent from camera orientation.
 */
public class Player {

    private final Vector3 position;
    private final float speed;

    private float velocityX;
    private float velocityZ;

    private static final float ACCELERATION = 18f;
    private static final float DECELERATION = 22f;
    private static final float VELOCITY_EPSILON = 0.01f;
    private static final float COLLISION_RADIUS = 0.6f;

    private static final float MIN_X = -28f;
    private static final float MAX_X = 28f;
    private static final float MIN_Z = -18f;
    private static final float MAX_Z = 18f;

    private boolean moving;

    private final Vector3 movementDirection = new Vector3(0f, 0f, -1f);
    private final Array<Rectangle> collisionObjects = new Array<>();

    public Player(float startX, float startY, float startZ, float speed) {
        position = new Vector3(startX, startY, startZ);
        this.speed = speed;
        clampToWorld();
    }

    public void addCollision(float x, float z, float width, float depth) {
        collisionObjects.add(new Rectangle(x, z, width, depth));
    }

    public void clearCollisions() {
        collisionObjects.clear();
    }

    /**
     * Updates movement using horizontal camera-relative forward/right vectors.
     */
    public void update(float delta, Vector3 cameraForward, Vector3 cameraRight) {
        delta = Math.min(Math.max(delta, 0f), 0.05f);

        float forwardInput = 0f;
        float rightInput = 0f;

        // W/S and A/D are intentionally read only here; they never rotate camera.
        if (com.badlogic.gdx.Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.W)) forwardInput += 1f;
        if (com.badlogic.gdx.Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.S)) forwardInput -= 1f;
        if (com.badlogic.gdx.Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.D)) rightInput += 1f;
        if (com.badlogic.gdx.Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.A)) rightInput -= 1f;

        Vector3 move = new Vector3();
        move.mulAdd(cameraForward, forwardInput);
        move.mulAdd(cameraRight, rightInput);
        move.y = 0f;

        if (move.len2() > 0f) move.nor();

        moving = move.len2() > 0f;

        float targetX = move.x * speed;
        float targetZ = move.z * speed;

        if (moving) {
            velocityX = moveTowards(velocityX, targetX, ACCELERATION * delta);
            velocityZ = moveTowards(velocityZ, targetZ, ACCELERATION * delta);
            movementDirection.set(move).nor();
        } else {
            velocityX = moveTowards(velocityX, 0f, DECELERATION * delta);
            velocityZ = moveTowards(velocityZ, 0f, DECELERATION * delta);
        }

        tryMoveX(velocityX * delta);
        tryMoveZ(velocityZ * delta);
        clampToWorld();

        if (Math.abs(velocityX) < VELOCITY_EPSILON) velocityX = 0f;
        if (Math.abs(velocityZ) < VELOCITY_EPSILON) velocityZ = 0f;
    }

    /** Backward-compatible overload for callers that do not provide camera vectors. */
    public void update(float delta) {
        update(delta, new Vector3(0f, 0f, -1f), new Vector3(1f, 0f, 0f));
    }

    private float moveTowards(float current, float target, float maxDelta) {
        if (Math.abs(target - current) <= maxDelta) return target;
        return current < target ? current + maxDelta : current - maxDelta;
    }

    private void tryMoveX(float amount) {
        if (amount == 0f) return;
        float newX = position.x + amount;
        if (!collides(newX, position.z)) position.x = newX;
        else velocityX = 0f;
    }

    private void tryMoveZ(float amount) {
        if (amount == 0f) return;
        float newZ = position.z + amount;
        if (!collides(position.x, newZ)) position.z = newZ;
        else velocityZ = 0f;
    }

    private boolean collides(float x, float z) {
        Rectangle bounds = new Rectangle(
            x - COLLISION_RADIUS,
            z - COLLISION_RADIUS,
            COLLISION_RADIUS * 2f,
            COLLISION_RADIUS * 2f
        );
        for (Rectangle obstacle : collisionObjects) {
            if (bounds.overlaps(obstacle)) return true;
        }
        return false;
    }

    private void clampToWorld() {
        if (position.x < MIN_X) { position.x = MIN_X; velocityX = 0f; }
        if (position.x > MAX_X) { position.x = MAX_X; velocityX = 0f; }
        if (position.z < MIN_Z) { position.z = MIN_Z; velocityZ = 0f; }
        if (position.z > MAX_Z) { position.z = MAX_Z; velocityZ = 0f; }
    }

    public Vector3 getPosition() { return position; }
    public float getX() { return position.x; }
    public float getY() { return position.y; }
    public float getZ() { return position.z; }
    public boolean isMoving() { return moving; }
    public float getVelocityX() { return velocityX; }
    public float getVelocityZ() { return velocityZ; }
    public Vector3 getMovementDirection() { return movementDirection; }
    public float getCurrentSpeed() { return (float)Math.sqrt(velocityX * velocityX + velocityZ * velocityZ); }
}
