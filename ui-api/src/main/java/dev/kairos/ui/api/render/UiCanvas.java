package dev.kairos.ui.api.render;

import dev.kairos.ui.api.geometry.Rect;

public interface UiCanvas {
    void fillRect(Rect rect, int argb);
    void roundedRect(Rect rect, float radius, int argb);
    /** Requests shared-backdrop blur plus tint; backends must provide a layered translucent fallback. */
    void glass(Rect rect, float cornerRadius, float blurRadius, int tintArgb);
    void text(String fontId, String text, float x, float baseline, float size, int argb);
    /**
     * Measures text for right-aligned HUD elements and content-sized surfaces.
     * Backends with a real font atlas should override this; the fallback keeps
     * third-party canvases source-compatible while remaining deterministic.
     */
    default float measureText(String fontId, String text, float size) {
        return text == null ? 0f : text.length() * size * 0.56f;
    }
    void image(String textureId, Rect rect, int tintArgb);
    void pushClip(Rect rect);
    void popClip();
}
