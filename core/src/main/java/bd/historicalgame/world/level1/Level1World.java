package bd.historicalgame.world.level1;

import bd.historicalgame.game.GameConfig;
import bd.historicalgame.player.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Level 1 world for 2x12.
 *
 * Presentation prototype features:
 *
 * - 3D campus
 * - Player
 * - WASD movement
 * - Third-person camera
 * - Building collision
 * - Mission objectives
 * - Objective counter
 * - Level completion
 */
public class Level1World implements Disposable {

    // =====================================================
    // 3D RENDERING
    // =====================================================

    private final ModelBatch modelBatch;
    private final Environment environment;

    private final Array<ModelInstance> instances;
    private final Array<Model> models;

    // =====================================================
    // 2D UI
    // =====================================================

    private final SpriteBatch uiBatch;
    private final BitmapFont font;

    // =====================================================
    // CAMERA
    // =====================================================

    private PerspectiveCamera camera;

    private static final float CAMERA_X = 0f;
    private static final float CAMERA_Y = 7f;
    private static final float CAMERA_Z = 12f;

    // =====================================================
    // PLAYER
    // =====================================================

    private Player player;

    private Model playerModel;
    private ModelInstance playerInstance;

    // =====================================================
    // MISSION
    // =====================================================

    private int objectivesCompleted = 0;

    private static final int TOTAL_OBJECTIVES = 3;

    private boolean courtyardVisited = false;
    private boolean classroomVisited = false;
    private boolean mainBuildingVisited = false;

    private boolean levelComplete = false;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public Level1World() {

        modelBatch = new ModelBatch();

        environment = new Environment();

        instances = new Array<>();
        models = new Array<>();

        uiBatch = new SpriteBatch();
        font = new BitmapFont();

        createLighting();

        createCamera();

        createPlayer();

        createWorld();

        createCollisions();
    }

    // =====================================================
    // LIGHTING
    // =====================================================

    private void createLighting() {

        environment.set(
            ColorAttribute.createAmbient(
                0.65f,
                0.65f,
                0.65f,
                1f
            )
        );

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

    // =====================================================
    // CAMERA
    // =====================================================

    private void createCamera() {

        float width =
            Gdx.graphics.getWidth();

        float height =
            Gdx.graphics.getHeight();

        if (width <= 0) {
            width = GameConfig.WIDTH;
        }

        if (height <= 0) {
            height = GameConfig.HEIGHT;
        }

        camera =
            new PerspectiveCamera(
                67f,
                width,
                height
            );

        camera.near = 0.1f;
        camera.far = 500f;

        camera.position.set(
            0f,
            8f,
            22f
        );

        camera.lookAt(
            0f,
            2f,
            0f
        );

        camera.update();
    }

    // =====================================================
    // PLAYER
    // =====================================================

    private void createPlayer() {

        /*
         * Start near the campus gate.
         */
        player =
            new Player(
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
            new ModelInstance(
                playerModel
            );

        updatePlayerModel();

        instances.add(playerInstance);
    }

    private void updatePlayerModel() {

        if (player == null ||
            playerInstance == null) {
            return;
        }

        playerInstance.transform
            .setToTranslation(
                player.getX(),
                player.getY(),
                player.getZ()
            );
    }

    // =====================================================
    // CAMERA FOLLOW
    // =====================================================

    private void updateCamera() {

        if (player == null) {
            return;
        }

        camera.position.set(
            player.getX() + CAMERA_X,
            player.getY() + CAMERA_Y,
            player.getZ() + CAMERA_Z
        );

        camera.lookAt(
            player.getX(),
            player.getY() + 0.8f,
            player.getZ()
        );

        camera.update();
    }

    // =====================================================
    // WORLD
    // =====================================================

    private void createWorld() {

        ModelBuilder builder =
            new ModelBuilder();

        long attributes =
            VertexAttributes.Usage.Position |
            VertexAttributes.Usage.Normal;

        createGround(
            builder,
            attributes
        );

        createMainBuilding(
            builder,
            attributes
        );

        createClassrooms(
            builder,
            attributes
        );

        createCourtyard(
            builder,
            attributes
        );

        createGate(
            builder,
            attributes
        );
    }

    // =====================================================
    // GROUND
    // =====================================================

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
            new ModelInstance(
                groundModel
            );

        ground.transform.setToTranslation(
            0f,
            -0.2f,
            0f
        );

        instances.add(ground);
    }

    // =====================================================
    // MAIN BUILDING
    // =====================================================

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
            new ModelInstance(
                buildingModel
            );

        building.transform.setToTranslation(
            0f,
            3.5f,
            -10f
        );

        instances.add(building);
    }

