package bd.historicalgame.game;

/**
 * Controls the high-level state of 2x12.
 */
public class GameManager {

    private GameState currentState;

    public GameManager() {
        currentState = GameState.MAIN_MENU;
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public void setState(GameState state) {
        if (state == null) {
            throw new IllegalArgumentException("Game state cannot be null.");
        }

        currentState = state;
    }

    public boolean isPlaying() {
        return currentState == GameState.PLAYING;
    }

    public boolean isPaused() {
        return currentState == GameState.PAUSED;
    }
}
