package dev.kairos.ui.api.render;

import dev.kairos.ui.api.geometry.Rect;

public interface UiCanvas {
    void fillRect(Rect rect, int argb);
    void roundedRect(Rect rect, float radius, int argb);
    /** Requests shared-backdrop blur plus tint; backends must provide a layered translucent fallback. */
    void glass(Rect rect, float cornerRadius, float blurRadius, int tintArgb);
    void text(String fontId, String text, float x, float baseline, float size, int argb);
    void image(String textureId, Rect rect, int tintArgb);
    void pushClip(Rect rect);
    void popClip();
}
