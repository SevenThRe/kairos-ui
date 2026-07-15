package dev.kairos.ui.platform.legacy;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.platform.ScissorBox;

/** Converts top-left scaled GUI coordinates to bottom-left OpenGL framebuffer pixels. */
public final class Legacy112ScissorMapper {
    private Legacy112ScissorMapper() {}

    public static ScissorBox map(Rect rect, float scaleFactor, int framebufferHeight) {
        if (scaleFactor <= 0f || framebufferHeight <= 0) throw new IllegalArgumentException("Invalid framebuffer");
        int left = (int) Math.floor(rect.getX() * scaleFactor);
        int right = (int) Math.ceil(rect.getRight() * scaleFactor);
        int top = (int) Math.floor(rect.getY() * scaleFactor);
        int bottom = (int) Math.ceil(rect.getBottom() * scaleFactor);
        return new ScissorBox(left, framebufferHeight - bottom, right - left, bottom - top);
    }
}
