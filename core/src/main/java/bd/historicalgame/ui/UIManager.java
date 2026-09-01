package bd.historicalgame.ui;

import bd.historicalgame.game.GameState;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Presentation-focused UI layer for 2x12.
 *
 * The existing gameplay/world systems remain responsible for 3D rendering,
 * player movement, camera control and missions.
 *
 * This class owns the screen-space presentation and menu interaction.
 */
public class UIManager implements Disposable {

    public enum Action {

        NONE,

        START_LEVEL1,

        START_PLAYING,

        RESUME,

        EXIT_TO_MAIN_MENU,

        EXIT_GAME,

        SETTINGS,

        CLOSE_SETTINGS,

        CONFIRM_EXIT,

        CANCEL_EXIT
    }

    // =========================================================
    // VISUAL THEME
    // =========================================================

    private static final Color GOLD =
        Color.valueOf("E6B84A");

    private static final Color CREAM =
        Color.valueOf("F2E9D5");

    private static final Color MUTED =
        Color.valueOf("B9B2A4");

    private static final Color PANEL =
        Color.valueOf("10151B");

    private static final Color PANEL_SOFT =
        Color.valueOf("171D24");

    // =========================================================
    // UI OBJECTS
    // =========================================================

    private final Stage stage;

    private final Skin skin;

    private final BitmapFont font;

    private final ShapeRenderer shapes;

    // =========================================================
    // PROCEDURAL UI TEXTURES
    // =========================================================

    private Texture panelTexture;

    private Texture panelSoftTexture;

    private Texture buttonTexture;

    private Texture buttonOverTexture;

    private Texture buttonDownTexture;

    private TextureRegionDrawable panelDrawable;

    private TextureRegionDrawable panelSoftDrawable;

    private TextureRegionDrawable buttonDrawable;

    private TextureRegionDrawable buttonOverDrawable;

    private TextureRegionDrawable buttonDownDrawable;

    // =========================================================
    // STATE
    // =========================================================

    private GameState activeState;

    private Action pendingAction =
        Action.NONE;

    private boolean settingsOpen;

    private boolean exitConfirmation;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public UIManager() {

        stage =
            new Stage(
                new ScreenViewport()
            );

        skin =
            new Skin();

        font =
            new BitmapFont();

        shapes =
            new ShapeRenderer();

        createDrawables();

        configureSkin();
    }

    // =========================================================
    // DRAWABLE CREATION
    // =========================================================

    private void createDrawables() {

        panelTexture =
            createTexture(
                new Color(
                    PANEL.r,
                    PANEL.g,
                    PANEL.b,
                    0.94f
                )
            );

        panelSoftTexture =
            createTexture(
                new Color(
                    PANEL_SOFT.r,
                    PANEL_SOFT.g,
                    PANEL_SOFT.b,
                    0.90f
                )
            );

        buttonTexture =
            createTexture(
                new Color(
                    0.07f,
                    0.09f,
                    0.11f,
                    0.84f
                )
            );

        buttonOverTexture =
            createTexture(
                new Color(
                    GOLD.r,
                    GOLD.g,
                    GOLD.b,
                    0.18f
                )
            );

        buttonDownTexture =
            createTexture(
                new Color(
                    GOLD.r,
                    GOLD.g,
                    GOLD.b,
                    0.30f
                )
            );

        panelDrawable =
            drawable(panelTexture);

        panelSoftDrawable =
            drawable(panelSoftTexture);

        buttonDrawable =
            drawable(buttonTexture);

        buttonOverDrawable =
            drawable(buttonOverTexture);

        buttonDownDrawable =
            drawable(buttonDownTexture);
    }

    private Texture createTexture(Color color) {

        Pixmap pixmap =
            new Pixmap(
                2,
                2,
                Pixmap.Format.RGBA8888
            );

        pixmap.setColor(color);

        pixmap.fill();

        Texture texture =
            new Texture(pixmap);

        texture.setFilter(
            Texture.TextureFilter.Linear,
            Texture.TextureFilter.Linear
        );

        pixmap.dispose();

        return texture;
    }

