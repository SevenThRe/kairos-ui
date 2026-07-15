package dev.kairos.ui.render.opengl.font;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FontAtlasBuilder {
    private final int padding;
    private final int maximumSize;

    public FontAtlasBuilder(int padding, int maximumSize) {
        if (padding < 0 || maximumSize < 32) throw new IllegalArgumentException("Invalid atlas limits");
        this.padding = padding;
        this.maximumSize = maximumSize;
    }

    public FontAtlas build(int[] codePoints, GlyphRasterizer rasterizer) {
        List<GlyphBitmap> bitmaps = new ArrayList<GlyphBitmap>();
        for (int codePoint : codePoints) bitmaps.add(rasterizer.rasterize(codePoint));
        int size = 32;
        while (size <= maximumSize) {
            FontAtlas atlas = tryPack(size, bitmaps);
            if (atlas != null) return atlas;
            size *= 2;
        }
        throw new IllegalStateException("Glyph set does not fit in " + maximumSize + "x" + maximumSize + " atlas");
    }

    private FontAtlas tryPack(int size, List<GlyphBitmap> bitmaps) {
        int x = padding;
        int y = padding;
        int rowHeight = 0;
        Map<Integer, Glyph> glyphs = new LinkedHashMap<Integer, Glyph>();
        byte[] pixels = new byte[size * size];
        for (GlyphBitmap bitmap : bitmaps) {
            if (bitmap.width + padding * 2 > size || bitmap.height + padding * 2 > size) return null;
            if (x + bitmap.width + padding > size) {
                x = padding;
                y += rowHeight + padding;
                rowHeight = 0;
            }
            if (y + bitmap.height + padding > size) return null;
            for (int row = 0; row < bitmap.height; row++) {
                System.arraycopy(bitmap.alpha, row * bitmap.width, pixels, (y + row) * size + x, bitmap.width);
            }
            glyphs.put(bitmap.codePoint, new Glyph(bitmap, x, y));
            x += bitmap.width + padding;
            rowHeight = Math.max(rowHeight, bitmap.height);
        }
        return new FontAtlas(size, size, pixels, glyphs);
    }
}
