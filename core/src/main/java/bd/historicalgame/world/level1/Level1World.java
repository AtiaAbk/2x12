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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Level 1 world for the 2x12 historical adventure.
 *
 * Current features:
 *
 * - 3D environment
 * - Lighting
 * - Ground
 * - Main building
 * - Classrooms
 * - Courtyard
 * - Gate
 * - Player
 * - Player movement
 * - Third-person camera
 * - Mission system
 * - Objective marker
 * - Direction indicator
 * - Distance indicator
 * - Mission completion
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

    private SpriteBatch spriteBatch;
    private BitmapFont font;

    // =========================================================
    // CAMERA
    // =========================================================

    private PerspectiveCamera camera;

    private final Vector3 cameraOffset =
        new Vector3(
            0f,
            8f,
            22f
        );

    // =========================================================
    // PLAYER
    // =========================================================

    private Player player;

    private Model playerModel;
    private ModelInstance playerInstance;

    // =========================================================
    // OBJECTIVE MARKER
    // =========================================================

    private Model objectiveMarkerModel;
    private ModelInstance objectiveMarkerInstance;

    // =========================================================
    // MISSION SYSTEM
    // =========================================================

    private int missionIndex = 0;

    private boolean levelCompleted = false;

    /*
     * Mission target positions.
     *
     * Mission 0:
     * Main Building
     *
     * Mission 1:
     * Courtyard
     *
     * Mission 2:
     * Notice / central investigation point
     */
    private final Vector3[] missionTargets = {

        new Vector3(
            0f,
            1f,
            -10f
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

        "Go to the Main Building.",

        "Investigate the Courtyard.",

        "Find the notice and investigate it."
    };

    /*
     * Distance required to complete an objective.
     */
    private static final float OBJECTIVE_DISTANCE = 4.0f;

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
            8f,
            22f
        );

        camera.near =
            0.1f;

        camera.far =
            500f;

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
                            Color.valueOf(
                                "C98B5A"
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
    // OBJECTIVE MARKER
    // =========================================================

    private void createObjectiveMarker() {

        ModelBuilder builder =
            new ModelBuilder();

        long attributes =
            VertexAttributes.Usage.Position |
            VertexAttributes.Usage.Normal;

        /*
         * Tall marker above the objective.
         */
        objectiveMarkerModel =
            builder.createCylinder(
                1.2f,
                4f,
                1.2f,
                20,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "E8B84A"
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
    // UPDATE PLAYER MODEL
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
    // UPDATE OBJECTIVE MARKER
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
         * Put marker above the target.
         */
        objectiveMarkerInstance
            .transform
            .setToTranslation(
                target.x,
                3.0f,
                target.z
            );
    }

    // =========================================================
    // CAMERA FOLLOW
    // =========================================================

    private void updateCamera() {

        if (
            camera == null ||
            player == null
        ) {
            return;
        }

        camera.position.set(
            player.getX()
                + cameraOffset.x,

            player.getY()
                + cameraOffset.y,

            player.getZ()
                + cameraOffset.z
        );

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
                            Color.valueOf(
                                "4B6B3C"
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

        ground.transform
            .setToTranslation(
                0f,
                -0.2f,
                0f
            );

        instances.add(
            ground
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
                                "D8C7A3"
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
                                "B8A47A"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            classroomModel
        );

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

        instances.add(
            leftClassroom
        );

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

        instances.add(
            rightClassroom
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
                0.5f,
                14f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "8E8068"
                                
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
                0.25f,
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
                2f,
                8f,
                2f,
                new Material(
                    new Attribute[]{
                        ColorAttribute.createDiffuse(
                            Color.valueOf(
                                "C5B28A"
                            )
                        )
                    }
                ),
                attributes
            );

        models.add(
            pillarModel
        );

        ModelInstance leftPillar =
            new ModelInstance(
                pillarModel
            );

        leftPillar.transform
            .setToTranslation(
                -12f,
                4f,
                15f
            );

        instances.add(
            leftPillar
        );

        ModelInstance rightPillar =
            new ModelInstance(
                pillarModel
            );

        rightPillar.transform
            .setToTranslation(
                12f,
                4f,
                15f
            );

        instances.add(
            rightPillar
        );
    }

    // =========================================================
    // MISSION SYSTEM
    // =========================================================

    private void updateMission() {

        if (levelCompleted) {
            return;
        }

        Vector3 target =
            missionTargets[
                missionIndex
            ];

        float dx =
            player.getX() - target.x;

        float dz =
            player.getZ() - target.z;

        float distance =
            (float) Math.sqrt(
                dx * dx +
                dz * dz
            );

        /*
         * Objective reached.
         */
        if (
            distance <=
            OBJECTIVE_DISTANCE
        ) {

            System.out.println(
                "MISSION COMPLETE: "
                + missionTitles[
                    missionIndex
                ]
            );

            missionIndex++;

            /*
             * All Level 1 missions completed.
             */
            if (
                missionIndex >=
                missionTargets.length
            ) {

                levelCompleted = true;

                System.out.println(
                    "================================"
                );

                System.out.println(
                    "LEVEL 1 COMPLETE!"
                );

                System.out.println(
                    "================================"
                );

            } else {

                updateObjectiveMarker();
            }
        }
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public void update(float delta) {

        if (levelCompleted) {

            /*
             * Allow ESC to exit Level 1
             * later through Main.
             */
            return;
        }

        if (player != null) {

            player.update(
                delta
            );
        }

        updatePlayerModel();

        updateCamera();

        updateMission();
    }

    // =========================================================
    // HUD
    // =========================================================

    private void renderHUD() {

        if (levelCompleted) {

            renderLevelComplete();

            return;
        }

        Vector3 target =
            missionTargets[
                missionIndex
            ];

        float dx =
            target.x - player.getX();

        float dz =
            target.z - player.getZ();

        float distance =
            (float) Math.sqrt(
                dx * dx +
                dz * dz
            );

        String direction =
            getDirection(
                dx,
                dz
            );

        int width =
            Gdx.graphics.getWidth();

        int height =
            Gdx.graphics.getHeight();

        spriteBatch.begin();

        /*
         * Mission number.
         */
        font.getData()
            .setScale(
                1.5f
            );

        font.setColor(
            Color.WHITE
        );

        font.draw(
            spriteBatch,
            "MISSION "
                + (missionIndex + 1)
                + " / "
                + missionTargets.length,

            40,
            height - 40
        );

        /*
         * Mission title.
         */
        font.getData()
            .setScale(
                1.8f
            );

        font.draw(
            spriteBatch,

            missionTitles[
                missionIndex
            ],

            40,
            height - 80
        );

        /*
         * Mission description.
         */
        font.getData()
            .setScale(
                1.1f
            );

        font.draw(
            spriteBatch,

            missionDescriptions[
                missionIndex
            ],

            40,
            height - 120
        );

        /*
         * Direction.
         */
        font.getData()
            .setScale(
                1.4f
            );

        font.draw(
            spriteBatch,

            "DIRECTION: "
                + direction,

            40,
            90
        );

        /*
         * Distance.
         */
        font.draw(
            spriteBatch,

            "DISTANCE: "
                + String.format(
                    "%.1f",
                    distance
                )
                + " m",

            40,
            55
        );

        /*
         * Controls.
         */
        font.getData()
            .setScale(
                1.0f
            );

        font.draw(
            spriteBatch,

            "W A S D  Move     ESC  Pause",

            40,
            25
        );

        spriteBatch.end();
    }

    // =========================================================
    // DIRECTION CALCULATION
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

        /*
         * World Z negative = forward.
         */
        if (
            Math.abs(dx) >
            Math.abs(dz)
        ) {

            if (dx > 0) {
                return "RIGHT";
            } else {
                return "LEFT";
            }

        } else {

            if (dz > 0) {
                return "BACK";
            } else {
                return "FORWARD";
            }
        }
    }

    // =========================================================
    // LEVEL COMPLETE
    // =========================================================

    private void renderLevelComplete() {

        int width =
            Gdx.graphics.getWidth();

        int height =
            Gdx.graphics.getHeight();

        spriteBatch.begin();

        font.setColor(
            Color.WHITE
        );

        font.getData()
            .setScale(
                3.0f
            );

        font.draw(
            spriteBatch,
            "LEVEL 1 COMPLETE!",
            width / 2f - 210,
            height / 2f + 50
        );

        font.getData()
            .setScale(
                1.4f
            );

        font.draw(
            spriteBatch,
            "You completed all objectives.",
            width / 2f - 150,
            height / 2f
        );

        font.draw(
            spriteBatch,
            "LEVEL 2 UNLOCKED",
            width / 2f - 120,
            height / 2f - 50
        );

        spriteBatch.end();
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
        modelBatch.begin(
            camera
        );

        modelBatch.render(
            instances,
            environment
        );

        modelBatch.end();

        /*
         * 2D mission HUD.
         */
        renderHUD();
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