    private TextureRegionDrawable drawable(
        Texture texture
    ) {

        return new TextureRegionDrawable(
            new TextureRegion(texture)
        );
    }

    // =========================================================
    // SKIN
    // =========================================================

    private void configureSkin() {

        skin.add(
            "default-font",
            font
        );

        Label.LabelStyle labelStyle =
            new Label.LabelStyle();

        labelStyle.font =
            font;

        labelStyle.fontColor =
            CREAM;

        skin.add(
            "default",
            labelStyle
        );

        TextButton.TextButtonStyle buttonStyle =
            new TextButton.TextButtonStyle();

        buttonStyle.font =
            font;

        buttonStyle.fontColor =
            CREAM;

        buttonStyle.overFontColor =
            GOLD;

        buttonStyle.downFontColor =
            Color.WHITE;

        buttonStyle.up =
            buttonDrawable;

        buttonStyle.over =
            buttonOverDrawable;

        buttonStyle.down =
            buttonDownDrawable;

        skin.add(
            "menu",
            buttonStyle
        );
    }

    // =========================================================
    // STATE UI
    // =========================================================

    /**
     * Builds the UI for a game state.
     *
     * The actor tree is rebuilt only when the state changes.
     * It is never rebuilt every frame.
     */
    public void showState(
        GameState state
    ) {

        if (
            state == activeState &&
            (
                exitConfirmation ||
                settingsOpen
            )
        ) {

            return;
        }

        if (state == activeState) {

            setInteractive(
                state != GameState.PLAYING
            );

            return;
        }

        activeState =
            state;

        stage.clear();

        settingsOpen =
            false;

        if (
            state ==
            GameState.MAIN_MENU
        ) {

            buildMainMenu();

        } else if (
            state ==
            GameState.LEVEL1_INTRO
        ) {

            buildIntro();

        } else if (
            state ==
            GameState.PAUSED
        ) {

            buildPauseMenu();
        }

        setInteractive(
            state != GameState.PLAYING
        );
    }

    // =========================================================
    // MAIN MENU
    // =========================================================

    private void buildMainMenu() {

        Table root =
            new Table();

        root.setFillParent(true);

        root.top().left();

        root.pad(
            0f,
            0f,
            0f,
            0f
        );

        Table content =
            new Table();

        content.top().left();

        content.padTop(
            105f
        );

        content.padLeft(
            96f
        );

        Label eyebrow =
            label(
                "DHAKA UNIVERSITY CAMPUS  •  LEVEL 1",
                0.72f,
                GOLD
            );

        Label title =
            label(
                "2x12",
                4.6f,
                Color.WHITE
            );

        Label subtitle =
            label(
                "WHERE WINDS MEET STYLE",
                1.25f,
                CREAM
            );

        Label period =
            label(
                "A HISTORICAL ADVENTURE",
                0.72f,
                MUTED
            );

        content
            .add(eyebrow)
            .left()
            .row();

        content
            .add(title)
            .left()
            .padTop(8f)
            .row();

        content
            .add(subtitle)
            .left()
            .padTop(2f)
            .row();

        content
            .add(period)
            .left()
            .padTop(10f)
            .row();

        content
            .add()
            .height(62f)
            .row();

        content
            .add(
                menuButton(
                    "BEGIN JOURNEY",
                    Action.START_LEVEL1
                )
            )
            .width(330f)
            .height(56f)
            .left()
            .row();

        content
            .add(
                menuButton(
                    "SETTINGS",
                    Action.SETTINGS
                )
            )
            .width(330f)
            .height(50f)
            .left()
            .padTop(10f)
            .row();

        content
            .add(
                menuButton(
                    "EXIT",
                    Action.EXIT_GAME
                )
            )
            .width(330f)
            .height(50f)
            .left()
            .padTop(10f)
            .row();

        Label controls =
            label(
                "↑ ↓  SELECT        ENTER  CONFIRM        ESC  EXIT",
                0.66f,
                MUTED
            );

        content
            .add(controls)
            .left()
            .padTop(38f)
            .row();

        root
            .add(content)
            .expand()
            .fill();

        addAnimatedActor(root);
    }

