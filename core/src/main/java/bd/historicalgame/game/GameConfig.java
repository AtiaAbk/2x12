package bd.historicalgame.game;

/**
 * Global configuration for 2x12.
 */
public final class GameConfig {

    public static final String GAME_NAME = "2x12";
    public static final String VERSION = "0.1.0";

    // =====================================================
    // WINDOW / UI
    // =====================================================

    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    // =====================================================
    // WORLD
    // =====================================================

    public static final int WORLD_WIDTH = 3200;
    public static final int WORLD_HEIGHT = 1800;

    // =====================================================
    // PLAYER
    // =====================================================

    /*
     * The Level 1 world uses relatively small 3D units,
     * so 8 units/second is appropriate.
     */
    public static final float PLAYER_SPEED = 8f;

    private GameConfig() {
        // Utility class.
    }
}