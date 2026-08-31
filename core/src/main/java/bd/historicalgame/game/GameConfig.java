package bd.historicalgame.game;

/**
 * Global configuration for 2x12.
 */
public final class GameConfig {

    // =========================================================
    // GAME
    // =========================================================

    public static final String GAME_NAME = "2x12";
    public static final String VERSION = "0.1.0";


    // =========================================================
    // WINDOW / SCREEN
    // =========================================================

    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;


    // =========================================================
    // WORLD
    // =========================================================

    public static final int WORLD_WIDTH = 3200;
    public static final int WORLD_HEIGHT = 1800;


    // =========================================================
    // PLAYER
    // =========================================================

    /*
     * Player movement speed.
     */
    public static final float PLAYER_SPEED = 8f;


    // =========================================================
    // MISSION
    // =========================================================

    /*
     * Distance required to reach an objective.
     *
     * When the player comes within this distance
     * of the mission target, the mission completes.
     */
    public static final float MISSION_COMPLETE_DISTANCE = 2.5f;


    // =========================================================
    // LEVEL 1
    // =========================================================

    /*
     * Level 1 playable boundaries.
     */
    public static final float LEVEL1_MIN_X = -28f;
    public static final float LEVEL1_MAX_X = 28f;

    public static final float LEVEL1_MIN_Z = -18f;
    public static final float LEVEL1_MAX_Z = 18f;


    // =========================================================
    // PRIVATE CONSTRUCTOR
    // =========================================================

    private GameConfig() {

        // Utility class.
    }
}