    // =========================================================
    // LEVEL 1 INTRO
    // =========================================================

    private void buildIntro() {

        Table root =
            new Table();

        root.setFillParent(true);

        root.bottom().left();

        Table panel =
            new Table();

        panel.setBackground(
            panelDrawable
        );

        panel.pad(
            34f,
            46f,
            30f,
            46f
        );

        panel
            .add(
                label(
                    "LEVEL 1  /  WHERE IT ALL BEGAN",
                    0.76f,
                    GOLD
                )
            )
            .left()
            .row();

        panel
            .add(
                label(
                    "MID-JUNE 2024",
                    2.05f,
                    Color.WHITE
                )
            )
            .left()
            .padTop(7f)
            .row();

        panel
            .add(
                label(
                    "A normal day is about to change.",
                    1.0f,
                    CREAM
                )
            )
            .left()
            .padTop(12f)
            .row();

        panel
            .add(
                label(
                    "Explore the campus and discover what is happening.",
                    0.82f,
                    MUTED
                )
            )
            .left()
            .padTop(5f)
            .row();

        panel
            .add(
                menuButton(
                    "ENTER CAMPUS",
                    Action.START_PLAYING
                )
            )
            .left()
            .width(260f)
            .height(50f)
            .padTop(22f)
            .row();

        panel
            .add(
                label(
                    "ENTER  BEGIN        Q  EXIT",
                    0.64f,
                    MUTED
                )
            )
            .left()
            .padTop(12f)
            .row();

        root
            .add(panel)
            .width(560f)
            .padLeft(42f)
            .padBottom(42f)
            .left();

        addAnimatedActor(root);
    }

    // =========================================================
    // PAUSE MENU
    // =========================================================

    private void buildPauseMenu() {

        Table root =
            new Table();

        root.setFillParent(true);

        root.center();

        Table panel =
            new Table();

        panel.setBackground(
            panelDrawable
        );

        panel.pad(
            38f,
            48f,
            34f,
            48f
        );

        panel
            .add(
                label(
                    "PAUSED",
                    2.55f,
                    Color.WHITE
                )
            )
            .center()
            .row();

        panel
            .add(
                label(
                    "2x12  •  DHAKA UNIVERSITY CAMPUS",
                    0.68f,
                    GOLD
                )
            )
            .center()
            .padTop(5f)
            .row();

        panel
            .add()
            .height(28f)
            .row();

        panel
            .add(
                menuButton(
                    "RESUME",
                    Action.RESUME
                )
            )
            .width(310f)
            .height(52f)
            .row();

        panel
            .add(
                menuButton(
                    "SETTINGS",
                    Action.SETTINGS
                )
            )
            .width(310f)
            .height(48f)
            .padTop(9f)
            .row();

        panel
            .add(
                menuButton(
                    "EXIT TO MAIN MENU",
                    Action.EXIT_TO_MAIN_MENU
                )
            )
            .width(310f)
            .height(48f)
            .padTop(9f)
            .row();

        panel
            .add(
                menuButton(
                    "EXIT GAME",
                    Action.EXIT_GAME
                )
            )
            .width(310f)
            .height(48f)
            .padTop(9f)
            .row();

        panel
            .add(
                label(
                    "ESC  RESUME",
                    0.64f,
                    MUTED
                )
            )
            .center()
            .padTop(20f)
            .row();

        root
            .add(panel)
            .width(430f);

        addAnimatedActor(root);
    }

    // =========================================================
    // BUTTON
    // =========================================================

    private TextButton menuButton(
        String text,
        final Action action
    ) {

        TextButton button =
            new TextButton(
                text,
                skin,
                "menu"
            );

        button
            .getLabel()
            .setFontScale(0.82f);

        button.addListener(
            new ClickListener() {

                @Override
                public void clicked(
                    InputEvent event,
                    float x,
                    float y
                ) {

                    pendingAction =
                        action;
                }
            }
        );

        return button;
    }

    // =========================================================
    // LABEL
    // =========================================================

