package bd.historicalgame.world;

import bd.historicalgame.world.level1.Level1World;

import com.badlogic.gdx.utils.Disposable;

/**
 * World controller for 2x12.
 */
public class World implements Disposable {

    private Level1World level1World;

    public World() {
        level1World = new Level1World();
    }

    public void update(float delta) {
        level1World.update(delta);
    }

    public void render() {
        level1World.render();
    }

    public Level1World getLevel1World() {
        return level1World;
    }

    @Override
    public void dispose() {

        if (level1World != null) {
            level1World.dispose();
        }
    }
}
