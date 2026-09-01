package bd.historicalgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * High-resolution, desktop UI renderer for 2x12.
 *
 * This class deliberately does NOT install an InputProcessor. The existing
 * keyboard/gameplay polling in Main remains authoritative. Mouse interaction
 * is added through hit-testing only, so gameplay input is not stolen.
 *
 * The renderer uses Java2D text rasterisation at 2x supersampling. This keeps
 * the UI crisp without requiring a font file to be bundled with the game.
 * The logical font families (SansSerif/Serif) resolve to the platform's own
 * installed fonts.
 */
public class UIManager implements Disposable {

    public enum Overlay {
        NONE,
        SETTINGS,
        EXIT_CONFIRMATION
    }

    public static final float DESIGN_WIDTH = 1920f;
    public static final float DESIGN_HEIGHT = 1080f;

    private static final String FONT_SANS = "SansSerif";
    private static final String FONT_SERIF = "Serif";

    // Refined wuxia-inspired palette: dark ink, warm ivory and restrained gold.
    private static final Color INK = color("081016", 1f);
    private static final Color INK_2 = color("0D171D", 0.92f);
    private static final Color PANEL = color("0A1218", 0.88f);
    private static final Color PANEL_2 = color("111D23", 0.82f);
    private static final Color IVORY = color("F4EFE4", 1f);
    private static final Color PAPER = color("D9D2C4", 1f);
    private static final Color MUTED = color("9CA19B", 1f);
    private static final Color GOLD = color("D7B36A", 1f);
    private static final Color GOLD_BRIGHT = color("F0D28A", 1f);
    private static final Color GOLD_DARK = color("8D6C39", 1f);
    private static final Color LINE = color("D7B36A", 0.44f);
    private static final Color SHADOW = color("000000", 0.70f);

    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final Map<String, Texture> textCache = new HashMap<String, Texture>();

    private Texture backgroundTexture;

    private float time;

    private int hoveredMain = -1;
    private int hoveredPause = -1;
    private int hoveredExit = -1;
    private boolean hoveredSettingsBack;

    private Overlay overlay = Overlay.NONE;
    private boolean settingsFromPause;
    private boolean exitFromMainMenu;

