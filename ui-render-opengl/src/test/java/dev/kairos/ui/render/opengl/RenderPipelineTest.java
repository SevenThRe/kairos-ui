package dev.kairos.ui.render.opengl;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.core.render.RecordingCanvas;
import dev.kairos.ui.core.render.RenderCommand;
import dev.kairos.ui.platform.RenderCapabilities;
import dev.kairos.ui.render.opengl.font.*;
import java.util.ArrayList;
import java.util.List;

public final class RenderPipelineTest {
    public static void main(String[] args) {
        sharedBlurIsPlannedOnce();
        shapeVerticesAreBatched();
        commandOrderAndDedicatedDrawsArePreserved();
        glyphsArePackedWithoutOverlap();
        System.out.println("RenderPipelineTest passed");
    }

    private static void sharedBlurIsPlannedOnce() {
        RecordingCanvas canvas = new RecordingCanvas();
        canvas.glass(new Rect(10f, 10f, 200f, 100f), 12f, 18f, 0xCC111820);
        canvas.glass(new Rect(240f, 10f, 200f, 100f), 12f, 18f, 0xCC111820);
        FrameRenderPlan plan = new FramePlanner(4, 2).plan(canvas.getCommands(), 1280, 720,
            new RenderCapabilities(true, true, true, true, true));
        require(plan.getBlur().isEnabled(), "blur enabled");
        require(plan.getBlur().getTextureWidth() == 320, "downsampled width");
        require(plan.getBlur().getRegions().size() == 2, "all glass regions share plan");
    }

    private static void shapeVerticesAreBatched() {
        ShapeBatch batch = new ShapeBatch();
        batch.add(new RenderCommand(RenderCommand.Type.ROUNDED_RECT,
            new Rect(0f, 0f, 100f, 40f), null, 8f, 0xFFFFFFFF));
        require(batch.getShapeCount() == 1, "shape count");
        require(batch.getVertexCount() == 6, "quad triangle vertices");
        require(batch.toArray().length == 6 * ShapeBatch.FLOATS_PER_VERTEX, "interleaved vertex layout");
    }

    private static void glyphsArePackedWithoutOverlap() {
        int[] codePoints = new int[95];
        for (int i = 0; i < codePoints.length; i++) codePoints[i] = 32 + i;
        FontAtlas atlas = new FontAtlasBuilder(1, 512).build(codePoints, new GlyphRasterizer() {
            @Override public GlyphBitmap rasterize(int codePoint) {
                int width = 4 + codePoint % 5;
                int height = 10;
                return new GlyphBitmap(codePoint, width, height, 0f, 8f, width + 1f, new byte[width * height]);
            }
        });
        require(atlas.getGlyphs().size() == 95, "ASCII atlas coverage");
        List<Glyph> glyphs = new ArrayList<Glyph>(atlas.getGlyphs().values());
        for (int i = 0; i < glyphs.size(); i++) {
            for (int j = i + 1; j < glyphs.size(); j++) require(!overlaps(glyphs.get(i), glyphs.get(j)), "glyph overlap");
        }
    }

    private static void commandOrderAndDedicatedDrawsArePreserved() {
        RecordingCanvas canvas = new RecordingCanvas();
        canvas.roundedRect(new Rect(0f, 0f, 40f, 20f), 4f, 0xFFFFFFFF);
        canvas.text("inter-regular", "Kairos", 8f, 16f, 10f, 0xFFFFFFFF);
        canvas.image("kairos:logo", new Rect(50f, 0f, 20f, 20f), 0xFFFFFFFF);
        FrameRenderPlan plan = new FramePlanner(4, 2).plan(canvas.getCommands(), 100, 100,
            RenderCapabilities.basic());
        RecordingSink sink = new RecordingSink();
        new CommandRenderer().render(plan, sink);
        require(sink.calls.toString().equals("[shapes:6, text:inter-regular:Kairos, image:kairos:logo]"),
            "command order and dedicated draws");
    }

    private static boolean overlaps(Glyph a, Glyph b) {
        return a.x < b.x + b.width && a.x + a.width > b.x
            && a.y < b.y + b.height && a.y + a.height > b.y;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class RecordingSink implements GlCommandSink {
        final List<String> calls = new ArrayList<String>();
        @Override public void captureAndBlur(BlurPlan plan) { calls.add("blur"); }
        @Override public void drawShapes(float[] vertices, int vertexCount) { calls.add("shapes:" + vertexCount); }
        @Override public void drawBlurredRegion(Rect region, float cornerRadius, int tintArgb) { calls.add("glass"); }
        @Override public void drawText(String encodedFontAndText, Rect bounds, int argb) {
            calls.add("text:" + encodedFontAndText);
        }
        @Override public void drawImage(String textureId, Rect region, int tintArgb) { calls.add("image:" + textureId); }
        @Override public void pushScissor(Rect region) { calls.add("push"); }
        @Override public void popScissor() { calls.add("pop"); }
    }
}
