package bd.historicalgame.world.level1;

import bd.historicalgame.game.GameConfig;
import bd.historicalgame.player.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class Level1World {

    private final ModelBatch modelBatch;
    private final Environment environment;
    private final PerspectiveCamera camera;
    private final Player player;

    private final ModelBuilder modelBuilder;

    private final List<Model> models = new ArrayList<>();
    private final List<ModelInstance> worldObjects = new ArrayList<>();

    private Model groundModel;
    private Model roadModel;
    private Model pathModel;
    private Model treeTrunkModel;
    private Model treeLeafModel;

    public Level1World() {

        modelBatch = new ModelBatch();

        modelBuilder = new ModelBuilder();

        environment = new Environment();

        /*
         * Soft daylight.
         */
        environment.set(
                ColorAttribute.createAmbientLight(
                        0.55f,
                        0.55f,
                        0.55f,
                        1.0f
                )
        );

        environment.add(
                new DirectionalLight().set(
                        0.85f,
                        0.82f,
                        0.72f,
                        -0.55f,
                        -1.0f,
                        -0.35f
                )
        );

        /*
         * Camera.
         */
        camera = new PerspectiveCamera(
                GameConfig.FOV,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
        );

        camera.near = 0.1f;
        camera.far = 500f;

        /*
         * Player starts near the central campus area.
         */
        player = new Player(
                0f,
                1.0f,
                12f,
                6.0f
        );

        /*
         * Create world.
         */
        createMaterials();

        createGround();

        createMainRoads();

        createSecondaryPaths();

        createCampusOpenAreas();

        createTrees();

        /*
         * Initial camera position.
         */
        updateCamera(0f);
    }

    private void createMaterials() {

        /*
         * Ground.
         */
        groundMaterial = new Material(
                ColorAttribute.createDiffuse(
                        new Color(0.32f, 0.48f, 0.25f, 1f)
                )
        );

        /*
         * Road.
         */
        roadMaterial = new Material(
                ColorAttribute.createDiffuse(
                        new Color(0.20f, 0.20f, 0.18f, 1f)
                )
        );

        /*
         * Path.
         */
        pathMaterial = new Material(
                ColorAttribute.createDiffuse(
                        new Color(0.58f, 0.54f, 0.45f, 1f)
                )
        );

        /*
         * Tree trunk.
         */
        trunkMaterial = new Material(
                ColorAttribute.createDiffuse(
                        new Color(0.30f, 0.18f, 0.09f, 1f)
                )
        );

        /*
         * Tree leaves.
         */
        leafMaterial = new Material(
                ColorAttribute.createDiffuse(
                        new Color(0.18f, 0.42f, 0.16f, 1f)
                )
        );
    }

    private Material groundMaterial;
    private Material roadMaterial;
    private Material pathMaterial;
    private Material trunkMaterial;
    private Material leafMaterial;

    private void createGround() {

        groundModel = modelBuilder.createBox(
                240f,
                0.4f,
                220f,
                groundMaterial,
                VertexAttributes.Usage.Position |
                        VertexAttributes.Usage.Normal
        );

        models.add(groundModel);

        ModelInstance ground =
                new ModelInstance(groundModel);

        ground.transform.setToTranslation(
                0f,
                -0.2f,
                0f
        );

        worldObjects.add(ground);
    }

    private void createMainRoads() {

        /*
         * Main east-west road.
         */
        createRoad(
                0f,
                0.02f,
                0f,
                150f,
                8f
        );

        /*
         * Main north-south road.
         */
        createRoad(
                0f,
                0.03f,
                0f,
                8f,
                150f
        );

        /*
         * Secondary connecting road.
         */
        createRoad(
                42f,
                0.04f,
                15f,
                55f,
                6f
        );

        /*
         * Another connecting road.
         */
        createRoad(
                -45f,
                0.04f,
                -18f,
                60f,
                6f
        );
    }

    private void createRoad(
            float x,
            float y,
            float z,
            float width,
            float depth
    ) {

        Model model = modelBuilder.createBox(
                width,
                0.12f,
                depth,
                roadMaterial,
                VertexAttributes.Usage.Position |
                        VertexAttributes.Usage.Normal
        );

        models.add(model);

        ModelInstance instance =
                new ModelInstance(model);

        instance.transform.setToTranslation(
                x,
                y,
                z
        );

        worldObjects.add(instance);
    }

    private void createSecondaryPaths() {

        /*
         * Path toward open campus area.
         */
        createPath(
                -28f,
                0.08f,
                25f,
                55f,
                3.5f
        );

        createPath(
                30f,
                0.08f,
                -28f,
                3.5f,
                55f
        );

        createPath(
                -55f,
                0.08f,
                -42f,
                50f,
                3.5f
        );

        createPath(
                55f,
                0.08f,
                45f,
                55f,
                3.5f
        );
    }

    private void createPath(
            float x,
            float y,
            float z,
            float width,
            float depth
    ) {

        Model model = modelBuilder.createBox(
                width,
                0.08f,
                depth,
                pathMaterial,
                VertexAttributes.Usage.Position |
                        VertexAttributes.Usage.Normal
        );

        models.add(model);

        ModelInstance instance =
                new ModelInstance(model);

        instance.transform.setToTranslation(
                x,
                y,
                z
        );

        worldObjects.add(instance);
    }

    private void createCampusOpenAreas() {

        /*
         * Central open chottor.
         */
        createOpenArea(
                0f,
                0.06f,
                0f,
                38f,
                30f
        );

        /*
         * East open area.
         */
        createOpenArea(
                55f,
                0.06f,
                20f,
                28f,
                25f
        );

        /*
         * West open area.
         */
        createOpenArea(
                -58f,
                0.06f,
                25f,
                30f,
                25f
        );
    }

    private void createOpenArea(
            float x,
            float y,
            float z,
            float width,
            float depth
    ) {

        Model model = modelBuilder.createBox(
                width,
                0.05f,
                depth,
                new Material(
                        ColorAttribute.createDiffuse(
                                new Color(
                                        0.38f,
                                        0.52f,
                                        0.29f,
                                        1f
                                )
                        )
                ),
                VertexAttributes.Usage.Position |
                        VertexAttributes.Usage.Normal
        );

        models.add(model);

        ModelInstance instance =
                new ModelInstance(model);

        instance.transform.setToTranslation(
                x,
                y,
                z
        );

        worldObjects.add(instance);
    }

    private void createTrees() {

        /*
         * Central campus trees.
         */
        addTree(-18f, 0f, -14f, 1.0f);
        addTree(18f, 0f, -14f, 1.2f);
        addTree(-22f, 0f, 16f, 1.1f);
        addTree(22f, 0f, 17f, 0.9f);

        /*
         * East side.
         */
        addTree(46f, 0f, 8f, 1.3f);
        addTree(58f, 0f, 38f, 1.0f);
        addTree(70f, 0f, 12f, 1.2f);
        addTree(45f, 0f, 45f, 0.9f);

        /*
         * West side.
         */
        addTree(-48f, 0f, 8f, 1.0f);
        addTree(-68f, 0f, 30f, 1.2f);
        addTree(-72f, 0f, -20f, 0.9f);
        addTree(-45f, 0f, -40f, 1.3f);

        /*
         * Background vegetation.
         */
        addTree(-85f, 0f, 60f, 1.5f);
        addTree(-55f, 0f, 70f, 1.2f);
        addTree(50f, 0f, 70f, 1.4f);
        addTree(85f, 0f, 55f, 1.6f);
    }

    private void addTree(
            float x,
            float y,
            float z,
            float scale
    ) {

        if (treeTrunkModel == null) {

            treeTrunkModel = modelBuilder.createCylinder(
                    0.45f,
                    4.0f,
                    0.45f,
                    8,
                    trunkMaterial,
                    VertexAttributes.Usage.Position |
                            VertexAttributes.Usage.Normal
            );

            models.add(treeTrunkModel);
        }

        if (treeLeafModel == null) {

            treeLeafModel = modelBuilder.createSphere(
                    4.5f,
                    4.5f,
                    4.5f,
                    12,
                    8,
                    leafMaterial,
                    VertexAttributes.Usage.Position |
                            VertexAttributes.Usage.Normal
            );

            models.add(treeLeafModel);
        }

        ModelInstance trunk =
                new ModelInstance(treeTrunkModel);

        trunk.transform.setToTranslation(
                x,
                2f * scale,
                z
        );

        trunk.transform.scale(
                scale,
                scale,
                scale
        );

        worldObjects.add(trunk);

        ModelInstance leaves =
                new ModelInstance(treeLeafModel);

        leaves.transform.setToTranslation(
                x,
                5.0f * scale,
                z
        );

        leaves.transform.scale(
                scale,
                scale,
                scale
        );

        worldObjects.add(leaves);
    }

    private void updateCamera(float delta) {

        Vector3 playerPosition =
                player.getPosition();

        /*
         * Third-person camera.
         */
        Vector3 desiredPosition =
                new Vector3(
                        playerPosition.x,
                        playerPosition.y + 7.5f,
                        playerPosition.z + 12f
                );

        camera.position.lerp(
                desiredPosition,
                Math.min(1f, delta * 6f)
        );

        camera.lookAt(
                playerPosition.x,
                playerPosition.y + 1.0f,
                playerPosition.z
        );

        camera.up.set(Vector3.Y);

        camera.update();
    }

    public void update(float delta) {

        player.update(delta);

        updateCamera(delta);
    }

    public void render() {

        Gdx.gl.glViewport(
                0,
                0,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
        );

        Gdx.gl.glClearColor(
                0.55f,
                0.70f,
                0.82f,
                1f
        );

        Gdx.gl.glClear(
                GL20.GL_COLOR_BUFFER_BIT |
                        GL20.GL_DEPTH_BUFFER_BIT
        );

        modelBatch.begin(camera);

        for (ModelInstance object : worldObjects) {

            modelBatch.render(
                    object,
                    environment
            );
        }

        modelBatch.end();
    }

    public void resize(
            int width,
            int height
    ) {

        camera.viewportWidth = width;
        camera.viewportHeight = height;

        camera.update();
    }

    public void dispose() {

        modelBatch.dispose();

        for (Model model : models) {
            model.dispose();
        }
    }

    public Player getPlayer() {
        return player;
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }
}