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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Level 1 World
 *
 * Dhaka University inspired historical campus environment.
 *
 * Features:
 * - 3D campus
 * - Smooth third-person camera
 * - Player movement
 * - Buildings
 * - Roads
 * - Courtyard
 * - Gate
 * - Trees
 * - Lamps
 * - Mission system
 * - Objective marker
 * - Direction indicator
 * - Distance indicator
 * - Mini-map
 * - Level completion
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
    // HUD
    // =========================================================

    private final SpriteBatch spriteBatch;
    private final BitmapFont font;

    // =========================================================
    // CAMERA
    // =========================================================

    private PerspectiveCamera camera;

    private final Vector3 desiredCameraPosition =
        new Vector3();

    private final Vector3 cameraLookAt =
        new Vector3();

    /*
     * Camera distance and height.
     */
    private static final float CAMERA_DISTANCE = 18f;
    private static final float CAMERA_HEIGHT = 8f;

    /*
     * Camera smoothing.
     */
    private static final float CAMERA_SMOOTHNESS = 5f;

    // =========================================================
    // PLAYER
    // =========================================================

    private Player player;

    private Model playerModel;
    private ModelInstance playerInstance;

    // =========================================================
    // OBJECTIVE
    // =========================================================

    private Model objectiveMarkerModel;
    private ModelInstance objectiveMarkerInstance;

    private int missionIndex = 0;

    private boolean levelCompleted = false;

    private float missionCompleteTimer = 0f;

    private static final float OBJECTIVE_DISTANCE = 3.5f;

    private final Vector3[] missionTargets = {

        /*
         * Mission 1:
         * Entrance/front of Main Building.
         */
        new Vector3(
            0f,
            1f,
            -4.5f
        ),

        /*
         * Mission 2:
         * Central courtyard.
         */
        new Vector3(
            0f,
            1f,
            5f
        ),

        /*
         * Mission 3:
         * Central investigation point.
         */
        new Vector3(
            0f,
            1f,
            0f
        )
    };

    private final String[] missionTitles = {

        "MAIN BUILDING",

        "COURTYARD",

        "FIND THE NOTICE"
    };

    private final String[] missionDescriptions = {

        "Reach the Main Building.",

        "Investigate the central courtyard.",

        "Find the notice and investigate it."
    };

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public Level1World() {

        modelBatch =
            new ModelBatch();

        environment =
            new Environment();

        instances =
            new Array<>();

        models =
            new Array<>();

        spriteBatch =
            new SpriteBatch();

        font =
            new BitmapFont();

        createLighting();

        createCamera();

        createPlayer();

        createWorld();

        createObjectiveMarker();
    }

    // =========================================================
    // LIGHTING
    // =========================================================

    private void createLighting() {

        /*
         * Soft ambient daylight.
         */
        environment.set(
            ColorAttribute.createAmbient(
                0.72f,
                0.70f,
                0.64f,
                1f
            )
        );

        /*
         * Warm sunlight.
         */
        environment.add(
            new DirectionalLight().set(
                1.0f,
                0.92f,
                0.78f,
                -0.7f,
                -1.0f,
                -0.4f
            )
        );
    }

    // =========================================================
    // CAMERA
    // =========================================================

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

        camera.position.set(
            0f,
            CAMERA_HEIGHT,
            28f
        );

        camera.near = 0.1f;

        camera.far = 500f;

        camera.lookAt(
            0f,
            1.5f,
            0f
        );

        camera.update();
    }

    // =========================================================
    // PLAYER
    // =========================================================

    private void createPlayer() {

        player =
            new Player(
                0f,
                1f,
                12f,
                GameConfig.PLAYER_SPEED
            );

        /*
         * Building collision areas.
         */
        player.addCollision(
            -9f,
            -14f,
            18f,
            8f
        );

        player.addCollision(
            -23f,
            -6.5f,
            12f,
            7f
        );

        player.addCollision(
            11f,
            -6.5f,
            12f,
            7f
        );

        /*
         * Gate pillars.
         */
        player.addCollision(
            -13f,
            14f,
            2f,
            2f
        );

        player.addCollision(
            11f,
            14f,
            2f,
            2f
        );

        ModelBuilder builder =
            new ModelBuilder();

        long attributes =
            VertexAttributes.Usage.Position |
            VertexAttributes.Usage.Normal;

        /*
         * Temporary character.
         */
        playerModel =
            builder.createBox(
                1.2f,
                2f,
                1.2f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "B96F45"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            playerModel
        );

        playerInstance =
            new ModelInstance(
                playerModel
            );

        updatePlayerModel();

        instances.add(
            playerInstance
        );
    }

    // =========================================================
    // PLAYER MODEL
    // =========================================================

    private void updatePlayerModel() {

        if (
            player == null ||
            playerInstance == null
        ) {
            return;
        }

        playerInstance.transform
            .setToTranslation(
                player.getX(),
                player.getY(),
                player.getZ()
            );
    }

    // =========================================================
    // OBJECTIVE MARKER
    // =========================================================

    private void createObjectiveMarker() {

        ModelBuilder builder =
            new ModelBuilder();

        long attributes =
            VertexAttributes.Usage.Position |
            VertexAttributes.Usage.Normal;

        objectiveMarkerModel =
            builder.createCylinder(
                1.0f,
                3.5f,
                1.0f,
                20,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "E7B84B"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            objectiveMarkerModel
        );

        objectiveMarkerInstance =
            new ModelInstance(
                objectiveMarkerModel
            );

        updateObjectiveMarker();

        instances.add(
            objectiveMarkerInstance
        );
    }

    // =========================================================
    // OBJECTIVE MARKER UPDATE
    // =========================================================

    private void updateObjectiveMarker() {

        if (
            objectiveMarkerInstance == null ||
            levelCompleted
        ) {
            return;
        }

        Vector3 target =
            missionTargets[
                missionIndex
            ];

        /*
         * Floating marker above objective.
         */
        float floatingY =
            3.5f +
            MathUtils.sin(
                missionCompleteTimer * 2f
            ) * 0.3f;

        objectiveMarkerInstance
            .transform
            .setToTranslation(
                target.x,
                floatingY,
                target.z
            );
    }

    // =========================================================
    // WORLD CREATION
    // =========================================================

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

        createRoads(
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

        createTrees(
            builder,
            attributes
        );

        createLamps(
            builder,
            attributes
        );

        createCampusWalls(
            builder,
            attributes
        );
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
                70f,
                0.4f,
                50f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "31522A"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            groundModel
        );

        ModelInstance ground =
            new ModelInstance(
                groundModel
            );

        ground.transform.setToTranslation(
            0f,
            -0.2f,
            0f
        );

        instances.add(
            ground
        );
    }

    // =========================================================
    // ROADS
    // =========================================================

    private void createRoads(
        ModelBuilder builder,
        long attributes
    ) {

        Model roadModel =
            builder.createBox(
                20f,
                0.18f,
                35f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "75664C"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            roadModel
        );

        ModelInstance centralRoad =
            new ModelInstance(
                roadModel
            );

        centralRoad.transform
            .setToTranslation(
                0f,
                0.1f,
                0f
            );

        instances.add(
            centralRoad
        );

        /*
         * Cross road.
         */
        Model crossRoadModel =
            builder.createBox(
                55f,
                0.16f,
                7f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "665844"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            crossRoadModel
        );

        ModelInstance crossRoad =
            new ModelInstance(
                crossRoadModel
            );

        crossRoad.transform
            .setToTranslation(
                0f,
                0.09f,
                7f
            );

        instances.add(
            crossRoad
        );
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
                            Color.valueOf(
                                "C9B58D"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            buildingModel
        );

        ModelInstance building =
            new ModelInstance(
                buildingModel
            );

        building.transform
            .setToTranslation(
                0f,
                3.5f,
                -10f
            );

        instances.add(
            building
        );

        /*
         * Entrance platform.
         */
        Model entranceModel =
            builder.createBox(
                10f,
                0.4f,
                3f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "9A8060"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            entranceModel
        );

        ModelInstance entrance =
            new ModelInstance(
                entranceModel
            );

        entrance.transform
            .setToTranslation(
                0f,
                0.2f,
                -5.2f
            );

        instances.add(
            entrance
        );

        /*
         * Entrance pillars.
         */
        Model pillarModel =
            builder.createBox(
                1.1f,
                6f,
                1.1f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "D7C49A"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            pillarModel
        );

        addInstance(
            pillarModel,
            0f,
            3f,
            -5.8f
        );

        addInstance(
            pillarModel,
            -7f,
            3f,
            -5.8f
        );

        addInstance(
            pillarModel,
            7f,
            3f,
            -5.8f
        );
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
                            Color.valueOf(
                                "A99570"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            classroomModel
        );

        ModelInstance left =
            new ModelInstance(
                classroomModel
            );

        left.transform
            .setToTranslation(
                -17f,
                2.5f,
                -3f
            );

        instances.add(
            left
        );

        ModelInstance right =
            new ModelInstance(
                classroomModel
            );

        right.transform
            .setToTranslation(
                17f,
                2.5f,
                -3f
            );

        instances.add(
            right
        );

        /*
         * Back academic buildings.
         */
        Model backModel =
            builder.createBox(
                14f,
                5f,
                6f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "B5A27D"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            backModel
        );

        addInstance(
            backModel,
            -18f,
            2.5f,
            11f
        );

        addInstance(
            backModel,
            18f,
            2.5f,
            11f
        );
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
                0.35f,
                14f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "88775B"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            courtyardModel
        );

        ModelInstance courtyard =
            new ModelInstance(
                courtyardModel
            );

        courtyard.transform
            .setToTranslation(
                0f,
                0.18f,
                5f
            );

        instances.add(
            courtyard
        );
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
                2.2f,
                8f,
                2.2f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "C7B188"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            pillarModel
        );

        addInstance(
            pillarModel,
            -12f,
            4f,
            15f
        );

        addInstance(
            pillarModel,
            12f,
            4f,
            15f
        );

        /*
         * Gate top beam.
         */
        Model gateTopModel =
            builder.createBox(
                26f,
                1.2f,
                1.5f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "9C805B"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            gateTopModel
        );

        addInstance(
            gateTopModel,
            0f,
            7.2f,
            15f
        );
    }

    // =========================================================
    // TREES
    // =========================================================

    private void createTrees(
        ModelBuilder builder,
        long attributes
    ) {

        Model trunkModel =
            builder.createCylinder(
                0.55f,
                3.5f,
                0.55f,
                10,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "63442D"
                            )
                        )
                    }
                ),
                attributes
            );

        Model leavesModel =
            builder.createSphere(
                3.8f,
                3.8f,
                3.8f,
                12,
                8,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "3F7139"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            trunkModel
        );

        models.add(
            leavesModel
        );

        /*
         * Left side.
         */
        addTree(
            trunkModel,
            leavesModel,
            -27f,
            0f,
            -12f
        );

        addTree(
            trunkModel,
            leavesModel,
            -27f,
            0f,
            0f
        );

        addTree(
            trunkModel,
            leavesModel,
            -27f,
            0f,
            12f
        );

        /*
         * Right side.
         */
        addTree(
            trunkModel,
            leavesModel,
            27f,
            0f,
            -12f
        );

        addTree(
            trunkModel,
            leavesModel,
            27f,
            0f,
            0f
        );

        addTree(
            trunkModel,
            leavesModel,
            27f,
            0f,
            12f
        );

        /*
         * Courtyard trees.
         */
        addTree(
            trunkModel,
            leavesModel,
            -9f,
            0f,
            5f
        );

        addTree(
            trunkModel,
            leavesModel,
            9f,
            0f,
            5f
        );

        addTree(
            trunkModel,
            leavesModel,
            -9f,
            0f,
            10f
        );

        addTree(
            trunkModel,
            leavesModel,
            9f,
            0f,
            10f
        );
    }

    // =========================================================
    // TREE HELPER
    // =========================================================

    private void addTree(
        Model trunkModel,
        Model leavesModel,
        float x,
        float y,
        float z
    ) {

        ModelInstance trunk =
            new ModelInstance(
                trunkModel
            );

        trunk.transform
            .setToTranslation(
                x,
                y + 1.75f,
                z
            );

        instances.add(
            trunk
        );

        ModelInstance leaves =
            new ModelInstance(
                leavesModel
            );

        leaves.transform
            .setToTranslation(
                x,
                y + 4.1f,
                z
            );

        instances.add(
            leaves
        );
    }

    // =========================================================
    // LAMPS
    // =========================================================

    private void createLamps(
        ModelBuilder builder,
        long attributes
    ) {

        Model poleModel =
            builder.createCylinder(
                0.18f,
                4f,
                0.18f,
                8,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "393633"
                            )
                        )
                    }
                ),
                attributes
            );

        Model lightModel =
            builder.createSphere(
                0.7f,
                0.7f,
                0.7f,
                8,
                8,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "F2D58A"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            poleModel
        );

        models.add(
            lightModel
        );

        addLamp(
            poleModel,
            lightModel,
            -10f,
            7f
        );

        addLamp(
            poleModel,
            lightModel,
            10f,
            7f
        );

        addLamp(
            poleModel,
            lightModel,
            -10f,
            0f
        );

        addLamp(
            poleModel,
            lightModel,
            10f,
            0f
        );

        addLamp(
            poleModel,
            lightModel,
            -10f,
            14f
        );

        addLamp(
            poleModel,
            lightModel,
            10f,
            14f
        );
    }

    // =========================================================
    // LAMP HELPER
    // =========================================================

    private void addLamp(
        Model poleModel,
        Model lightModel,
        float x,
        float z
    ) {

        ModelInstance pole =
            new ModelInstance(
                poleModel
            );

        pole.transform
            .setToTranslation(
                x,
                2f,
                z
            );

        instances.add(
            pole
        );

        ModelInstance light =
            new ModelInstance(
                lightModel
            );

        light.transform
            .setToTranslation(
                x,
                4.2f,
                z
            );

        instances.add(
            light
        );
    }

    // =========================================================
    // CAMPUS WALLS
    // =========================================================

    private void createCampusWalls(
        ModelBuilder builder,
        long attributes
    ) {

        Model wallModel =
            builder.createBox(
                70f,
                2.5f,
                0.5f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "77664E"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            wallModel
        );

        /*
         * Back wall.
         */
        addInstance(
            wallModel,
            0f,
            1.25f,
            -24f
        );

        /*
         * Front wall.
         */
        addInstance(
            wallModel,
            0f,
            1.25f,
            24f
        );
    }

    // =========================================================
    // INSTANCE HELPER
    // =========================================================

    private void addInstance(
        Model model,
        float x,
        float y,
        float z
    ) {

        ModelInstance instance =
            new ModelInstance(
                model
            );

        instance.transform
            .setToTranslation(
                x,
                y,
                z
            );

        instances.add(
            instance
        );
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public void update(float delta) {

        if (delta > 0.1f) {
            delta = 0.1f;
        }

        missionCompleteTimer += delta;

        /*
         * Update player.
         */
        if (
            player != null &&
            !levelCompleted
        ) {

            player.update(delta);

            updatePlayerModel();

            updateCamera();

            checkMissionProgress();
        }

        updateObjectiveMarker();
    }

    // =========================================================
    // CAMERA UPDATE
    // =========================================================

    private void updateCamera() {

        if (
            camera == null ||
            player == null
        ) {
            return;
        }

        /*
         * Third-person camera behind player.
         *
         * Current player movement uses world Z.
         */
        desiredCameraPosition.set(
            player.getX(),
            player.getY() + CAMERA_HEIGHT,
            player.getZ() + CAMERA_DISTANCE
        );

        /*
         * Smooth camera movement.
         */
        float smoothing =
            1f -
            (float) Math.exp(
                -CAMERA_SMOOTHNESS *
                Gdx.graphics.getDeltaTime()
            );

        camera.position.lerp(
            desiredCameraPosition,
            smoothing
        );

        cameraLookAt.set(
            player.getX(),
            player.getY() + 1.0f,
            player.getZ()
        );

        camera.lookAt(
            cameraLookAt
        );

        camera.update();
    }

    // =========================================================
    // MISSION CHECK
    // =========================================================

    private void checkMissionProgress() {

        if (levelCompleted) {
            return;
        }

        Vector3 target =
            missionTargets[
                missionIndex
            ];

        float dx =
            player.getX() -
            target.x;

        float dz =
            player.getZ() -
            target.z;

        float distance =
            (float) Math.sqrt(
                dx * dx +
                dz * dz
            );

        if (
            distance <=
            OBJECTIVE_DISTANCE
        ) {

            missionIndex++;

            if (
                missionIndex >=
                missionTargets.length
            ) {

                levelCompleted = true;

                System.out.println(
                    "LEVEL 1 COMPLETE"
                );

            } else {

                System.out.println(
                    "Mission completed. Next: " +
                    missionTitles[missionIndex]
                );
            }
        }
    }

    // =========================================================
    // RENDER
    // =========================================================

    public void render() {

        int width =
            Gdx.graphics.getWidth();

        int height =
            Gdx.graphics.getHeight();

        if (width <= 0) {
            width = GameConfig.WIDTH;
        }

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
         * Depth testing.
         */
        Gdx.gl.glEnable(
            GL20.GL_DEPTH_TEST
        );

        /*
         * Atmospheric dark blue sky.
         */
        Gdx.gl.glClearColor(
            0.07f,
            0.11f,
            0.15f,
            1f
        );

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT |
            GL20.GL_DEPTH_BUFFER_BIT
        );

        /*
         * Render 3D world.
         */
        modelBatch.begin(
            camera
        );

        modelBatch.render(
            instances,
            environment
        );

        modelBatch.end();

        /*
         * Render HUD.
         */
        renderHUD();
    }

    // =========================================================
    // HUD
    // =========================================================

    private void renderHUD() {

        spriteBatch.begin();

        if (levelCompleted) {

            renderLevelComplete();

        } else {

            renderMissionHUD();

            renderMiniMap();

            renderDirectionIndicator();
        }

        spriteBatch.end();
    }

    // =========================================================
    // MISSION HUD
    // =========================================================

    private void renderMissionHUD() {

        font.getData().setScale(
            1.25f
        );

        font.draw(
            spriteBatch,
            "LEVEL 1",
            30f,
            Gdx.graphics.getHeight() - 35f
        );

        font.getData().setScale(
            1.05f
        );

        font.draw(
            spriteBatch,
            "OBJECTIVE: " +
            missionTitles[missionIndex],
            30f,
            Gdx.graphics.getHeight() - 70f
        );

        font.getData().setScale(
            0.9f
        );

        font.draw(
            spriteBatch,
            missionDescriptions[
                missionIndex
            ],
            30f,
            Gdx.graphics.getHeight() - 100f
        );

        /*
         * Distance.
         */
        Vector3 target =
            missionTargets[
                missionIndex
            ];

        float dx =
            player.getX() -
            target.x;

        float dz =
            player.getZ() -
            target.z;

        float distance =
            (float) Math.sqrt(
                dx * dx +
                dz * dz
            );

        font.draw(
            spriteBatch,
            "Distance: " +
            String.format(
                "%.1f m",
                distance
            ),
            30f,
            Gdx.graphics.getHeight() - 130f
        );

        font.getData().setScale(
            0.8f
        );

        font.draw(
            spriteBatch,
            "W A S D  Move",
            30f,
            65f
        );

        font.draw(
            spriteBatch,
            "ESC  Pause",
            30f,
            38f
        );

        font.getData().setScale(
            1f
        );
    }

    // =========================================================
    // DIRECTION INDICATOR
    // =========================================================

    private void renderDirectionIndicator() {

        Vector3 target =
            missionTargets[
                missionIndex
            ];

        float dx =
            target.x -
            player.getX();

        float dz =
            target.z -
            player.getZ();

        String direction;

        if (
            Math.abs(dx) >
            Math.abs(dz)
        ) {

            if (dx > 0) {
                direction = "EAST →";
            } else {
                direction = "← WEST";
            }

        } else {

            if (dz > 0) {
                direction = "SOUTH ↓";
            } else {
                direction = "NORTH ↑";
            }
        }

        font.getData().setScale(
            1.15f
        );

        float x =
            Gdx.graphics.getWidth() / 2f -
            55f;

        font.draw(
            spriteBatch,
            direction,
            x,
            Gdx.graphics.getHeight() - 35f
        );

        font.getData().setScale(
            1f
        );
    }

    // =========================================================
    // MINI MAP
    // =========================================================

    private void renderMiniMap() {

        float mapSize = 150f;

        float mapX =
            Gdx.graphics.getWidth() -
            mapSize -
            25f;

        float mapY =
            Gdx.graphics.getHeight() -
            mapSize -
            30f;

        /*
         * Simple border.
         */
        font.getData().setScale(
            0.85f
        );

        font.draw(
            spriteBatch,
            "MAP",
            mapX,
            mapY + mapSize + 18f
        );

        /*
         * Player marker.
         */
        float normalizedX =
            (player.getX() + 30f) /
            60f;

        float normalizedZ =
            (player.getZ() + 20f) /
            40f;

        float px =
            mapX +
            MathUtils.clamp(
                normalizedX,
                0f,
                1f
            ) * mapSize;

        float py =
            mapY +
            MathUtils.clamp(
                normalizedZ,
                0f,
                1f
            ) * mapSize;

        font.draw(
            spriteBatch,
            "●",
            px,
            py
        );

        /*
         * Objective marker.
         */
        Vector3 target =
            missionTargets[
                missionIndex
            ];

        float targetX =
            mapX +
            MathUtils.clamp(
                (target.x + 30f) / 60f,
                0f,
                1f
            ) * mapSize;

        float targetY =
            mapY +
            MathUtils.clamp(
                (target.z + 20f) / 40f,
                0f,
                1f
            ) * mapSize;

        font.draw(
            spriteBatch,
            "★",
            targetX,
            targetY
        );

        font.getData().setScale(
            1f
        );
    }

    // =========================================================
    // LEVEL COMPLETE
    // =========================================================

    private void renderLevelComplete() {

        float centerX =
            Gdx.graphics.getWidth() / 2f;

        float centerY =
            Gdx.graphics.getHeight() / 2f;

        font.getData().setScale(
            2.8f
        );

        font.draw(
            spriteBatch,
            "LEVEL 1 COMPLETE",
            centerX - 230f,
            centerY + 50f
        );

        font.getData().setScale(
            1.3f
        );

        font.draw(
            spriteBatch,
            "WHERE IT ALL BEGAN",
            centerX - 135f,
            centerY
        );

        font.getData().setScale(
            1f
        );

        font.draw(
            spriteBatch,
            "All objectives completed.",
            centerX - 100f,
            centerY - 50f
        );

        font.draw(
            spriteBatch,
            "Press ESC to return.",
            centerX - 90f,
            centerY - 90f
        );
    }

    // =========================================================
    // CAMERA GETTER
    // =========================================================

    public PerspectiveCamera getCamera() {
        return camera;
    }

    // =========================================================
    // PLAYER GETTER
    // =========================================================

    public Player getPlayer() {
        return player;
    }

    // =========================================================
    // LEVEL COMPLETE GETTER
    // =========================================================

    public boolean isLevelCompleted() {
        return levelCompleted;
    }

    // =========================================================
    // RESIZE
    // =========================================================

    public void resize(
        int width,
        int height
    ) {

        if (
            camera == null ||
            height <= 0
        ) {
            return;
        }

        camera.viewportWidth =
            width;

        camera.viewportHeight =
            height;

        camera.update();
    }

    // =========================================================
    // DISPOSE
    // =========================================================

    @Override
    public void dispose() {

        if (modelBatch != null) {
            modelBatch.dispose();
        }

        if (spriteBatch != null) {
            spriteBatch.dispose();
        }

        if (font != null) {
            font.dispose();
        }

        for (
            Model model :
            models
        ) {

            if (model != null) {
                model.dispose();
            }
        }

        models.clear();

        instances.clear();

        player = null;

        playerInstance = null;

        playerModel = null;

        objectiveMarkerInstance = null;

        objectiveMarkerModel = null;
    }
}