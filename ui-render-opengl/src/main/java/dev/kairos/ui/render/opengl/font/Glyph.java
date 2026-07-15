package dev.kairos.ui.render.opengl.font;

public final class Glyph {
    public final int codePoint;
    public final int x;
    public final int y;
    public final int width;
    public final int height;
    public final float bearingX;
    public final float bearingY;
    public final float advance;

    public Glyph(GlyphBitmap source, int x, int y) {
        this.codePoint = source.codePoint;
        this.x = x;
        this.y = y;
        this.width = source.width;
        this.height = source.height;
        this.bearingX = source.bearingX;
        this.bearingY = source.bearingY;
        this.advance = source.advance;
    }
}
