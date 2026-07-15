package dev.kairos.ui.render.opengl;

import dev.kairos.ui.api.geometry.Rect;

/** Thin adapter implemented with LWJGL2 or the modern Minecraft render system. */
public interface GlCommandSink {
    void captureAndBlur(BlurPlan plan);
    void drawShapes(float[] vertices, int vertexCount);
    void drawBlurredRegion(Rect region, float cornerRadius, int tintArgb);
    void drawText(String encodedFontAndText, Rect bounds, int argb);
    void drawImage(String textureId, Rect region, int tintArgb);
    void pushScissor(Rect region);
    void popScissor();
}
