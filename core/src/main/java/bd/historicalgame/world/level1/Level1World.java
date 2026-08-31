package bd.historicalgame.world.level1;

import bd.historicalgame.game.GameConfig;
import bd.historicalgame.player.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Level 1 world for the 2x12 historical adventure.
 *
 * Includes:
 * - 3D environment
 * - Lighting
 * - Ground
 * - Main building
 * - Classrooms
 * - Courtyard
 * - Gate
 * - 3D player
 * - Player movement
 * - Third-person camera
 */
public class Level1World implements Disposable {

    // =========================================================
    // RENDERING
    // =========================================================

    private final ModelBatch modelBatch;
    private final Environment environment;
    private final Array<ModelInstance> instances;
    private final Array<Model> models;

    // =========================================================
    // CAMERA
    // =========================================================

    private PerspectiveCamera camera;

    /*
     * Temporary vector used for camera calculations.
     */
    private final Vector3 cameraOffset = new Vector3(
        0f,
        8f,
        14f
    );

    // =========================================================
    // PLAYER
    // =========================================================

    private Player player;
    private Model playerModel;
    private ModelInstance playerInstance;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public Level1World() {

        modelBatch = new ModelBatch();

        environment = new Environment();

        instances = new Array<>();
        models = new Array<>();

        createLighting();
        createCamera();
        createPlayer();
        createWorld();
    }

    // =========================================================
    // LIGHTING
    // =========================================================

    private void createLighting() {

        /*
         * Ambient light.
         */
        environment.set(
            ColorAttribute.createAmbient(
                0.65f,
                0.65f,
                0.65f,
                1f
            )
        );

        /*
         * Directional sunlight.
         */
        environment.add(
            new DirectionalLight().set(
                1.0f,
                0.95f,
                0.85f,
                -1.0f,
                -0.8f,
                -0.5f
            )
        );
    }

    // =========================================================
    // CAMERA
    // =========================================================

    private void createCamera() {

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        if (width <= 0) {
            width = GameConfig.WIDTH;
        }

        if (height <= 0) {
            height = GameConfig.HEIGHT;
        }

        camera = new PerspectiveCamera(
            67f,
            width,
            height
        );

        /*
         * Initial camera position.
         */
        camera.position.set(
            0f,
            8f,
            22f
        );

        camera.near = 0.1f;
        camera.far = 500f;

        camera.lookAt(
            0f,
            2f,
            0f
        );

        camera.update();
    }

    // =========================================================
    // PLAYER
    // =========================================================

