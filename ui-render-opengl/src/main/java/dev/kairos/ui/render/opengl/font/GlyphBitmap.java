package dev.kairos.ui.render.opengl.font;

public final class GlyphBitmap {
    public final int codePoint;
    public final int width;
    public final int height;
    public final float bearingX;
    public final float bearingY;
    public final float advance;
    public final byte[] alpha;

    public GlyphBitmap(int codePoint, int width, int height, float bearingX, float bearingY,
                       float advance, byte[] alpha) {
        if (width < 0 || height < 0 || alpha.length != width * height) {
            throw new IllegalArgumentException("Invalid glyph bitmap");
        }
        this.codePoint = codePoint;
        this.width = width;
        this.height = height;
        this.bearingX = bearingX;
        this.bearingY = bearingY;
        this.advance = advance;
        this.alpha = alpha;
    }
}
