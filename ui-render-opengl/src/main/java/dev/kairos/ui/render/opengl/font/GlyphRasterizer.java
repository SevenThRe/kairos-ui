package dev.kairos.ui.render.opengl.font;

public interface GlyphRasterizer {
    GlyphBitmap rasterize(int codePoint);
}