    private void createPlayer() {

        /*
         * Player starts near the Level 1 gate/courtyard.
         *
         * X = 0
         * Y = 1
         * Z = 10
         */
        player = new Player(
            0f,
            1f,
            10f,
            GameConfig.PLAYER_SPEED
        );

        ModelBuilder builder =
            new ModelBuilder();

        long attributes =
            VertexAttributes.Usage.Position |
            VertexAttributes.Usage.Normal;

        /*
         * Temporary player body.
         *
         * This is only a placeholder.
         * Later we will replace it with
         * the actual character model.
         */
        playerModel =
            builder.createBox(
                1.2f,
                2f,
                1.2f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf("C98B5A")
                        )
                    }
                ),
                attributes
            );

        models.add(playerModel);

        playerInstance =
            new ModelInstance(playerModel);

        /*
         * Position the player.
         */
        updatePlayerModel();

        instances.add(playerInstance);
    }

    // =========================================================
    // PLAYER MODEL UPDATE
    // =========================================================

    private void updatePlayerModel() {

        if (player == null ||
            playerInstance == null) {
            return;
        }

        /*
         * Player Y position represents the
         * center of the body.
         */
        playerInstance.transform.setToTranslation(
            player.getX(),
            player.getY(),
            player.getZ()
        );
    }

    // =========================================================
    // CAMERA FOLLOW
    // =========================================================

    private void updateCamera() {

        if (camera == null ||
            player == null) {
            return;
        }

        /*
         * Camera follows behind the player.
         */
        camera.position.set(
            player.getX() + cameraOffset.x,
            player.getY() + cameraOffset.y,
            player.getZ() + cameraOffset.z
        );

        /*
         * Look slightly above player's center.
         */
        camera.lookAt(
            player.getX(),
            player.getY() + 0.8f,
            player.getZ()
        );

        camera.update();
    }

    // =========================================================
    // WORLD
    // =========================================================

    private void createWorld() {

        ModelBuilder builder =
            new ModelBuilder();

        long attributes =
            VertexAttributes.Usage.Position |
            VertexAttributes.Usage.Normal;

        createGround(builder, attributes);

        createMainBuilding(builder, attributes);

        createClassrooms(builder, attributes);

        createCourtyard(builder, attributes);

        createGate(builder, attributes);
    }

    // =========================================================
    // GROUND
    // =========================================================

    private void createGround(
        ModelBuilder builder,
        long attributes
    ) {

        Model groundModel =
            builder.createBox(
                60f,
                0.4f,
                40f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf("4B6B3C")
                        )
                    }
                ),
                attributes
            );

        models.add(groundModel);

        ModelInstance ground =
            new ModelInstance(groundModel);

        ground.transform.setToTranslation(
            0f,
            -0.2f,
            0f
        );

        instances.add(ground);
    }

    // =========================================================
    // MAIN BUILDING
    // =========================================================

    private void createMainBuilding(
        ModelBuilder builder,
        long attributes
    ) {

        Model buildingModel =
            builder.createBox(
                18f,
                7f,
                8f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf("D8C7A3")
                        )
                    }
                ),
                attributes
            );

        models.add(buildingModel);

        ModelInstance building =
            new ModelInstance(buildingModel);

        building.transform.setToTranslation(
            0f,
            3.5f,
            -10f
        );

        instances.add(building);
    }

    // =========================================================
    // CLASSROOMS
    // =========================================================

    private void createClassrooms(
        ModelBuilder builder,
        long attributes
    ) {

        Model classroomModel =
            builder.createBox(
                12f,
                5f,
                7f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf("B8A47A")
                        )
                    }
                ),
                attributes
            );

        models.add(classroomModel);

        /*
         * Left classroom.
         */
        ModelInstance leftClassroom =
            new ModelInstance(classroomModel);

        leftClassroom.transform.setToTranslation(
            -17f,
            2.5f,
            -3f
        );

        instances.add(leftClassroom);

        /*
         * Right classroom.
         */
        ModelInstance rightClassroom =
            new ModelInstance(classroomModel);

        rightClassroom.transform.setToTranslation(
            17f,
            2.5f,
            -3f
        );

        instances.add(rightClassroom);
    }

    // =========================================================
    // COURTYARD
    // =========================================================

    private void createCourtyard(
        ModelBuilder builder,
        long attributes
    ) {

        Model courtyardModel =
            builder.createBox(
                18f,
                0.5f,
                14f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf("8E8068")
                        )
                    }
                ),
                attributes
            );

        models.add(courtyardModel);

        ModelInstance courtyard =
            new ModelInstance(courtyardModel);

        courtyard.transform.setToTranslation(
            0f,
            0.25f,
            5f
        );

        instances.add(courtyard);
    }

    // =========================================================
    // GATE
    // =========================================================

    private void createGate(
        ModelBuilder builder,
        long attributes
    ) {

        Model pillarModel =
            builder.createBox(
                2f,
                8f,
                2f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf("C5B28A")
                        )
                    }
                ),
                attributes
            );

        models.add(pillarModel);

        /*
         * Left gate pillar.
         */
        ModelInstance leftPillar =
            new ModelInstance(pillarModel);

        leftPillar.transform.setToTranslation(
            -12f,
            4f,
            15f
        );

        instances.add(leftPillar);

        /*
         * Right gate pillar.
         */
        ModelInstance rightPillar =
            new ModelInstance(pillarModel);

        rightPillar.transform.setToTranslation(
            12f,
            4f,
            15f
        );

        instances.add(rightPillar);
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public void update(float delta) {

        /*
         * Update player movement.
         */
        if (player != null) {
            player.update(delta);
        }

        /*
         * Synchronize visible player model.
         */
        updatePlayerModel();

        /*
         * Follow player.
         */
        updateCamera();
    }

    // =========================================================
    // RENDER
    // =========================================================

    public void render() {

        int width =
            Gdx.graphics.getWidth();

        int height =
            Gdx.graphics.getHeight();

        if (height <= 0) {
            height = 1;
        }

        Gdx.gl.glViewport(
            0,
            0,
            width,
            height
        );

        /*
         * Enable depth testing.
         */
        Gdx.gl.glEnable(
            GL20.GL_DEPTH_TEST
        );

        /*
         * Background / sky color.
         */
        Gdx.gl.glClearColor(
            0.08f,
            0.12f,
            0.16f,
            1f
        );

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT |
            GL20.GL_DEPTH_BUFFER_BIT
        );

        /*
         * Render 3D world.
         */
        modelBatch.begin(camera);

        modelBatch.render(
            instances,
            environment
        );

        modelBatch.end();
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public PerspectiveCamera getCamera() {
        return camera;
    }

    public Player getPlayer() {
        return player;
    }

    // =========================================================
    // DISPOSE
    // =========================================================

    @Override
    public void dispose() {

        if (modelBatch != null) {
            modelBatch.dispose();
        }

        for (Model model : models) {

            if (model != null) {
                model.dispose();
            }
        }

        models.clear();
        instances.clear();

        player = null;
        playerInstance = null;
        playerModel = null;
    }
}