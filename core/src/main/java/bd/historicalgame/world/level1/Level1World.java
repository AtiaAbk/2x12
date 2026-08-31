package bd.historicalgame.world.level1;

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
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Level 1 world for the 2x12 historical adventure.
 *
 * This version provides:
 * - 3D camera
 * - Ambient light
 * - Directional sunlight
 * - Procedural ground
 * - Main building
 * - Classroom blocks
 * - Courtyard
 * - Gate pillars
 * - Camera mouse/keyboard control
 */
public class Level1World implements Disposable {

    private final ModelBatch modelBatch;
    private final Environment environment;
    private final Array<ModelInstance> instances;
    private final Array<Model> models;

    private PerspectiveCamera camera;
    private CameraInputController cameraController;

    public Level1World() {

        modelBatch = new ModelBatch();

        environment = new Environment();

        instances = new Array<>();
        models = new Array<>();

        createLighting();
        createCamera();
        createWorld();
    }

    // =========================================================
    // LIGHTING
    // =========================================================

    private void createLighting() {

        /*
         * Ambient light
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
         *
         * Important:
         * DirectionalLight is used here instead of the
         * non-existent ColorAttribute.createDirectional().
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

        /*
         * Prevent invalid viewport dimensions.
         */
        if (width <= 0) {
            width = 1280f;
        }

        if (height <= 0) {
            height = 720f;
        }

        camera = new PerspectiveCamera(
            67f,
            width,
            height
        );

        /*
         * Initial player/camera position.
         */
        camera.position.set(
            0f,
            8f,
            22f
        );

        /*
         * Look toward the center of the Level 1 world.
         */
        camera.lookAt(
            0f,
            2f,
            0f
        );

        camera.near = 0.1f;
        camera.far = 500f;

        camera.update();

        /*
         * Camera controller.
         */
        cameraController =
            new CameraInputController(camera);

        /*
         * Make the camera controller receive
         * mouse/keyboard input.
         */
        Gdx.input.setInputProcessor(
            cameraController
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

        if (cameraController != null) {
            cameraController.update();
        }

        if (camera != null) {
            camera.update();
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

        Gdx.gl.glViewport(
            0,
            0,
            width,
            height
        );

        /*
         * Enable depth testing so that
         * 3D objects render correctly.
         */
        Gdx.gl.glEnable(
            GL20.GL_DEPTH_TEST
        );

        /*
         * Sky/background color.
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
         * Render all Level 1 models.
         */
        modelBatch.begin(camera);

        modelBatch.render(
            instances,
            environment
        );

        modelBatch.end();
    }

    // =========================================================
    // CAMERA ACCESS
    // =========================================================

    public PerspectiveCamera getCamera() {
        return camera;
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
    }
}