    public UIManager() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        backgroundTexture = createAtmosphericBackground();
    }

    public void update(float delta) {
        time += Math.min(Math.max(delta, 0f), 0.05f);
    }

    // ---------------------------------------------------------------------
    // Coordinate helpers
    // ---------------------------------------------------------------------

    private float scale() {
        return Math.min(
            Gdx.graphics.getWidth() / DESIGN_WIDTH,
            Gdx.graphics.getHeight() / DESIGN_HEIGHT
        );
    }

    private float offsetX() {
        return (Gdx.graphics.getWidth() - DESIGN_WIDTH * scale()) * 0.5f;
    }

    private float offsetY() {
        return (Gdx.graphics.getHeight() - DESIGN_HEIGHT * scale()) * 0.5f;
    }

    private float sx(float x) {
        return offsetX() + x * scale();
    }

    private float sy(float y) {
        return offsetY() + y * scale();
    }

    private float sw(float value) {
        return value * scale();
    }

    private float sh(float value) {
        return value * scale();
    }

    private float mouseXDesign() {
        return (Gdx.input.getX() - offsetX()) / scale();
    }

    private float mouseYDesign() {
        return (Gdx.graphics.getHeight() - Gdx.input.getY() - offsetY()) / scale();
    }

    // ---------------------------------------------------------------------
    // Hit testing. These are public so Main can preserve its keyboard logic.
    // ---------------------------------------------------------------------

    public int getMainButtonAtMouse() {
        float x = mouseXDesign();
        float y = mouseYDesign();
        for (int i = 0; i < 3; i++) {
            if (inRect(x, y, 1180f, 475f + i * 78f, 470f, 58f)) return i;
        }
        return -1;
    }

    public int getPauseButtonAtMouse() {
        float x = mouseXDesign();
        float y = mouseYDesign();
        for (int i = 0; i < 4; i++) {
            if (inRect(x, y, 760f, 405f + i * 68f, 400f, 52f)) return i;
        }
        return -1;
    }

    public int getExitButtonAtMouse() {
        float x = mouseXDesign();
        float y = mouseYDesign();
        if (inRect(x, y, 770f, 575f, 180f, 54f)) return 0;
        if (inRect(x, y, 970f, 575f, 180f, 54f)) return 1;
        return -1;
    }

    public boolean isSettingsBackAtMouse() {
        float x = mouseXDesign();
        float y = mouseYDesign();
        return inRect(x, y, 760f, 735f, 250f, 56f);
    }

    private boolean inRect(float x, float y, float rx, float ry, float rw, float rh) {
        return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
    }

    // ---------------------------------------------------------------------
    // Main menu
    // ---------------------------------------------------------------------

    public void renderMainMenu(int selectedOption) {
        renderBackdrop(false);

        hoveredMain = getMainButtonAtMouse();

        beginShapes();
        drawMainAtmosphere();
        endShapes();

        // left-side identity block
        drawText("DHAKA UNIVERSITY CAMPUS", FONT_SANS, 17f, GOLD, 92f, 922f, false, true);
        drawText("HISTORICAL ADVENTURE", FONT_SANS, 15f, MUTED, 92f, 888f, false, false);

        // large title, intentionally minimal rather than copying another game's title layout
        drawText("2x12", FONT_SERIF, 118f, IVORY, 1180f, 826f, false, true);
        drawText("WHERE IT ALL BEGAN", FONT_SANS, 24f, PAPER, 1190f, 752f, false, true);

        drawLine(1188f, 722f, 1480f, 722f, GOLD, 2.0f);
        drawText("A STORY OF PLACE, MEMORY & DISCOVERY", FONT_SANS, 13f, MUTED, 1192f, 690f, false, false);

        // menu
        String[] options = {"BEGIN JOURNEY", "SETTINGS", "EXIT"};
        for (int i = 0; i < options.length; i++) {
            int stateIndex = hoveredMain >= 0 ? hoveredMain : selectedOption;
            boolean active = i == stateIndex;
            float y = 475f + i * 78f;
            drawText(options[i], FONT_SANS, active ? 29f : 26f,
                active ? GOLD_BRIGHT : IVORY,
                1210f, y + 39f, false, active);
            if (active) {
                drawLine(1179f, y + 7f, 1200f, y + 7f, GOLD_BRIGHT, 3.2f);
                drawLine(1210f, y - 7f, 1470f, y - 7f, GOLD, 0.85f);
            }
        }

        // footer
        drawText("v0.1.0", FONT_SANS, 13f, MUTED, 92f, 88f, false, false);
        drawText("↑ ↓  NAVIGATE     ENTER / SPACE  SELECT     ESC / Q  EXIT", FONT_SANS,
            13f, MUTED, 1184f, 88f, false, false);
    }

    // ---------------------------------------------------------------------
    // Level 1 intro
    // ---------------------------------------------------------------------

    public void renderIntro() {
        renderBackdrop(false);

        beginShapes();
        drawIntroAtmosphere();
        endShapes();

        drawText("LEVEL 01", FONT_SANS, 18f, GOLD, 92f, 908f, false, true);
        drawText("WHERE IT ALL BEGAN", FONT_SERIF, 68f, IVORY, 92f, 836f, false, true);
        drawText("MID-JUNE 2024", FONT_SANS, 20f, GOLD_BRIGHT, 96f, 760f, false, true);

        drawPanel(90f, 565f, 740f, 132f, PANEL);
        drawText("A normal day is about to change.", FONT_SANS, 27f, PAPER, 122f, 650f, false, true);
        drawText("Explore the campus and discover what is happening.", FONT_SANS,
            17f, MUTED, 122f, 612f, false, false);

        drawText("ENTER", FONT_SANS, 22f, GOLD_BRIGHT, 122f, 535f, false, true);
        drawText("BEGIN JOURNEY", FONT_SANS, 22f, IVORY, 220f, 535f, false, true);
        drawText("Q / ESC  EXIT", FONT_SANS, 13f, MUTED, 122f, 495f, false, false);
    }

    // ---------------------------------------------------------------------
    // Pause menu
    // ---------------------------------------------------------------------

    public void renderPause(int selectedOption) {
        renderOverlayShade(0.54f);
        hoveredPause = getPauseButtonAtMouse();

        drawPanel(675f, 315f, 570f, 500f, PANEL);
        drawText("PAUSED", FONT_SERIF, 65f, IVORY, 960f, 756f, true, true);
        drawText("DHAKA UNIVERSITY CAMPUS", FONT_SANS, 14f, GOLD, 960f, 714f, true, false);
        drawLine(790f, 683f, 1130f, 683f, GOLD, 1.5f);

        String[] options = {"RESUME", "SETTINGS", "EXIT TO MAIN MENU", "EXIT GAME"};
        for (int i = 0; i < options.length; i++) {
            int stateIndex = hoveredPause >= 0 ? hoveredPause : selectedOption;
            boolean active = i == stateIndex;
            float y = 405f + i * 68f;
            drawText(options[i], FONT_SANS, active ? 23f : 21f,
                active ? GOLD_BRIGHT : IVORY,
                805f, y + 31f, false, active);
            if (active) drawLine(770f, y + 7f, 793f, y + 7f, GOLD_BRIGHT, 3f);
        }

        drawText("ESC  RESUME", FONT_SANS, 13f, MUTED, 960f, 350f, true, false);
    }

    // ---------------------------------------------------------------------
    // Settings
    // ---------------------------------------------------------------------

    public void renderSettings() {
        hoveredSettingsBack = isSettingsBackAtMouse();
        drawOverlayShade(0.62f);

        drawPanel(700f, 205f, 520f, 670f, PANEL_2);
        drawText("SETTINGS", FONT_SERIF, 58f, IVORY, 960f, 798f, true, true);
        drawText("PRESENTATION", FONT_SANS, 14f, GOLD, 760f, 741f, false, true);

        settingRow("DISPLAY", "Desktop / Fullscreen", 688f);
        settingRow("VSYNC", "Enabled", 638f);
        settingRow("CAMERA", "Mouse controlled", 588f);
        settingRow("CONTROLS", "W A S D  /  SHIFT  /  ESC", 538f);
        settingRow("TEXT", "High-resolution raster UI", 488f);

        drawLine(760f, 445f, 1160f, 445f, LINE, 1f);
        drawText("The visual layer is independent from gameplay input.", FONT_SANS,
            14f, MUTED, 760f, 408f, false, false);
        drawText("Your existing keyboard controls remain active in-game.", FONT_SANS,
            14f, MUTED, 760f, 382f, false, false);

        boolean active = hoveredSettingsBack;
        drawPanel(760f, 735f, 250f, 56f, active ? GOLD_DARK : INK);
        drawText("BACK", FONT_SANS, 20f, active ? IVORY : PAPER, 885f, 771f, true, true);
        drawText("ESC  BACK", FONT_SANS, 13f, MUTED, 960f, 265f, true, false);
    }

    private void settingRow(String label, String value, float y) {
        drawText(label, FONT_SANS, 13f, GOLD, 760f, y, false, true);
        drawText(value, FONT_SANS, 17f, PAPER, 915f, y, false, false);
        drawLine(760f, y - 16f, 1160f, y - 16f, color("A7AFAB", 0.18f), 1f);
    }

    // ---------------------------------------------------------------------
    // Exit confirmation
    // ---------------------------------------------------------------------

    public void renderExitConfirmation(int selection, boolean fromMainMenu) {
        hoveredExit = getExitButtonAtMouse();
        drawOverlayShade(0.68f);
        drawPanel(720f, 355f, 480f, 360f, PANEL_2);

        drawText(fromMainMenu ? "EXIT 2x12?" : "LEAVE GAME?", FONT_SERIF,
            48f, IVORY, 960f, 637f, true, true);
        drawText("Are you sure you want to leave this session?", FONT_SANS,
            16f, PAPER, 960f, 587f, true, false);

        int stateIndex = hoveredExit >= 0 ? hoveredExit : selection;
        boolean yesActive = stateIndex == 0;
        boolean noActive = stateIndex == 1;

        drawPanel(770f, 575f, 180f, 54f, yesActive ? GOLD_DARK : INK);
        drawText("YES", FONT_SANS, 19f, yesActive ? IVORY : PAPER, 860f, 610f, true, true);

        drawPanel(970f, 575f, 180f, 54f, noActive ? GOLD_DARK : INK);
        drawText("NO", FONT_SANS, 19f, noActive ? IVORY : PAPER, 1060f, 610f, true, true);

        drawText("← →  SELECT     ENTER  CONFIRM     ESC  CANCEL", FONT_SANS,
            13f, MUTED, 960f, 455f, true, false);
    }

    // ---------------------------------------------------------------------
    // Background and atmosphere
    // ---------------------------------------------------------------------

    private void renderBackdrop(boolean bright) {
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        batch.begin();
        batch.draw(backgroundTexture, 0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        if (bright) renderOverlayShade(0.12f); else renderOverlayShade(0.18f);
    }

    private void drawMainAtmosphere() {
        float pulse = (float)(0.5 + 0.5 * Math.sin(time * 0.22));

        // distant rooflines / campus silhouette
        shapes.setColor(color("081016", 0.84f));
        shapes.rect(sx(0f), sy(265f), sw(DESIGN_WIDTH), sh(370f));

        shapes.setColor(color("101C22", 0.58f));
        for (int i = 0; i < 17; i++) {
            float x = 35f + i * 118f;
            float h = 45f + (i % 5) * 14f;
            shapes.rect(sx(x), sy(525f + (i % 3) * 7f), sw(86f), sh(h));
        }

        // restrained light haze on the far horizon
        shapes.setColor(color("D7B36A", 0.055f + pulse * 0.018f));
        shapes.circle(sx(1110f), sy(700f), sw(230f));
        shapes.setColor(color("96BCC0", 0.035f));
        shapes.circle(sx(1670f), sy(620f), sw(280f));

        // subtle vertical rain / dust strokes, deterministic and very soft
        shapes.setColor(color("D7D8D0", 0.035f));
        for (int i = 0; i < 58; i++) {
            float x = (i * 137f + time * (18f + (i % 5) * 3f)) % DESIGN_WIDTH;
            float y = 120f + (i * 47f) % 760f;
            float len = 15f + (i % 6) * 5f;
            drawLine(x, y, x + 5f, y - len, color("D7D8D0", 0.025f), 1f);
        }

        // left gold guide line for the editorial feel
        drawLine(92f, 150f, 92f, 865f, GOLD, 1.2f);
    }

    private void drawIntroAtmosphere() {
        shapes.setColor(color("0E181E", 0.72f));
        shapes.rect(sx(0f), sy(0f), sw(DESIGN_WIDTH), sh(1080f));
        shapes.setColor(color("D7B36A", 0.09f));
        shapes.circle(sx(1500f), sy(680f), sw(330f));
        drawLine(980f, 220f, 1780f, 220f, color("D7B36A", 0.16f), 1f);
        drawLine(980f, 860f, 1780f, 860f, color("D7B36A", 0.10f), 1f);
    }

    private void renderOverlayShade(float alpha) {
        beginShapes();
        shapes.setColor(color("000000", alpha));
        shapes.rect(0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        endShapes();
    }

    private void drawOverlayShade(float alpha) {
        beginShapes();
        shapes.setColor(color("000000", alpha));
        shapes.rect(0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        endShapes();
    }

    // ---------------------------------------------------------------------
    // Primitive UI drawing
    // ---------------------------------------------------------------------

    private void drawPanel(float x, float y, float w, float h, Color color) {
        beginShapes();
        shapes.setColor(color);
        shapes.rect(sx(x), sy(y), sw(w), sh(h));
        shapes.setColor(color("000000", 0.22f));
        shapes.rect(sx(x + 5f), sy(y - 5f), sw(w), sh(5f));
        shapes.setColor(color("D7B36A", 0.12f));
        shapes.rect(sx(x), sy(y + h - 2f), sw(w), sh(2f));
        endShapes();
    }

    private void drawLine(float x1, float y1, float x2, float y2, Color color, float width) {
        shapes.setColor(color);
        shapes.rectLine(sx(x1), sy(y1), sx(x2), sy(y2), sw(width));
    }

    private void drawText(String text, String family, float size, Color color,
                          float x, float y, boolean centered, boolean bold) {
        TextureRegionInfo info = getText(text, family, size, color, bold);
        batch.begin();
        float drawW = info.width * scale() / 2f;
        float drawH = info.height * scale() / 2f;
        float px = sx(x);
        float py = sy(y) - drawH;
        if (centered) px -= drawW / 2f;
        batch.draw(info.texture, px, py, drawW, drawH);
        batch.end();
    }

    private TextureRegionInfo getText(String text, String family, float size, Color color, boolean bold) {
        String key = text + "|" + family + "|" + size + "|" + color.toString() + "|" + bold;
        Texture cached = textCache.get(key);
        if (cached != null) return new TextureRegionInfo(cached, cached.getWidth(), cached.getHeight());

        final int ss = 2;
        int pixelSize = Math.max(8, Math.round(size * ss));
        int style = bold ? Font.BOLD : Font.PLAIN;
        Font font = new Font(family, style, pixelSize);

        BufferedImage probe = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gp = probe.createGraphics();
        applyTextHints(gp);
        FontMetrics metrics = gp.getFontMetrics(font);
        int textW = Math.max(4, metrics.stringWidth(text));
        int textH = Math.max(4, metrics.getAscent() + metrics.getDescent());
        gp.dispose();

        int padding = Math.max(10, pixelSize / 5);
        BufferedImage image = new BufferedImage(textW + padding * 2, textH + padding * 2,
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        applyTextHints(g);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        int baseline = padding + fm.getAscent();

        // Supersampled soft shadow makes small text more readable on busy worlds.
        java.awt.Color awtShadow = new java.awt.Color(0, 0, 0, Math.round(130));
        g.setColor(awtShadow);
        g.drawString(text, padding + 2, baseline + 3);

        java.awt.Color awtColor = new java.awt.Color(
            clamp(Math.round(color.r * 255f)),
            clamp(Math.round(color.g * 255f)),
            clamp(Math.round(color.b * 255f)),
            clamp(Math.round(color.a * 255f))
        );
        g.setColor(awtColor);
        g.drawString(text, padding, baseline);
        g.dispose();

        Texture texture = pixmapFromImage(image);
        textCache.put(key, texture);
        return new TextureRegionInfo(texture, image.getWidth(), image.getHeight());
    }

    private void applyTextHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY);
    }

    private Texture pixmapFromImage(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = image.getRGB(x, y);
                int a = (argb >>> 24) & 0xFF;
                int r = (argb >>> 16) & 0xFF;
                int g = (argb >>> 8) & 0xFF;
                int b = argb & 0xFF;
                int rgba = (r << 24) | (g << 16) | (b << 8) | a;
                pixmap.drawPixel(x, h - 1 - y, rgba);
            }
        }
        Texture texture = new Texture(pixmap, true);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    private Texture createAtmosphericBackground() {
        final int size = 512;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float nx = x / (float)(size - 1);
                float ny = y / (float)(size - 1);
                float horizon = 1f - Math.abs(ny - 0.58f) / 0.58f;
                float warm = (float)Math.exp(-((nx - 0.70f) * (nx - 0.70f) + (ny - 0.57f) * (ny - 0.57f)) / 0.09f);
                float cool = (float)Math.exp(-((nx - 0.93f) * (nx - 0.93f) + (ny - 0.35f) * (ny - 0.35f)) / 0.11f);
                int r = clamp(Math.round(6 + horizon * 9 + warm * 28 + cool * 4));
                int g = clamp(Math.round(13 + horizon * 13 + warm * 20 + cool * 10));
                int b = clamp(Math.round(18 + horizon * 16 + warm * 10 + cool * 18));
                int rgba = (r << 24) | (g << 16) | (b << 8) | 255;
                pixmap.drawPixel(x, size - 1 - y, rgba);
            }
        }
        Texture texture = new Texture(pixmap, true);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    private void beginShapes() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
    }

    private void endShapes() {
        shapes.end();
    }

    private static Color color(String hex, float alpha) {
        Color c = Color.valueOf(hex);
        c.a = alpha;
        return c;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static class TextureRegionInfo {
        final Texture texture;
        final int width;
        final int height;
        TextureRegionInfo(Texture texture, int width, int height) {
            this.texture = texture;
            this.width = width;
            this.height = height;
        }
    }

    public void setSettingsContext(boolean fromPause) {
        settingsFromPause = fromPause;
        overlay = Overlay.SETTINGS;
    }

    public void setExitContext(boolean fromMainMenu) {
        exitFromMainMenu = fromMainMenu;
        overlay = Overlay.EXIT_CONFIRMATION;
    }

    public boolean isSettingsOpen() {
        return overlay == Overlay.SETTINGS;
    }

    public boolean isExitConfirmation() {
        return overlay == Overlay.EXIT_CONFIRMATION;
    }

    public boolean isSettingsFromPause() {
        return settingsFromPause;
    }

    public boolean isExitFromMainMenu() {
        return exitFromMainMenu;
    }

    public void closeOverlay() {
        overlay = Overlay.NONE;
    }

    public Overlay getOverlay() {
        return overlay;
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        for (Texture texture : textCache.values()) texture.dispose();
        textCache.clear();
    }
}
