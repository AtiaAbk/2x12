package bd.historicalgame.world.level1;

import bd.historicalgame.assets.FontManager;
import bd.historicalgame.game.GameConfig;
import bd.historicalgame.player.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
 * Dhaka University inspired historical campus environment.
 *
 * Design goals:
 * - Smooth third-person camera
 * - Cinematic environment
 * - Central campus road
 * - Curzon Hall inspired main building
 * - TSC inspired secondary location
 * - Library inspired location
 * - Central lawn
 * - Trees
 * - Lamps
 * - Benches
 * - Campus walls
 * - Mission markers
 * - Direction indicator
 * - Distance indicator
 * - Mini-map
 * - Atmospheric lighting
 *
 * NOTE:
 * The architecture is intentionally procedural so the game
 * can run without external 3D assets.
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
    private final FontManager fontManager;
    private final ShapeRenderer hudShapes;

    /**
     * Reference pixel size for a HUD "scale" of 1.0, mirroring
     * UIManager's approach: every size actually used is rendered
     * by FreeType at its exact pixel size instead of being
     * stretched from one small bitmap font (which is what made the
     * HUD text blurry).
     */
    private static final float HUD_BASE_FONT_PX = 18f;
    private static final int HUD_MIN_FONT_PX = 12;
    private static final int HUD_MAX_FONT_PX = 96;

    /**
     * Returns a crisp font for the given HUD "scale" (same
     * convention the old {@code font.getData().setScale(scale)}
     * calls used), already sized for the current window height.
     */
    private BitmapFont hudFont(float scale) {

        int pixelSize =
            MathUtils.clamp(
                Math.round(HUD_BASE_FONT_PX * scale),
                HUD_MIN_FONT_PX,
                HUD_MAX_FONT_PX
            );

        return fontManager.get(pixelSize);
    }

    // =========================================================
    // CAMERA
    // =========================================================

    private PerspectiveCamera camera;

    private float cameraYaw = 0f;
    private float cameraPitch = -12f;
    private static final float DEFAULT_CAMERA_YAW = 0f;
    private static final float DEFAULT_CAMERA_PITCH = -12f;
    private static final float MOUSE_SENSITIVITY = 0.18f;
    private static final float MIN_PITCH = -55f;
    private static final float MAX_PITCH = 35f;

    private final Vector3 desiredCameraPosition =
        new Vector3();

    private final Vector3 cameraLookAt =
        new Vector3();

    private static final float CAMERA_HEIGHT = 7.5f;
    private static final float CAMERA_DISTANCE = 13f;

    private static final float CAMERA_SMOOTH = 5.5f;

    // =========================================================
    // PLAYER
    // =========================================================

    private Player player;

    private Model playerBodyModel;
    private Model playerHeadModel;

    private ModelInstance playerBody;
    private ModelInstance playerHead;

    // =========================================================
    // MISSION
    // =========================================================

    private int missionIndex = 0;

    private boolean levelCompleted = false;

    private float missionTimer = 0f;

    private float totalTime = 0f;

    private final Vector3[] missionTargets = {

        // Mission 1
        // Just in front of the entrance stairs - reachable on
        // foot now that Curzon Hall's walls actually block
        // walking through them.
        new Vector3(
            0f,
            1f,
            -6.0f
        ),

        // Mission 2
        new Vector3(
            0f,
            1f,
            5f
        ),

        // Mission 3
        new Vector3(
            18f,
            1f,
            -3f
        ),

        // Mission 4
        // Just south of the library entrance - reachable on foot
        // now that its walls actually block walking through them.
        new Vector3(
            -18f,
            1f,
            1.5f
        ),

        // Mission 5
        new Vector3(
            0f,
            1f,
            14f
        )
    };

    private final String[] missionTitles = {

        "CURZON HALL",

        "CENTRAL LAWN",

        "TSC",

        "DU LIBRARY",

        "RETURN TO MAIN GATE"
    };

    private final String[] missionDescriptions = {

        "Reach Curzon Hall and investigate the area.",

        "Cross the Central Lawn and investigate the campus.",

        "Visit the TSC area and look for clues.",

        "Investigate the university library area.",

        "Return to the Main Gate."
    };

    private static final float MISSION_DISTANCE = 3.2f;

    // TSC roof: the building body is 4.5f high and the roof slab
    // raises the walkable top to approximately 4.825f.
    private static final float TSC_ROOF_Y = 4.825f;
    private static final float TSC_PLAYER_CENTER_Y = TSC_ROOF_Y + 0.75f;
    private static final float TSC_ROOF_TOLERANCE = 0.30f;

    // =========================================================
    // OBJECTIVE MARKER
    // =========================================================

    private Model objectiveModel;
    private ModelInstance objectiveMarker;

    // =========================================================
    // WORLD COLORS
    // =========================================================

    private static final Color GRASS =
        Color.valueOf("3C5D35");

    private static final Color GRASS_DARK =
        Color.valueOf("29452A");

    private static final Color PATH =
        Color.valueOf("8A7657");

    private static final Color PATH_LIGHT =
        Color.valueOf("A58F6D");

    private static final Color BRICK =
        Color.valueOf("A85E45");

    private static final Color BRICK_DARK =
        Color.valueOf("703B31");

    private static final Color CREAM =
        Color.valueOf("D7C39A");

    private static final Color ROOF =
        Color.valueOf("653C32");

    private static final Color WOOD =
        Color.valueOf("5A3E2B");

    private static final Color METAL =
        Color.valueOf("343A35");

    private static final Color GOLD =
        Color.valueOf("E6B84A");

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

        fontManager =
            new FontManager();

        hudShapes =
            new ShapeRenderer();

        createLighting();

        createCamera();

        createPlayer();

        createWorld();

        createObjectiveMarker();

        registerCollisions();
    }

    // =========================================================
    // LIGHTING
    // =========================================================

    private void createLighting() {

        environment.set(
            ColorAttribute.createAmbient(
                0.58f,
                0.58f,
                0.58f,
                1f
            )
        );

        environment.add(
            new DirectionalLight().set(
                1.0f,
                0.92f,
                0.78f,
                -0.65f,
                -1.0f,
                -0.45f
            )
        );
    }

    // =========================================================
    // CAMERA
    // =========================================================

    private void createCamera() {

        float width =
            Math.max(
                1f,
                Gdx.graphics.getWidth()
            );

        float height =
            Math.max(
                1f,
                Gdx.graphics.getHeight()
            );

        camera =
            new PerspectiveCamera(
                67f,
                width,
                height
            );

        camera.near = 0.1f;
        camera.far = 300f;
        resetCamera();
    }

    // =========================================================
    // PLAYER
    // =========================================================

    private void createPlayer() {

        player =
            new Player(
                0f,
                1f,
                14f,
                GameConfig.PLAYER_SPEED
            );

        ModelBuilder builder =
            new ModelBuilder();

        long attributes =
            VertexAttributes.Usage.Position |
            VertexAttributes.Usage.Normal;

        // -----------------------------------------------------
        // BODY
        // -----------------------------------------------------

        playerBodyModel =
            builder.createBox(
                0.9f,
                1.5f,
                0.65f,
                material(
                    Color.valueOf("33495C")
                ),
                attributes
            );

        models.add(
            playerBodyModel
        );

        playerBody =
            new ModelInstance(
                playerBodyModel
            );

        instances.add(
            playerBody
        );

        // -----------------------------------------------------
        // HEAD
        // -----------------------------------------------------

        playerHeadModel =
            builder.createSphere(
                0.72f,
                0.72f,
                0.72f,
                18,
                18,
                material(
                    Color.valueOf("B87850")
                ),
                attributes
            );

        models.add(
            playerHeadModel
        );

        playerHead =
            new ModelInstance(
                playerHeadModel
            );

        instances.add(
            playerHead
        );

        updatePlayerModel();
    }

    // =========================================================
    // PLAYER MODEL UPDATE
    // =========================================================

    private void updatePlayerModel() {

        if (player == null) {
            return;
        }

        float x =
            player.getX();

        float z =
            player.getZ();

        float bob =
            player.isMoving()
                ? MathUtils.sin(totalTime * 10f) * 0.035f
                : 0f;

        float y = player.getY();

        playerBody.transform.setToTranslation(
            x,
            y + bob,
            z
        );

        playerHead.transform.setToTranslation(
            x,
            y + 1.05f + bob,
            z
        );

        /*
         * Rotate player toward movement direction.
         */
        if (player.isMoving()) {

            Vector3 direction =
                player.getMovementDirection();

            float angle =
                MathUtils.atan2(
                    direction.x,
                    direction.z
                ) * MathUtils.radiansToDegrees;

            playerBody.transform.rotate(
                Vector3.Y,
                angle
            );
        }
    }

    // =========================================================
    // CAMERA FOLLOW
    // =========================================================

    private void updateCamera(float delta) {

        if (player == null) return;

        // R always restores the explicitly stored default camera state.
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            resetCamera();
            return;
        }

        // Mouse controls orientation only. Movement keys never touch yaw/pitch.
        if (!Gdx.input.isCursorCatched()) {
            Gdx.input.setCursorCatched(true);
        }

        float mouseDX = Gdx.input.getDeltaX();
        float mouseDY = Gdx.input.getDeltaY();

        cameraYaw -= mouseDX * MOUSE_SENSITIVITY;
        cameraPitch += mouseDY * MOUSE_SENSITIVITY;
        cameraPitch = MathUtils.clamp(cameraPitch, MIN_PITCH, MAX_PITCH);
        cameraYaw = ((cameraYaw + 180f) % 360f + 360f) % 360f - 180f;

        float yawRad = cameraYaw * MathUtils.degreesToRadians;
        Vector3 forward = new Vector3(
            -MathUtils.sin(yawRad),
            0f,
            -MathUtils.cos(yawRad)
        ).nor();
        Vector3 right = new Vector3(
            -forward.z,
            0f,
            forward.x
        ).nor();

        player.update(delta, forward, right);

        float pitchRad = cameraPitch * MathUtils.degreesToRadians;
        Vector3 lookDirection = new Vector3(
            forward.x * MathUtils.cos(pitchRad),
            MathUtils.sin(pitchRad),
            forward.z * MathUtils.cos(pitchRad)
        ).nor();

        Vector3 target = new Vector3(
            player.getX(),
            player.getY() + 1.25f,
            player.getZ()
        );

        float horizontalDistance = CAMERA_DISTANCE * MathUtils.cos(pitchRad);
        desiredCameraPosition.set(
            target.x - forward.x * horizontalDistance,
            target.y - lookDirection.y * CAMERA_DISTANCE,
            target.z - forward.z * horizontalDistance
        );

        float alpha = 1f - (float)Math.exp(-CAMERA_SMOOTH * delta);
        camera.position.lerp(desiredCameraPosition, alpha);
        camera.up.set(Vector3.Y);
        camera.lookAt(target);
        camera.update();
    }

    private void resetCamera() {
        cameraYaw = DEFAULT_CAMERA_YAW;
        cameraPitch = DEFAULT_CAMERA_PITCH;

        float yawRad = cameraYaw * MathUtils.degreesToRadians;
        Vector3 forward = new Vector3(
            -MathUtils.sin(yawRad), 0f, -MathUtils.cos(yawRad)
        ).nor();

        Vector3 target = new Vector3(
            player == null ? 0f : player.getX(),
            player == null ? 1.25f : player.getY() + 1.25f,
            player == null ? 0f : player.getZ()
        );

        float pitchRad = cameraPitch * MathUtils.degreesToRadians;
        Vector3 lookDirection = new Vector3(
            forward.x * MathUtils.cos(pitchRad),
            MathUtils.sin(pitchRad),
            forward.z * MathUtils.cos(pitchRad)
        ).nor();

        camera.position.set(
            target.x - forward.x * CAMERA_DISTANCE * MathUtils.cos(pitchRad),
            target.y - lookDirection.y * CAMERA_DISTANCE,
            target.z - forward.z * CAMERA_DISTANCE * MathUtils.cos(pitchRad)
        );
        camera.up.set(Vector3.Y);
        camera.lookAt(target);
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

        createGround(
            builder,
            attributes
        );

        createCampusPaths(
            builder,
            attributes
        );

        createCentralLawn(
            builder,
            attributes
        );

        createCurzonHall(
            builder,
            attributes
        );

        createTSC(
            builder,
            attributes
        );

        createLibrary(
            builder,
            attributes
        );

        createAcademicBuildings(
            builder,
            attributes
        );

        createMainGate(
            builder,
            attributes
        );

        createTrees(
            builder,
            attributes
        );

        createBenches(
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

        createDecorativeFlowers(
            builder,
            attributes
        );
    }

    // =========================================================
    // MATERIAL HELPER
    // =========================================================

    private Material material(Color color) {

        return new Material(
            ColorAttribute.createDiffuse(
                color
            )
        );
    }

    // =========================================================
    // ADD BOX
    // =========================================================

    private void addBox(
        ModelBuilder builder,
        long attributes,
        float width,
        float height,
        float depth,
        Color color,
        float x,
        float y,
        float z
    ) {

        Model model =
            builder.createBox(
                width,
                height,
                depth,
                material(color),
                attributes
            );

        models.add(model);

        ModelInstance instance =
            new ModelInstance(model);

        instance.transform.setToTranslation(
            x,
            y,
            z
        );

        instances.add(instance);
    }

    // =========================================================
    // GROUND
    // =========================================================

    private void createGround(
        ModelBuilder builder,
        long attributes
    ) {

        addBox(
            builder,
            attributes,
            72f,
            0.35f,
            52f,
            GRASS,
            0f,
            -0.2f,
            0f
        );

        /*
         * Dark green outer border.
         */
        addBox(
            builder,
            attributes,
            72f,
            0.08f,
            52f,
            GRASS_DARK,
            0f,
            0.01f,
            0f
        );
    }

    // =========================================================
    // CAMPUS PATHS
    // =========================================================

    private void createCampusPaths(
        ModelBuilder builder,
        long attributes
    ) {

        // Main north-south path
        addBox(
            builder,
            attributes,
            12f,
            0.18f,
            40f,
            PATH,
            0f,
            0.12f,
            0f
        );

        // Main east-west path
        addBox(
            builder,
            attributes,
            55f,
            0.17f,
            7f,
            PATH_LIGHT,
            0f,
            0.13f,
            5f
        );

        // Curzon approach
        addBox(
            builder,
            attributes,
            16f,
            0.17f,
            9f,
            PATH_LIGHT,
            0f,
            0.14f,
            -8f
        );

        // Library path
        addBox(
            builder,
            attributes,
            10f,
            0.16f,
            18f,
            PATH,
            -17f,
            0.12f,
            0f
        );

        // TSC path
        addBox(
            builder,
            attributes,
            10f,
            0.16f,
            18f,
            PATH,
            17f,
            0.12f,
            0f
        );
    }

    // =========================================================
    // CENTRAL LAWN
    // =========================================================

    private void createCentralLawn(
        ModelBuilder builder,
        long attributes
    ) {

        addBox(
            builder,
            attributes,
            20f,
            0.12f,
            14f,
            Color.valueOf("567744"),
            0f,
            0.08f,
            5f
        );

        /*
         * Decorative central circle.
         */
        Model circle =
            builder.createCylinder(
                5.5f,
                0.15f,
                5.5f,
                32,
                material(
                    Color.valueOf("6F8F55")
                ),
                attributes
            );

        models.add(circle);

        ModelInstance circleInstance =
            new ModelInstance(circle);

        circleInstance.transform.setToTranslation(
            0f,
            0.18f,
            5f
        );

        instances.add(circleInstance);
    }

    // =========================================================
    // CURZON HALL
    // =========================================================

    private void createCurzonHall(
        ModelBuilder builder,
        long attributes
    ) {

        /*
         * Main red-brick body.
         */
        addBox(
            builder,
            attributes,
            22f,
            6f,
            7f,
            BRICK,
            0f,
            3f,
            -13f
        );

        /*
         * Central entrance.
         */
        addBox(
            builder,
            attributes,
            7f,
            7.5f,
            4f,
            BRICK_DARK,
            0f,
            3.75f,
            -8.8f
        );

        /*
         * Central tower.
         */
        addBox(
            builder,
            attributes,
            4f,
            10f,
            4f,
            BRICK,
            0f,
            7f,
            -13f
        );

        /*
         * Tower roof.
         */
        Model roof =
            builder.createCone(
                3.3f,
                3.8f,
                3.3f,
                4,
                material(ROOF),
                attributes
            );

        models.add(roof);

        ModelInstance roofInstance =
            new ModelInstance(roof);

        roofInstance.transform.setToTranslation(
            0f,
            12.8f,
            -13f
        );

        instances.add(roofInstance);

        /*
         * Side towers.
         */
        createTower(
            builder,
            attributes,
            -9f,
            -13f
        );

        createTower(
            builder,
            attributes,
            9f,
            -13f
        );

        /*
         * Windows.
         */
        createWindowRow(
            builder,
            attributes,
            -7f,
            -10f
        );

        createWindowRow(
            builder,
            attributes,
            7f,
            -10f
        );

        /*
         * Entrance stairs.
         */
        addBox(
            builder,
            attributes,
            10f,
            0.25f,
            3f,
            PATH_LIGHT,
            0f,
            0.22f,
            -7.5f
        );
    }

    // =========================================================
    // TOWER
    // =========================================================

    private void createTower(
        ModelBuilder builder,
        long attributes,
        float x,
        float z
    ) {

        addBox(
            builder,
            attributes,
            4f,
            9f,
            4f,
            BRICK_DARK,
            x,
            4.5f,
            z
        );

        Model roof =
            builder.createCone(
                3.2f,
                3.5f,
                3.2f,
                4,
                material(ROOF),
                attributes
            );

        models.add(roof);

        ModelInstance instance =
            new ModelInstance(roof);

        instance.transform.setToTranslation(
            x,
            10f,
            z
        );

        instances.add(instance);
    }

    // =========================================================
    // WINDOWS
    // =========================================================

    private void createWindowRow(
        ModelBuilder builder,
        long attributes,
        float startX,
        float z
    ) {

        for (int i = 0; i < 4; i++) {

            float x =
                startX
                    + (i * 4f);

            addBox(
                builder,
                attributes,
                1.4f,
                2f,
                0.18f,
                Color.valueOf("2C3D43"),
                x,
                3.2f,
                z
            );
        }
    }

    // =========================================================
    // TSC
    // =========================================================

    private void createTSC(
        ModelBuilder builder,
        long attributes
    ) {

        addBox(
            builder,
            attributes,
            12f,
            4.5f,
            7f,
            CREAM,
            18f,
            2.25f,
            -3f
        );

        addBox(
            builder,
            attributes,
            13f,
            0.35f,
            8f,
            ROOF,
            18f,
            4.65f,
            -3f
        );

        createWindowRow(
            builder,
            attributes,
            14f,
            -6.55f
        );
    }

    // =========================================================
    // LIBRARY
    // =========================================================

    private void createLibrary(
        ModelBuilder builder,
        long attributes
    ) {

        addBox(
            builder,
            attributes,
            12f,
            4.5f,
            7f,
            CREAM,
            -18f,
            2.25f,
            -3f
        );

        addBox(
            builder,
            attributes,
            13f,
            0.35f,
            8f,
            ROOF,
            -18f,
            4.65f,
            -3f
        );

        createWindowRow(
            builder,
            attributes,
            -24f,
            -6.55f
        );
    }

    // =========================================================
    // ACADEMIC BUILDINGS
    // =========================================================

    private void createAcademicBuildings(
        ModelBuilder builder,
        long attributes
    ) {

        addBuilding(
            builder,
            attributes,
            -20f,
            11f
        );

        addBuilding(
            builder,
            attributes,
            20f,
            11f
        );

        addBuilding(
            builder,
            attributes,
            -26f,
            -13f
        );

        addBuilding(
            builder,
            attributes,
            26f,
            -13f
        );
    }

    private void addBuilding(
        ModelBuilder builder,
        long attributes,
        float x,
        float z
    ) {

        addBox(
            builder,
            attributes,
            10f,
            4f,
            6f,
            CREAM,
            x,
            2f,
            z
        );

        addBox(
            builder,
            attributes,
            11f,
            0.3f,
            7f,
            ROOF,
            x,
            4.15f,
            z
        );
    }

    // =========================================================
    // MAIN GATE
    // =========================================================

    private void createMainGate(
        ModelBuilder builder,
        long attributes
    ) {

        createGatePillar(
            builder,
            attributes,
            -10f,
            16f
        );

        createGatePillar(
            builder,
            attributes,
            10f,
            16f
        );

        /*
         * Gate beam.
         */
        addBox(
            builder,
            attributes,
            22f,
            1.3f,
            1.3f,
            CREAM,
            0f,
            7f,
            16f
        );

        /*
         * Gate pathway.
         */
        addBox(
            builder,
            attributes,
            12f,
            0.15f,
            8f,
            PATH_LIGHT,
            0f,
            0.12f,
            15f
        );
    }

    private void createGatePillar(
        ModelBuilder builder,
        long attributes,
        float x,
        float z
    ) {

        addBox(
            builder,
            attributes,
            2.2f,
            7f,
            2.2f,
            CREAM,
            x,
            3.5f,
            z
        );

        Model cap =
            builder.createCone(
                1.6f,
                1.5f,
                1.6f,
                4,
                material(ROOF),
                attributes
            );

        models.add(cap);

        ModelInstance instance =
            new ModelInstance(cap);

        instance.transform.setToTranslation(
            x,
            7.8f,
            z
        );

        instances.add(instance);
    }

    // =========================================================
    // TREES
    // =========================================================

    private void createTrees(
        ModelBuilder builder,
        long attributes
    ) {

        float[][] positions = {

            {-27f, 14f},
            {-21f, 7f},
            {-27f, 1f},
            {-25f, -7f},
            {-28f, -17f},

            {-12f, 12f},
            {-12f, 7f},
            {-12f, 1f},
            {-12f, -5f},

            {12f, 12f},
            {12f, 7f},
            {12f, 1f},
            {12f, -5f},

            {27f, 14f},
            {22f, 8f},
            {27f, 1f},
            {25f, -7f},
            {28f, -17f},

            {-7f, 17f},
            {7f, 17f}
        };

        for (float[] p : positions) {

            createTree(
                builder,
                attributes,
                p[0],
                p[1]
            );
        }
    }

    private void createTree(
        ModelBuilder builder,
        long attributes,
        float x,
        float z
    ) {

        /*
         * Trunk.
         */
        Model trunk =
            builder.createCylinder(
                0.55f,
                3.2f,
                0.55f,
                12,
                material(WOOD),
                attributes
            );

        models.add(trunk);

        ModelInstance trunkInstance =
            new ModelInstance(trunk);

        trunkInstance.transform.setToTranslation(
            x,
            1.6f,
            z
        );

        instances.add(trunkInstance);

        /*
         * Lower foliage.
         */
        Model leaves =
            builder.createSphere(
                4.2f,
                4.2f,
                4.2f,
                16,
                16,
                material(
                    Color.valueOf("365B35")
                ),
                attributes
            );

        models.add(leaves);

        ModelInstance leavesInstance =
            new ModelInstance(leaves);

        leavesInstance.transform.setToTranslation(
            x,
            4.8f,
            z
        );

        instances.add(leavesInstance);

        /*
         * Upper foliage.
         */
        Model crown =
            builder.createSphere(
                3.2f,
                3.2f,
                3.2f,
                16,
                16,
                material(
                    Color.valueOf("426B3C")
                ),
                attributes
            );

        models.add(crown);

        ModelInstance crownInstance =
            new ModelInstance(crown);

        crownInstance.transform.setToTranslation(
            x + 0.5f,
            7.2f,
            z
        );

        instances.add(crownInstance);
    }

    // =========================================================
    // BENCHES
    // =========================================================

    private void createBenches(
        ModelBuilder builder,
        long attributes
    ) {

        float[][] positions = {

            {-8f, 5f},
            {8f, 5f},
            {-8f, 1f},
            {8f, 1f},
            {-7f, 9f},
            {7f, 9f}
        };

        for (float[] p : positions) {

            createBench(
                builder,
                attributes,
                p[0],
                p[1]
            );
        }
    }

    private void createBench(
        ModelBuilder builder,
        long attributes,
        float x,
        float z
    ) {

        /*
         * Seat.
         */
        addBox(
            builder,
            attributes,
            3.5f,
            0.3f,
            0.7f,
            WOOD,
            x,
            0.9f,
            z
        );

        /*
         * Back.
         */
        addBox(
            builder,
            attributes,
            3.5f,
            1.2f,
            0.25f,
            WOOD,
            x,
            1.45f,
            z + 0.25f
        );

        /*
         * Legs.
         */
        addBox(
            builder,
            attributes,
            0.3f,
            0.9f,
            0.3f,
            METAL,
            x - 1.2f,
            0.45f,
            z
        );

        addBox(
            builder,
            attributes,
            0.3f,
            0.9f,
            0.3f,
            METAL,
            x + 1.2f,
            0.45f,
            z
        );
    }

    // =========================================================
    // LAMPS
    // =========================================================

    private void createLamps(
        ModelBuilder builder,
        long attributes
    ) {

        float[][] positions = {

            {-8f, 13f},
            {8f, 13f},
            {-8f, 7f},
            {8f, 7f},
            {-8f, 0f},
            {8f, 0f},
            {-8f, -7f},
            {8f, -7f},
            {-8f, -14f},
            {8f, -14f}
        };

        for (float[] p : positions) {

            createLamp(
                builder,
                attributes,
                p[0],
                p[1]
            );
        }
    }

    private void createLamp(
        ModelBuilder builder,
        long attributes,
        float x,
        float z
    ) {

        Model pole =
            builder.createCylinder(
                0.14f,
                3.5f,
                0.14f,
                10,
                material(METAL),
                attributes
            );

        models.add(pole);

        ModelInstance poleInstance =
            new ModelInstance(pole);

        poleInstance.transform.setToTranslation(
            x,
            1.75f,
            z
        );

        instances.add(poleInstance);

        Model light =
            builder.createSphere(
                0.55f,
                0.55f,
                0.55f,
                12,
                12,
                material(GOLD),
                attributes
            );

        models.add(light);

        ModelInstance lightInstance =
            new ModelInstance(light);

        lightInstance.transform.setToTranslation(
            x,
            3.55f,
            z
        );

        instances.add(lightInstance);
    }

    // =========================================================
    // CAMPUS WALLS
    // =========================================================

    private void createCampusWalls(
        ModelBuilder builder,
        long attributes
    ) {

        addBox(
            builder,
            attributes,
            70f,
            1.8f,
            0.5f,
            BRICK_DARK,
            0f,
            0.9f,
            -24f
        );

        addBox(
            builder,
            attributes,
            70f,
            1.8f,
            0.5f,
            BRICK_DARK,
            0f,
            0.9f,
            24f
        );

        addBox(
            builder,
            attributes,
            0.5f,
            1.8f,
            48f,
            BRICK_DARK,
            -34f,
            0.9f,
            0f
        );

        addBox(
            builder,
            attributes,
            0.5f,
            1.8f,
            48f,
            BRICK_DARK,
            34f,
            0.9f,
            0f
        );
    }

    // =========================================================
    // FLOWERS / SMALL DECORATION
    // =========================================================

    private void createDecorativeFlowers(
        ModelBuilder builder,
        long attributes
    ) {

        for (int i = 0; i < 30; i++) {

            float x =
                -30f +
                ((i * 17) % 60);

            float z =
                -20f +
                ((i * 13) % 40);

            Model flower =
                builder.createSphere(
                    0.16f,
                    0.16f,
                    0.16f,
                    8,
                    8,
                    material(
                        Color.valueOf(
                            i % 2 == 0
                                ? "D7A7A0"
                                : "D9C16C"
                        )
                    ),
                    attributes
                );

            models.add(flower);

            ModelInstance flowerInstance =
                new ModelInstance(flower);

            flowerInstance.transform.setToTranslation(
                x,
                0.3f,
                z
            );

            instances.add(
                flowerInstance
            );
        }
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

        objectiveModel =
            builder.createCylinder(
                0.8f,
                3.2f,
                0.8f,
                20,
                material(GOLD),
                attributes
            );

        models.add(
            objectiveModel
        );

        objectiveMarker =
            new ModelInstance(
                objectiveModel
            );

        instances.add(
            objectiveMarker
        );

        updateObjectiveMarker();
    }

    private void updateObjectiveMarker() {

        if (
            levelCompleted ||
            objectiveMarker == null
        ) {
            return;
        }

        Vector3 target =
            missionTargets[
                missionIndex
            ];

        float baseY =
            missionIndex == 2
                ? 6.35f
                : 3.8f;

        float y =
            baseY +
            MathUtils.sin(
                totalTime * 3f
            ) * 0.5f;

        objectiveMarker.transform
            .setToTranslation(
                target.x,
                y,
                target.z
            );

        objectiveMarker.transform.rotate(
            Vector3.Y,
            totalTime * 60f
        );
    }

    // =========================================================
    // COLLISIONS
    // =========================================================

    private void registerCollisions() {

        /*
         * Curzon Hall - main body.
         * Rendered as a 22x7 box centered at (0, -13).
         */
        player.addCollision(
            -11f,
            -16.5f,
            22f,
            7f,
            6.0f
        );

        /*
         * Curzon Hall - entrance block.
         * Rendered as a 7x4 box centered at (0, -8.8), taller
         * than the main body, protruding toward the courtyard.
         */
        player.addCollision(
            -3.5f,
            -10.8f,
            7f,
            4f,
            7.5f
        );

        /*
         * TSC. The roof is a real jumpable platform.
         * Rendered as a 12x7 box centered at (18, -3).
         */
        player.addCollision(
            12f,
            -6.5f,
            12f,
            7f,
            TSC_ROOF_Y
        );

        /*
         * Library.
         * Rendered as a 12x7 box centered at (-18, -3).
         */
        player.addCollision(
            -24f,
            -6.5f,
            12f,
            7f,
            4.5f
        );

        /*
         * The four academic buildings (createAcademicBuildings()),
         * each a 10x6 box. These previously had NO collision
         * registered at all, which is why the player could walk
         * straight through them.
         */
        registerAcademicBuilding(-20f, 11f);
        registerAcademicBuilding(20f, 11f);
        registerAcademicBuilding(-26f, -13f);
        registerAcademicBuilding(26f, -13f);

        /*
         * Main gate pillars.
         * Rendered as 2.2x2.2 columns at (-10, 16) and (10, 16).
         */
        registerGatePillar(-10f, 16f);
        registerGatePillar(10f, 16f);
    }

    /**
     * Registers a collision box for one academic building, given
     * its center (matching the x/z passed to addBuilding() in
     * createAcademicBuildings()). The building itself is a 10x6
     * box with its roof topping out around y=4.3.
     */
    private void registerAcademicBuilding(
        float centerX,
        float centerZ
    ) {
        player.addCollision(
            centerX - 5f,
            centerZ - 3f,
            10f,
            6f,
            4.0f
        );
    }

    /**
     * Registers a small collision box for one gate pillar, given
     * its center (matching the x/z passed to createGatePillar()).
     */
    private void registerGatePillar(
        float centerX,
        float centerZ
    ) {
        player.addCollision(
            centerX - 1.1f,
            centerZ - 1.1f,
            2.2f,
            2.2f,
            7.0f
        );
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public void update(float delta) {

        /*
         * Avoid huge frame jumps.
         */
        delta =
            Math.min(
                delta,
                0.05f
            );

        totalTime += delta;

        if (!levelCompleted) {

            updateCamera(delta);

            updatePlayerModel();

            updateObjectiveMarker();

            updateMission(delta);
        }
    }

    // =========================================================
    // MISSION UPDATE
    // =========================================================

    private void updateMission(float delta) {

        missionTimer += delta;

        Vector3 target =
            missionTargets[
                missionIndex
            ];

        float dx =
            player.getX() - target.x;

        float dz =
            player.getZ() - target.z;

        float distance =
            (float)Math.sqrt(
                dx * dx +
                dz * dz
            );

        boolean insideObjectiveRadius =
            distance <= MISSION_DISTANCE;

        boolean tscRoofReached =
            missionIndex != 2
            || (
                player.isGrounded()
                &&
                player.getY() >=
                    TSC_PLAYER_CENTER_Y - TSC_ROOF_TOLERANCE
            );

        if (insideObjectiveRadius && tscRoofReached) {
            completeCurrentMission();
        }
    }

    private void completeCurrentMission() {

        missionTimer = 0f;

        if (
            missionIndex <
            missionTargets.length - 1
        ) {

            missionIndex++;

        } else {

            levelCompleted = true;
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

        /*
         * Cinematic dark-blue sky.
         */
        Gdx.gl.glClearColor(
            0.055f,
            0.09f,
            0.12f,
            1f
        );

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT |
            GL20.GL_DEPTH_BUFFER_BIT
        );

        // -----------------------------------------------------
        // 3D
        // -----------------------------------------------------

        modelBatch.begin(camera);

        modelBatch.render(
            instances,
            environment
        );

        modelBatch.end();

        // -----------------------------------------------------
        // HUD
        // -----------------------------------------------------

        renderHUD();
    }

    // =========================================================
    // HUD
    // =========================================================

    private void renderHUD() {

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float scale = Math.max(1f, h / 1080f);

        // High-resolution, screen-space HUD panels.
        hudShapes.setProjectionMatrix(spriteBatch.getProjectionMatrix());
        hudShapes.begin(ShapeRenderer.ShapeType.Filled);
        hudShapes.setColor(0.025f, 0.035f, 0.045f, 0.88f);
        hudShapes.rect(24f * scale, h - 166f * scale, 560f * scale, 142f * scale);
        hudShapes.setColor(0.90f, 0.72f, 0.25f, 0.95f);
        hudShapes.rect(24f * scale, h - 166f * scale, 5f * scale, 142f * scale);

        if (!levelCompleted) {
            hudShapes.setColor(0.025f, 0.035f, 0.045f, 0.88f);
            hudShapes.circle(w / 2f, 78f * scale, 46f * scale, 32);
            hudShapes.setColor(0.90f, 0.72f, 0.25f, 0.95f);
            hudShapes.circle(w / 2f, 78f * scale, 4f * scale, 20);
        }
        hudShapes.end();

        spriteBatch.begin();

        float titleScale = 1.45f * scale;
        float bodyScale = 0.92f * scale;
        float smallScale = 0.78f * scale;

        BitmapFont titleFont = hudFont(titleScale);
        titleFont.setColor(Color.WHITE);
        String title = levelCompleted
            ? "LEVEL 1 COMPLETE"
            : "MISSION " + (missionIndex + 1) + "  •  " + missionTitles[missionIndex];
        titleFont.draw(spriteBatch, title, 48f * scale, h - 50f * scale);

        BitmapFont bodyFont = hudFont(bodyScale);
        bodyFont.setColor(Color.WHITE);

        if (!levelCompleted) {
            bodyFont.draw(spriteBatch, missionDescriptions[missionIndex], 48f * scale, h - 84f * scale);

            Vector3 target = missionTargets[missionIndex];
            float dx = target.x - player.getX();
            float dz = target.z - player.getZ();
            float distance = (float)Math.sqrt(dx * dx + dz * dz);
            bodyFont.draw(spriteBatch, String.format("OBJECTIVE   %.1f m", distance), 48f * scale, h - 119f * scale);

            BitmapFont smallFont = hudFont(smallScale);
            smallFont.setColor(Color.WHITE);
            smallFont.draw(spriteBatch, "NEXT: " + getDirection(dx, dz), 48f * scale, h - 145f * scale);

            if (missionIndex == 2) {
                BitmapFont rooftopFont = hudFont(0.74f * scale);
                rooftopFont.setColor(Color.valueOf("6FE7E0"));
                rooftopFont.draw(
                    spriteBatch,
                    "ROOFTOP OBJECTIVE  •  SHIFT / SPACE TO JUMP",
                    48f * scale,
                    h - 170f * scale
                );
            }

            // Center compass/waypoint label.
            String arrow = getCameraRelativeArrow(target);
            float arrowWidth = bodyFont.getData().capHeight * 2f;
            bodyFont.draw(spriteBatch, arrow, w / 2f - arrowWidth, 93f * scale);
        } else {
            bodyFont.draw(spriteBatch, "Campus exploration completed.", 48f * scale, h - 92f * scale);

            BitmapFont smallFont = hudFont(smallScale);
            smallFont.setColor(Color.WHITE);
            smallFont.draw(spriteBatch, "Press ESC to open the pause menu.", 48f * scale, h - 125f * scale);
        }

        BitmapFont hintFont = hudFont(smallScale);
        hintFont.setColor(Color.WHITE);

        String controlsHint =
            "W A S D  MOVE     SHIFT / SPACE  JUMP     MOUSE  CAMERA     R  RESET     ESC  PAUSE";

        GlyphLayout controlsLayout =
            new GlyphLayout(hintFont, controlsHint);

        hintFont.draw(
            spriteBatch,
            controlsHint,
            w - controlsLayout.width - 28f * scale,
            28f * scale
        );

        if (levelCompleted) {
            BitmapFont completeFont = hudFont(2.4f * scale);
            completeFont.setColor(Color.WHITE);
            String complete = "LEVEL 1 COMPLETE";
            GlyphLayout completeLayout = new GlyphLayout(completeFont, complete);
            completeFont.draw(spriteBatch, complete, w / 2f - completeLayout.width / 2f, h / 2f + 30f * scale);

            BitmapFont completeBodyFont = hudFont(1.0f * scale);
            completeBodyFont.setColor(Color.WHITE);
            GlyphLayout completeBodyLayout = new GlyphLayout(completeBodyFont, "Campus exploration completed.");
            completeBodyFont.draw(
                spriteBatch,
                "Campus exploration completed.",
                w / 2f - completeBodyLayout.width / 2f,
                h / 2f - 20f * scale
            );
        }

        spriteBatch.end();

        /*
         * Self-contained: manages its own ShapeRenderer and
         * SpriteBatch begin/end pairs, so it must run after
         * the main HUD batch above has been closed.
         */
        renderMiniMap();
    }

    private String getCameraRelativeArrow(Vector3 target) {
        float dx = target.x - player.getX();
        float dz = target.z - player.getZ();
        if (Math.abs(dx) < 1.5f && Math.abs(dz) < 1.5f) return "◆";

        float yawRad = cameraYaw * MathUtils.degreesToRadians;
        Vector3 forward = new Vector3(-MathUtils.sin(yawRad), 0f, -MathUtils.cos(yawRad)).nor();
        Vector3 right = new Vector3(-forward.z, 0f, forward.x).nor();
        Vector3 toTarget = new Vector3(dx, 0f, dz).nor();

        float forwardDot = forward.dot(toTarget);
        float rightDot = right.dot(toTarget);
        if (forwardDot > 0.70f) return "↑";
        if (forwardDot < -0.70f) return "↓";
        if (rightDot > 0f) return "→";
        return "←";
    }

    // =========================================================
    // DIRECTION
    // =========================================================

    private String getDirection(
        float dx,
        float dz
    ) {

        if (
            Math.abs(dx) <
                1.5f &&
            Math.abs(dz) <
                1.5f
        ) {

            return "HERE";
        }

        if (
            Math.abs(dx) >
            Math.abs(dz)
        ) {

            return dx > 0
                ? "EAST  →"
                : "WEST  ←";
        }

        return dz > 0
            ? "SOUTH  ↓"
            : "NORTH  ↑";
    }

    // =========================================================
    // MINI MAP (RADAR)
    // =========================================================

    private static final float MINIMAP_RADIUS = 76f;
    private static final float MINIMAP_RANGE = 32f;

    /**
     * Circular radar in the top-right corner. Rotates with the
     * camera so the player's current facing direction always
     * points to the top of the disc (same convention as the
     * on-screen directional arrow), with a dot per mission
     * target and a highlighted dot for the current objective.
     */
    private void renderMiniMap() {

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        float scale = Math.max(1f, h / 1080f);

        float radius = MINIMAP_RADIUS * scale;
        float margin = 26f * scale;

        float centerX = w - margin - radius;
        float centerY = h - margin - radius;

        /*
         * Same forward/right convention as
         * getCameraRelativeArrow(), so the radar and the
         * on-screen compass arrow always agree.
         */
        float yawRad =
            cameraYaw * MathUtils.degreesToRadians;

        float forwardX = -MathUtils.sin(yawRad);
        float forwardZ = -MathUtils.cos(yawRad);

        float rightX = -forwardZ;
        float rightZ = forwardX;

        hudShapes.setProjectionMatrix(
            spriteBatch.getProjectionMatrix()
        );

        hudShapes.begin(ShapeRenderer.ShapeType.Filled);

        /*
         * Ring border: a slightly larger gold disc behind a
         * smaller dark disc, leaving a thin gold rim visible.
         */
        hudShapes.setColor(0.90f, 0.72f, 0.25f, 0.55f);
        hudShapes.circle(centerX, centerY, radius + 3f * scale, 48);

        hudShapes.setColor(0.03f, 0.045f, 0.055f, 0.90f);
        hudShapes.circle(centerX, centerY, radius, 48);

        /*
         * Mission markers.
         */
        for (int i = 0; i < missionTargets.length; i++) {

            Vector3 target = missionTargets[i];

            float dx = target.x - player.getX();
            float dz = target.z - player.getZ();

            float worldDist =
                (float) Math.sqrt(dx * dx + dz * dz);

            float mapX;
            float mapY;

            if (worldDist < 0.001f) {

                mapX = 0f;
                mapY = 0f;

            } else {

                float clampedDist =
                    Math.min(worldDist, MINIMAP_RANGE);

                float mapDist =
                    (clampedDist / MINIMAP_RANGE)
                        * (radius - 8f * scale);

                float mapRight =
                    (dx * rightX + dz * rightZ) / worldDist;

                float mapUp =
                    (dx * forwardX + dz * forwardZ) / worldDist;

                mapX = mapRight * mapDist;
                mapY = mapUp * mapDist;
            }

            boolean isCurrent =
                i == missionIndex && !levelCompleted;

            boolean isPast =
                i < missionIndex;

            if (isCurrent) {
                hudShapes.setColor(0.90f, 0.72f, 0.25f, 1f);
            } else if (isPast) {
                hudShapes.setColor(0.6f, 0.6f, 0.6f, 0.55f);
            } else {
                hudShapes.setColor(0.6f, 0.6f, 0.6f, 0.35f);
            }

            hudShapes.circle(
                centerX + mapX,
                centerY + mapY,
                (isCurrent ? 5f : 3.5f) * scale,
                16
            );
        }

        /*
         * Player marker, fixed at the radar's center. Since the
         * radar rotates with the camera, this always points up.
         */
        hudShapes.setColor(0.94f, 0.96f, 0.98f, 1f);

        float triSize = 7f * scale;

        hudShapes.triangle(
            centerX, centerY + triSize,
            centerX - triSize * 0.65f, centerY - triSize * 0.55f,
            centerX + triSize * 0.65f, centerY - triSize * 0.55f
        );

        hudShapes.end();

        /*
         * Crisp thin ring outline over the filled discs.
         */
        hudShapes.begin(ShapeRenderer.ShapeType.Line);
        hudShapes.setColor(0.90f, 0.72f, 0.25f, 0.9f);
        hudShapes.circle(centerX, centerY, radius, 48);
        hudShapes.end();

        String label =
            levelCompleted
                ? "CAMPUS MAP"
                : missionTitles[
                    Math.min(missionIndex, missionTitles.length - 1)
                ];

        BitmapFont labelFont = hudFont(0.62f * scale);
        labelFont.setColor(Color.valueOf("E8BC55"));

        GlyphLayout labelLayout =
            new GlyphLayout(labelFont, label);

        spriteBatch.begin();

        labelFont.draw(
            spriteBatch,
            label,
            centerX - labelLayout.width / 2f,
            centerY - radius - 10f * scale
        );

        spriteBatch.end();
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

    public boolean isLevelCompleted() {

        return levelCompleted;
    }

    public int getMissionIndex() {

        return missionIndex;
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

        if (fontManager != null) {
            fontManager.dispose();
        }

        if (hudShapes != null) {
            hudShapes.dispose();
        }

        for (Model model : models) {

            if (model != null) {
                model.dispose();
            }
        }

        models.clear();

        instances.clear();

        player = null;
        playerBody = null;
        playerHead = null;
        objectiveMarker = null;
    }
}