    private Label label(
        String text,
        float scale,
        Color color
    ) {

        Label label =
            new Label(
                text,
                skin
            );

        label.setColor(
            color
        );

        label.setFontScale(
            scale
        );

        return label;
    }

    // =========================================================
    // ANIMATION
    // =========================================================

    private void addAnimatedActor(
        Actor actor
    ) {

        actor.getColor().a =
            0f;

        stage.addActor(
            actor
        );

        actor.addAction(
            Actions.fadeIn(
                0.28f,
                Interpolation.fade
            )
        );
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    public void showSettings() {

        settingsOpen =
            true;

        stage.clear();

        Table root =
            new Table();

        root.setFillParent(true);

        root.center();

        Table panel =
            new Table();

        panel.setBackground(
            panelSoftDrawable
        );

        panel.pad(
            36f,
            44f,
            32f,
            44f
        );

        panel
            .add(
                label(
                    "SETTINGS",
                    2.15f,
                    Color.WHITE
                )
            )
            .center()
            .row();

        panel
            .add(
                label(
                    "Presentation",
                    0.72f,
                    GOLD
                )
            )
            .left()
            .padTop(20f)
            .row();

        panel
            .add(
                label(
                    "Resolution: Desktop / Fullscreen",
                    0.82f,
                    CREAM
                )
            )
            .left()
            .padTop(8f)
            .row();

        panel
            .add(
                label(
                    "VSync: Enabled",
                    0.82f,
                    CREAM
                )
            )
            .left()
            .padTop(5f)
            .row();

        panel
            .add(
                label(
                    "Camera: Mouse controlled",
                    0.82f,
                    CREAM
                )
            )
            .left()
            .padTop(5f)
            .row();

        panel
            .add(
                label(
                    "Audio: Ready for project assets",
                    0.82f,
                    MUTED
                )
            )
            .left()
            .padTop(5f)
            .row();

        panel
            .add(
                menuButton(
                    "BACK",
                    Action.CLOSE_SETTINGS
                )
            )
            .width(220f)
            .height(48f)
            .padTop(24f)
            .row();

        panel
            .add(
                label(
                    "ESC  BACK",
                    0.64f,
                    MUTED
                )
            )
            .center()
            .padTop(12f)
            .row();

        root
            .add(panel)
            .width(440f);

        addAnimatedActor(root);

        setInteractive(true);
    }

    // =========================================================
    // EXIT CONFIRMATION
    // =========================================================

    public void showExitConfirmation(
        boolean fromMainMenu
    ) {

        exitConfirmation =
            true;

        stage.clear();

        Table root =
            new Table();

        root.setFillParent(true);

        root.center();

        Table panel =
            new Table();

        panel.setBackground(
            panelDrawable
        );

        panel.pad(
            34f,
            44f,
            30f,
            44f
        );

        panel
            .add(
                label(
                    fromMainMenu
                        ? "EXIT 2x12?"
                        : "LEAVE GAME?",
                    2.0f,
                    Color.WHITE
                )
            )
            .center()
            .row();

        panel
            .add(
                label(
                    "Are you sure you want to leave this session?",
                    0.82f,
                    CREAM
                )
            )
            .center()
            .padTop(12f)
            .row();

        panel
            .add(
                menuButton(
                    "YES, EXIT",
                    Action.CONFIRM_EXIT
                )
            )
            .width(270f)
            .height(50f)
            .padTop(22f)
            .row();

        panel
            .add(
                menuButton(
                    "NO, GO BACK",
                    Action.CANCEL_EXIT
                )
            )
            .width(270f)
            .height(50f)
            .padTop(9f)
            .row();

        panel
            .add(
                label(
                    "ENTER  CONFIRM        ESC  CANCEL",
                    0.64f,
                    MUTED
                )
            )
            .center()
            .padTop(16f)
            .row();

        root
            .add(panel)
            .width(430f);

        addAnimatedActor(root);

        setInteractive(true);
    }

    // =========================================================
    // BACKDROP
    // =========================================================

    /**
     * Draws a subtle cinematic backdrop for menu screens.
     *
     * No external image dependency is required.
     */
    public void renderBackdrop(
        boolean dark
    ) {

        float width =
            Gdx.graphics.getWidth();

        float height =
            Gdx.graphics.getHeight();

        shapes.setProjectionMatrix(
            stage.getCamera().combined
        );

        shapes.begin(
            ShapeRenderer.ShapeType.Filled
        );

        if (dark) {

            shapes.setColor(
                new Color(
                    0.018f,
                    0.025f,
                    0.034f,
                    1f
                )
            );

        } else {

            shapes.setColor(
                new Color(
                    0.03f,
                    0.04f,
                    0.05f,
                    1f
                )
            );
        }

        shapes.rect(
            0f,
            0f,
            width,
            height
        );

        shapes.setColor(
            new Color(
                0.10f,
                0.12f,
                0.13f,
                0.30f
            )
        );

        shapes.rect(
            0f,
            height * 0.62f,
            width,
            height * 0.38f
        );

        shapes.setColor(
            new Color(
                GOLD.r,
                GOLD.g,
                GOLD.b,
                0.45f
            )
        );

        shapes.rect(
            72f,
            78f,
            2f,
            height - 156f
        );

        shapes.rect(
            72f,
            height - 82f,
            Math.min(
                560f,
                width * 0.42f
            ),
            2f
        );

        shapes.end();
    }

    // =========================================================
    // RENDER
    // =========================================================

    public void render(
        float delta
    ) {

        stage.act(
            Math.min(
                delta,
                0.05f
            )
        );

        stage.draw();
    }

    // =========================================================
    // GAMEPLAY OVERLAY
    // =========================================================

    /**
     * Extension point for future health/stamina/ability UI.
     *
     * The existing Level1World already owns the current mission HUD,
     * therefore this method intentionally does not duplicate it.
     */
    public void renderGameplayOverlay(
        float delta
    ) {

        // Reserved for future gameplay values.
    }

    // =========================================================
    // INPUT MODE
    // =========================================================

    /**
     * Switches between menu mouse interaction and gameplay camera mode.
     *
     * UI mode:
     * - cursor visible
     * - Stage receives mouse input
     *
     * Gameplay mode:
     * - cursor captured
     * - existing Level1World camera controls the mouse
     */
    public void setInteractive(
        boolean interactive
    ) {

        if (interactive) {

            Gdx.input.setCursorCatched(
                false
            );

            Gdx.input.setInputProcessor(
                stage
            );

        } else {

            Gdx.input.setInputProcessor(
                null
            );

            Gdx.input.setCursorCatched(
                true
            );
        }
    }

    // =========================================================
    // ACTION QUEUE
    // =========================================================

    public Action consumeAction() {

        Action action =
            pendingAction;

        pendingAction =
            Action.NONE;

        return action;
    }

    // =========================================================
    // STATE FLAGS
    // =========================================================

    public boolean isSettingsOpen() {

        return settingsOpen;
    }

    public boolean isExitConfirmation() {

        return exitConfirmation;
    }

    public void closeSettings() {

        settingsOpen =
            false;

        pendingAction =
            Action.CLOSE_SETTINGS;
    }

    public void cancelExitConfirmation() {

        exitConfirmation =
            false;

        pendingAction =
            Action.CANCEL_EXIT;
    }

    public void clearOverlayFlags() {

        settingsOpen =
            false;

        exitConfirmation =
            false;

        activeState =
            null;
    }

    // =========================================================
    // STAGE
    // =========================================================

    public Stage getStage() {

        return stage;
    }

    // =========================================================
    // DISPOSE
    // =========================================================

    @Override
    public void dispose() {

        stage.dispose();

        skin.dispose();

        shapes.dispose();

        if (panelTexture != null) {
            panelTexture.dispose();
        }

        if (panelSoftTexture != null) {
            panelSoftTexture.dispose();
        }

        if (buttonTexture != null) {
            buttonTexture.dispose();
        }

        if (buttonOverTexture != null) {
            buttonOverTexture.dispose();
        }

        if (buttonDownTexture != null) {
            buttonDownTexture.dispose();
        }
    }
}