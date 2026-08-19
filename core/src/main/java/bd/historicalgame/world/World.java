package bd.historicalgame.world;

import bd.historicalgame.game.GameConfig;
import bd.historicalgame.player.Player;

/**
 * Represents the playable world of 2x12.
 */
public class World {

    private final Player player;

    public World() {
        player = new Player(
                GameConfig.WORLD_WIDTH / 2f,
                GameConfig.WORLD_HEIGHT / 2f
        );
    }

    public void update(float delta) {
        player.update(delta);
    }

    public Player getPlayer() {
        return player;
    }
}
