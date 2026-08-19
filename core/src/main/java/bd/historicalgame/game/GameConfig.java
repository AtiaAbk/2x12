package bd.historicalgame.game;

/**
 * Global configuration for 2x12.
 */
public final class GameConfig {

    public static final String GAME_NAME = "2x12";
    public static final String VERSION = "0.1.0";

    // Window
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    // World
    public static final int WORLD_WIDTH = 3200;
    public static final int WORLD_HEIGHT = 1800;

    // Player
    public static final float PLAYER_SPEED = 220f;

    private GameConfig() {
        // Utility class.
    }
}
