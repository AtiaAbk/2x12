package bd.historicalgame.world.level1;

import bd.historicalgame.game.GameConfig;
import bd.historicalgame.player.Player;

import com.badlogic.gdx.Gdx;
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
 * LEVEL 1
 *
 * Cinematic historical campus prototype.
 *
 * Main goals:
 * - Smooth third person camera
 * - Cinematic lighting
 * - More detailed architecture
 * - Trees and vegetation
 * - Roads and courtyard
 * - Gate
 * - Lamps
 * - Mission system
 * - Objective marker
 * - Distance indicator
 * - Direction indicator
 * - Mini map
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

    private static final float CAMERA_DISTANCE = 14f;
    private static final float CAMERA_HEIGHT = 6.5f;
    private static final float CAMERA_SMOOTHNESS = 7f;

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

    private float worldTime = 0f;

    private static final float OBJECTIVE_DISTANCE = 3.5f;

    private final Vector3[] missionTargets = {

        new Vector3(
            0f,
            1f,
            -4.5f
        ),

        new Vector3(
            0f,
            1f,
            5f
        ),

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

        modelBatch = new ModelBatch();

        environment = new Environment();

        instances = new Array<>();

        models = new Array<>();

        spriteBatch = new SpriteBatch();

        font = new BitmapFont();

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
         * Warm natural ambient light.
         */
        environment.set(
            ColorAttribute.createAmbient(
                0.55f,
                0.58f,
                0.62f,
                1f
            )
        );

        /*
         * Main sunlight.
         */
        environment.add(
            new DirectionalLight().set(
                1.0f,
                0.88f,
                0.68f,
                -0.65f,
                -1.0f,
                -0.35f
            )
        );

        /*
         * Secondary soft fill light.
         */
        environment.add(
            new DirectionalLight().set(
                0.30f,
                0.38f,
                0.50f,
                0.5f,
                -0.4f,
                0.7f
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
            65f,
            width,
            height
        );

        camera.near = 0.1f;

        camera.far = 500f;

        camera.position.set(
            0f,
            CAMERA_HEIGHT,
            26f
        );

        camera.lookAt(
            0f,
            1.4f,
            8f
        );

        camera.update();
    }

    // =========================================================
    // PLAYER
    // =========================================================

    private void createPlayer() {

        player = new Player(
            0f,
            1f,
            12f,
            GameConfig.PLAYER_SPEED
        );

        /*
         * Main building collision.
         */
        player.addCollision(
            -9f,
            -14f,
            18f,
            8f
        );

        /*
         * Left classroom.
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
         * Temporary human-like stylized character.
         *
         * This is still procedural.
         * Later we can replace it with a real
         * animated GLB/FBX character.
         */
        playerModel =
            builder.createCylinder(
                0.55f,
                2.0f,
                0.55f,
                16,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "6F3827"
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
    // PLAYER MODEL UPDATE
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
    // OBJECTIVE
    // =========================================================

    private void createObjectiveMarker() {

        ModelBuilder builder =
            new ModelBuilder();

        long attributes =
            VertexAttributes.Usage.Position |
            VertexAttributes.Usage.Normal;

        objectiveMarkerModel =
            builder.createCylinder(
                0.65f,
                2.8f,
                0.65f,
                24,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "D6A63A"
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
    // OBJECTIVE UPDATE
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

        float y =
            2.8f +
            MathUtils.sin(
                worldTime * 2.4f
            ) * 0.35f;

        objectiveMarkerInstance.transform
            .setToTranslation(
                target.x,
                y,
                target.z
            );
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

        createCourtyardDetails(
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
                75f,
                0.35f,
                55f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "263C2A"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            groundModel
        );

        addInstance(
            groundModel,
            0f,
            -0.18f,
            0f
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
                19f,
                0.12f,
                38f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "51493E"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            roadModel
        );

        addInstance(
            roadModel,
            0f,
            0.05f,
            0f
        );

        Model crossRoadModel =
            builder.createBox(
                58f,
                0.10f,
                7f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "48443D"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            crossRoadModel
        );

        addInstance(
            crossRoadModel,
            0f,
            0.04f,
            7f
        );

        /*
         * Road edge strips.
         */
        Model edgeModel =
            builder.createBox(
                0.35f,
                0.08f,
                38f,
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
            edgeModel
        );

        addInstance(
            edgeModel,
            -9.7f,
            0.12f,
            0f
        );

        addInstance(
            edgeModel,
            9.7f,
            0.12f,
            0f
        );
    }

    // =========================================================
    // MAIN BUILDING
    // =========================================================

    private void createMainBuilding(
        ModelBuilder builder,
        long attributes
    ) {

        /*
         * Main body.
         */
        Model buildingModel =
            builder.createBox(
                18f,
                7f,
                8f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "B9A77F"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            buildingModel
        );

        addInstance(
            buildingModel,
            0f,
            3.5f,
            -10f
        );

        /*
         * Dark roof.
         */
        Model roofModel =
            builder.createBox(
                19f,
                0.8f,
                9f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "44372D"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            roofModel
        );

        addInstance(
            roofModel,
            0f,
            7.35f,
            -10f
        );

        /*
         * Entrance platform.
         */
        Model entranceModel =
            builder.createBox(
                11f,
                0.4f,
                3.5f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "806A50"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            entranceModel
        );

        addInstance(
            entranceModel,
            0f,
            0.2f,
            -5.3f
        );

        /*
         * Entrance pillars.
         */
        Model pillarModel =
            builder.createBox(
                1.0f,
                6.2f,
                1.0f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "D1BF99"
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
            -7f,
            3.1f,
            -5.8f
        );

        addInstance(
            pillarModel,
            7f,
            3.1f,
            -5.8f
        );

        addInstance(
            pillarModel,
            0f,
            3.1f,
            -5.8f
        );

        /*
         * Entrance top beam.
         */
        Model beamModel =
            builder.createBox(
                15f,
                0.8f,
                1.1f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "8C704F"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            beamModel
        );

        addInstance(
            beamModel,
            0f,
            6.1f,
            -5.8f
        );

        /*
         * Windows.
         */
        Model windowModel =
            builder.createBox(
                1.8f,
                2.2f,
                0.18f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "263744"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            windowModel
        );

        addInstance(
            windowModel,
            -6f,
            4.1f,
            -5.82f
        );

        addInstance(
            windowModel,
            -3f,
            4.1f,
            -5.82f
        );

        addInstance(
            windowModel,
            3f,
            4.1f,
            -5.82f
        );

        addInstance(
            windowModel,
            6f,
            4.1f,
            -5.82f
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
                                "967F60"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            classroomModel
        );

        addInstance(
            classroomModel,
            -17f,
            2.5f,
            -3f
        );

        addInstance(
            classroomModel,
            17f,
            2.5f,
            -3f
        );

        /*
         * Roofs.
         */
        Model roofModel =
            builder.createBox(
                13f,
                0.7f,
                7.8f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "45382D"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            roofModel
        );

        addInstance(
            roofModel,
            -17f,
            5.35f,
            -3f
        );

        addInstance(
            roofModel,
            17f,
            5.35f,
            -3f
        );

        /*
         * Back buildings.
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
                                "A18C6A"
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
                0.30f,
                14f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "74664F"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            courtyardModel
        );

        addInstance(
            courtyardModel,
            0f,
            0.18f,
            5f
        );

        /*
         * Central stone circle.
         */
        Model circleModel =
            builder.createCylinder(
                4.2f,
                0.18f,
                4.2f,
                32,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "95866C"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            circleModel
        );

        addInstance(
            circleModel,
            0f,
            0.42f,
            5f
        );
    }

    // =========================================================
    // COURTYARD DETAILS
    // =========================================================

    private void createCourtyardDetails(
        ModelBuilder builder,
        long attributes
    ) {

        /*
         * Central monument base.
         */
        Model baseModel =
            builder.createCylinder(
                1.6f,
                0.8f,
                1.6f,
                24,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "6D604E"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            baseModel
        );

        addInstance(
            baseModel,
            0f,
            0.8f,
            5f
        );

        /*
         * Monument column.
         */
        Model columnModel =
            builder.createCylinder(
                0.65f,
                3.2f,
                0.65f,
                20,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "B4A487"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            columnModel
        );

        addInstance(
            columnModel,
            0f,
            2.4f,
            5f
        );

        /*
         * Top ornament.
         */
        Model topModel =
            builder.createSphere(
                1.1f,
                1.1f,
                1.1f,
                16,
                12,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "B9964A"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            topModel
        );

        addInstance(
            topModel,
            0f,
            4.3f,
            5f
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
                                "B29A70"
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
         * Gate beam.
         */
        Model beamModel =
            builder.createBox(
                26f,
                1.3f,
                1.5f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "72583D"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            beamModel
        );

        addInstance(
            beamModel,
            0f,
            7.2f,
            15f
        );

        /*
         * Gate top decoration.
         */
        Model topModel =
            builder.createBox(
                18f,
                0.7f,
                1.8f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "4E3C2C"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            topModel
        );

        addInstance(
            topModel,
            0f,
            8.2f,
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
                0.48f,
                3.2f,
                0.48f,
                12,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "4E3525"
                            )
                        )
                    }
                ),
                attributes
            );

        Model leavesModel =
            builder.createSphere(
                3.5f,
                3.8f,
                3.5f,
                16,
                12,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "315E35"
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
         * Perimeter trees.
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
            -8f,
            0f,
            5f
        );

        addTree(
            trunkModel,
            leavesModel,
            8f,
            0f,
            5f
        );

        addTree(
            trunkModel,
            leavesModel,
            -8f,
            0f,
            10f
        );

        addTree(
            trunkModel,
            leavesModel,
            8f,
            0f,
            10f
        );

        /*
         * Additional trees.
         */
        addTree(
            trunkModel,
            leavesModel,
            -22f,
            0f,
            8f
        );

        addTree(
            trunkModel,
            leavesModel,
            22f,
            0f,
            8f
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
                y + 1.6f,
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
                y + 4.0f,
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
                0.16f,
                3.8f,
                0.16f,
                10,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "292725"
                            )
                        )
                    }
                ),
                attributes
            );

        Model lightModel =
            builder.createSphere(
                0.55f,
                0.55f,
                0.55f,
                12,
                10,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "E8C66C"
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
                1.9f,
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
                4.0f,
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
                                "655744"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            wallModel
        );

        addInstance(
            wallModel,
            0f,
            1.25f,
            -24f
        );

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

        worldTime += delta;

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
         * Camera follows from behind.
         */
        desiredCameraPosition.set(
            player.getX(),
            player.getY() + CAMERA_HEIGHT,
            player.getZ() + CAMERA_DISTANCE
        );

        /*
         * Smooth exponential interpolation.
         */
        float smoothing =
            1f -
            (float) Math.exp(
                -CAMERA_SMOOTHNESS * Gdx.graphics.getDeltaTime()
            );

        camera.position.lerp(
            desiredCameraPosition,
            smoothing
        );

        /*
         * Slightly above player's head.
         */
        cameraLookAt.set(
            player.getX(),
            player.getY() + 1.15f,
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
                    "MISSION COMPLETE"
                );

                System.out.println(
                    "NEXT: " +
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
            height = GameConfig.HEIGHT;
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

        /*
         * Cinematic warm sky.
         */
        Gdx.gl.glClearColor(
            0.12f,
            0.17f,
            0.21f,
            1f
        );

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT |
            GL20.GL_DEPTH_BUFFER_BIT
        );

        /*
         * 3D world.
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
         * HUD.
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

            renderDirectionIndicator();

            renderMiniMap();
        }

        spriteBatch.end();
    }

    // =========================================================
    // MISSION HUD
    // =========================================================

    private void renderMissionHUD() {

        font.getData().setScale(
            1.35f
        );

        font.draw(
            spriteBatch,
            "LEVEL 01",
            30f,
            Gdx.graphics.getHeight() - 35f
        );

        font.getData().setScale(
            1.05f
        );

        font.draw(
            spriteBatch,
            "OBJECTIVE",
            30f,
            Gdx.graphics.getHeight() - 70f
        );

        font.getData().setScale(
            0.95f
        );

        font.draw(
            spriteBatch,
            missionTitles[missionIndex],
            30f,
            Gdx.graphics.getHeight() - 100f
        );

        font.getData().setScale(
            0.82f
        );

        font.draw(
            spriteBatch,
            missionDescriptions[
                missionIndex
            ],
            30f,
            Gdx.graphics.getHeight() - 128f
        );

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
            "Distance  " +
            String.format(
                "%.1f m",
                distance
            ),
            30f,
            Gdx.graphics.getHeight() - 155f
        );

        font.getData().setScale(
            0.78f
        );

        font.draw(
            spriteBatch,
            "W A S D   MOVE",
            30f,
            60f
        );

        font.draw(
            spriteBatch,
            "ESC   PAUSE",
            30f,
            35f
        );

        font.getData().setScale(
            1f
        );
    }

    // =========================================================
    // DIRECTION
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
                direction = "EAST  >";
            } else {
                direction = "<  WEST";
            }

        } else {

            if (dz > 0) {
                direction = "SOUTH  v";
            } else {
                direction = "NORTH  ^";
            }
        }

        font.getData().setScale(
            1.05f
        );

        float x =
            Gdx.graphics.getWidth() / 2f -
            45f;

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

        float mapSize = 140f;

        float mapX =
            Gdx.graphics.getWidth() -
            mapSize -
            25f;

        float mapY =
            Gdx.graphics.getHeight() -
            mapSize -
            35f;

        font.getData().setScale(
            0.82f
        );

        font.draw(
            spriteBatch,
            "CAMPUS",
            mapX,
            mapY + mapSize + 18f
        );

        /*
         * Player.
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
            ) *
            mapSize;

        float py =
            mapY +
            MathUtils.clamp(
                normalizedZ,
                0f,
                1f
            ) *
            mapSize;

        font.draw(
            spriteBatch,
            "●",
            px,
            py
        );

        /*
         * Objective.
         */
        Vector3 target =
            missionTargets[
                missionIndex
            ];

        float tx =
            mapX +
            MathUtils.clamp(
                (target.x + 30f) / 60f,
                0f,
                1f
            ) *
            mapSize;

        float ty =
            mapY +
            MathUtils.clamp(
                (target.z + 20f) / 40f,
                0f,
                1f
            ) *
            mapSize;

        font.draw(
            spriteBatch,
            "★",
            tx,
            ty
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
            2.7f
        );

        font.draw(
            spriteBatch,
            "LEVEL 1 COMPLETE",
            centerX - 220f,
            centerY + 55f
        );

        font.getData().setScale(
            1.25f
        );

        font.draw(
            spriteBatch,
            "WHERE IT ALL BEGAN",
            centerX - 130f,
            centerY
        );

        font.getData().setScale(
            0.95f
        );

        font.draw(
            spriteBatch,
            "All objectives completed.",
            centerX - 95f,
            centerY - 45f
        );

        font.draw(
            spriteBatch,
            "Press ESC to return.",
            centerX - 85f,
            centerY - 80f
        );

        font.getData().setScale(
            1f
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
    // LEVEL COMPLETE
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