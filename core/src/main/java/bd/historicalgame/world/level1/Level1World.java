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

        // Ambient light
        environment.set(
            ColorAttribute.createAmbient(
                0.75f,
                0.75f,
                0.75f,
                1f
            )
        );

        // Directional sunlight — DirectionalLight, NOT ColorAttribute.createDirectional
        environment.add(
            new DirectionalLight().set(
                1f,
                1f,
                1f,
                -1f,
                -0.8f,
                -0.5f
            )
        );

        instances = new Array<>();
        models = new Array<>();

        createCamera();
        createWorld();
    }

    private void createCamera() {

        camera = new PerspectiveCamera(67f, 1280f, 720f);
        camera.position.set(0f, 8f, 18f);
        camera.lookAt(0f, 0f, 0f);
        camera.near = 0.1f;
        camera.far = 500f;
        camera.update();

        cameraController = new CameraInputController(camera);
    }

    private void createWorld() {

        ModelBuilder builder = new ModelBuilder();

        long attributes =
            VertexAttributes.Usage.Position |
            VertexAttributes.Usage.Normal;

        // Ground
        Model groundModel = builder.createBox(
            60f, 0.4f, 40f,
            new Material(new Attribute[]{ ColorAttribute.createDiffuse(Color.valueOf("4B6B3C")) }),
            attributes
        );
        models.add(groundModel);
        ModelInstance ground = new ModelInstance(groundModel);
        ground.transform.setToTranslation(0f, -0.2f, 0f);
        instances.add(ground);

        // Main campus building
        Model buildingModel = builder.createBox(
            18f, 7f, 8f,
            new Material(new Attribute[]{ ColorAttribute.createDiffuse(Color.valueOf("D8C7A3")) }),
            attributes
        );
        models.add(buildingModel);
        ModelInstance building = new ModelInstance(buildingModel);
        building.transform.setToTranslation(0f, 3.5f, -10f);
        instances.add(building);

        // Left classroom block
        Model classroomModel = builder.createBox(
            12f, 5f, 7f,
            new Material(new Attribute[]{ ColorAttribute.createDiffuse(Color.valueOf("B8A47A")) }),
            attributes
        );
        models.add(classroomModel);
        ModelInstance classroom = new ModelInstance(classroomModel);
        classroom.transform.setToTranslation(-17f, 2.5f, -3f);
        instances.add(classroom);

        // Right classroom block
        ModelInstance classroom2 = new ModelInstance(classroomModel);
        classroom2.transform.setToTranslation(17f, 2.5f, -3f);
        instances.add(classroom2);

        // Courtyard platform
        Model courtyardModel = builder.createBox(
            18f, 0.5f, 14f,
            new Material(new Attribute[]{ ColorAttribute.createDiffuse(Color.valueOf("8E8068")) }),
            attributes
        );
        models.add(courtyardModel);
        ModelInstance courtyard = new ModelInstance(courtyardModel);
        courtyard.transform.setToTranslation(0f, 0.25f, 5f);
        instances.add(courtyard);

        // Gate pillars
        Model pillarModel = builder.createBox(
            2f, 8f, 2f,
            new Material(new Attribute[]{ ColorAttribute.createDiffuse(Color.valueOf("C5B28A")) }),
            attributes
        );
        models.add(pillarModel);
        ModelInstance leftPillar = new ModelInstance(pillarModel);
        leftPillar.transform.setToTranslation(-12f, 4f, 15f);
        instances.add(leftPillar);

        ModelInstance rightPillar = new ModelInstance(pillarModel);
        rightPillar.transform.setToTranslation(12f, 4f, 15f);
        instances.add(rightPillar);
    }

    public void update(float delta) {
        cameraController.update();
        camera.update();
    }

    public void render() {

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        modelBatch.end();
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    @Override
    public void dispose() {
        modelBatch.dispose();

        for (Model model : models) {
            model.dispose();
        }

        models.clear();
        instances.clear();
    }
}