    // =====================================================
    // CLASSROOMS
    // =====================================================

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

        ModelInstance leftClassroom =
            new ModelInstance(
                classroomModel
            );

        leftClassroom.transform
            .setToTranslation(
                -17f,
                2.5f,
                -3f
            );

        instances.add(leftClassroom);

        ModelInstance rightClassroom =
            new ModelInstance(
                classroomModel
            );

        rightClassroom.transform
            .setToTranslation(
                17f,
                2.5f,
                -3f
            );

        instances.add(rightClassroom);
    }

    // =====================================================
    // COURTYARD
    // =====================================================

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
            new ModelInstance(
                courtyardModel
            );

        courtyard.transform.setToTranslation(
            0f,
            0.25f,
            5f
        );

        instances.add(courtyard);
    }

    // =====================================================
    // GATE
    // =====================================================

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

        ModelInstance leftPillar =
            new ModelInstance(
                pillarModel
            );

        leftPillar.transform.setToTranslation(
            -12f,
            4f,
            15f
        );

        instances.add(leftPillar);

        ModelInstance rightPillar =
            new ModelInstance(
                pillarModel
            );

        rightPillar.transform.setToTranslation(
            12f,
            4f,
            15f
        );

        instances.add(rightPillar);
    }

    // =====================================================
    // COLLISION
    // =====================================================

    private void createCollisions() {

        /*
         * Main building.
         *
         * Visual:
         * width  = 18
         * depth  = 8
         * center = (0,-10)
         */
        player.addCollision(
            -9f,
            -14f,
            18f,
            8f
        );

        /*
         * Left classroom.
         *
         * width  = 12
         * depth  = 7
         * center = (-17,-3)
         */
        player.addCollision(
            -23f,
            -6.5f,
            12f,
            7f
        );

        /*
         * Right classroom.
         */
        player.addCollision(
            11f,
            -6.5f,
            12f,
            7f
        );

        /*
         * Left gate pillar.
         */
        player.addCollision(
            -13f,
            14f,
            2f,
            2f
        );

        /*
         * Right gate pillar.
         */
        player.addCollision(
            11f,
            14f,
            2f,
            2f
        );
    }

    // =====================================================
    // MISSION
    // =====================================================

    private void updateMission() {

        if (player == null ||
            levelComplete) {
            return;
        }

        float x = player.getX();
        float z = player.getZ();

        /*
         * OBJECTIVE 1
         *
         * Visit courtyard.
         */
        if (!courtyardVisited) {

            if (distance(
                x,
                z,
                0f,
                5f
            ) < 6f) {

                courtyardVisited = true;
                objectivesCompleted++;

                System.out.println(
                    "Objective 1 complete: Courtyard visited."
                );
            }
        }

        /*
         * OBJECTIVE 2
         *
         * Visit left classroom.
         */
        if (!classroomVisited) {

            if (distance(
                x,
                z,
                -17f,
                -3f
            ) < 6f) {

                classroomVisited = true;
                objectivesCompleted++;

                System.out.println(
                    "Objective 2 complete: Classroom visited."
                );
            }
        }

        /*
         * OBJECTIVE 3
         *
         * Reach main building.
         */
        if (!mainBuildingVisited) {

            if (distance(
                x,
                z,
                0f,
                -5f
            ) < 4f) {

                mainBuildingVisited = true;
                objectivesCompleted++;

                System.out.println(
                    "Objective 3 complete: Main building reached."
                );
            }
        }

        /*
         * All objectives completed.
         */
        if (objectivesCompleted >=
            TOTAL_OBJECTIVES) {

            levelComplete = true;

            System.out.println(
                "================================"
            );

            System.out.println(
                "LEVEL 1 COMPLETE"
            );

            System.out.println(
                "================================"
            );
        }
    }

    private float distance(
        float x1,
        float z1,
        float x2,
        float z2
    ) {

        float dx = x1 - x2;
        float dz = z1 - z2;

        return (float) Math.sqrt(
            dx * dx +
            dz * dz
        );
    }

    // =====================================================
    // UPDATE
    // =====================================================

    public void update(float delta) {

        /*
         * Do not move after completion.
         */
        if (!levelComplete) {

            player.update(delta);
        }

        updatePlayerModel();

        updateCamera();

        updateMission();
    }

    // =====================================================
    // RENDER
    // =====================================================

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

        Gdx.gl.glEnable(
            GL20.GL_DEPTH_TEST
        );

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
         * 3D world.
         */
        modelBatch.begin(camera);

        modelBatch.render(
            instances,
            environment
        );

        modelBatch.end();

        /*
         * 2D HUD.
         */
        renderHUD();
    }

    // =====================================================
    // HUD
    // =====================================================

    private void renderHUD() {

        uiBatch.begin();

        font.getData().setScale(1.5f);

        font.draw(
            uiBatch,
            "LEVEL 1",
            30f,
            Gdx.graphics.getHeight() - 30f
        );

        font.getData().setScale(1.1f);

        font.draw(
            uiBatch,
            "Objective",
            30f,
            Gdx.graphics.getHeight() - 65f
        );

        font.draw(
            uiBatch,
            objectivesCompleted +
            "/" +
            TOTAL_OBJECTIVES +
            " locations discovered",
            30f,
            Gdx.graphics.getHeight() - 95f
        );

        font.getData().setScale(1f);

        if (!courtyardVisited) {

            font.draw(
                uiBatch,
                "1. Explore the courtyard",
                30f,
                Gdx.graphics.getHeight() - 130f
            );

        } else {

            font.draw(
                uiBatch,
                "1. Courtyard ✓",
                30f,
                Gdx.graphics.getHeight() - 130f
            );
        }

        if (!classroomVisited) {

            font.draw(
                uiBatch,
                "2. Visit the classroom",
                30f,
                Gdx.graphics.getHeight() - 155f
            );

        } else {

            font.draw(
                uiBatch,
                "2. Classroom ✓",
                30f,
                Gdx.graphics.getHeight() - 155f
            );
        }

        if (!mainBuildingVisited) {

            font.draw(
                uiBatch,
                "3. Reach the main building",
                30f,
                Gdx.graphics.getHeight() - 180f
            );

        } else {

            font.draw(
                uiBatch,
                "3. Main building ✓",
                30f,
                Gdx.graphics.getHeight() - 180f
            );
        }

        /*
         * Controls.
         */
        font.draw(
            uiBatch,
            "W A S D  Move",
            30f,
            45f
        );

        font.draw(
            uiBatch,
            "ESC  Pause",
            30f,
            22f
        );

        /*
         * Completion screen.
         */
        if (levelComplete) {

            font.getData().setScale(3f);

            font.draw(
                uiBatch,
                "LEVEL 1 COMPLETE!",
                Gdx.graphics.getWidth() / 2f - 190f,
                Gdx.graphics.getHeight() / 2f + 50f
            );

            font.getData().setScale(1.3f);

            font.draw(
                uiBatch,
                "All locations discovered.",
                Gdx.graphics.getWidth() / 2f - 110f,
                Gdx.graphics.getHeight() / 2f
            );

            font.draw(
                uiBatch,
                "Press ENTER to continue",
                Gdx.graphics.getWidth() / 2f - 120f,
                Gdx.graphics.getHeight() / 2f - 50f
            );
        }

        uiBatch.end();
    }

    // =====================================================
    // GETTERS
    // =====================================================

    public PerspectiveCamera getCamera() {
        return camera;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isLevelComplete() {
        return levelComplete;
    }

    public int getObjectivesCompleted() {
        return objectivesCompleted;
    }

    // =====================================================
    // DISPOSE
    // =====================================================

    @Override
    public void dispose() {

        modelBatch.dispose();

        uiBatch.dispose();

        font.dispose();

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