package dev.kairos.ui.core.render;

import dev.kairos.ui.api.geometry.Rect;

public final class RenderCommand {
    public enum Type { FILL_RECT, ROUNDED_RECT, GLASS, TEXT, IMAGE, PUSH_CLIP, POP_CLIP }
    public final Type type;
    public final Rect rect;
    public final String text;
    public final float radius;
    public final int color;

    public RenderCommand(Type type, Rect rect, String text, float radius, int color) {
        this.type = type;
        this.rect = rect;
        this.text = text;
        this.radius = radius;
        this.color = color;
    }
}
