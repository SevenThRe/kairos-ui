package dev.kairos.ui.render.opengl.font;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FontAtlas {
    public final int width;
    public final int height;
    public final byte[] alpha;
    private final Map<Integer, Glyph> glyphs;

    public FontAtlas(int width, int height, byte[] alpha, Map<Integer, Glyph> glyphs) {
        this.width = width;
        this.height = height;
        this.alpha = alpha;
        this.glyphs = Collections.unmodifiableMap(new LinkedHashMap<Integer, Glyph>(glyphs));
    }

    public Glyph get(int codePoint) { return glyphs.get(codePoint); }
    public Map<Integer, Glyph> getGlyphs() { return glyphs; }
}
