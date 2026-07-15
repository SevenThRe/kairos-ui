package dev.kairos.ui.platform.modern;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.platform.ScissorBox;

/** Converts modern scaled GUI coordinates to the OpenGL scissor coordinate system. */
public final class Modern120ScissorMapper {
    private Modern120ScissorMapper() {}

    public static ScissorBox map(Rect rect, float guiScale, int framebufferHeight) {
        if (guiScale <= 0f || framebufferHeight <= 0) throw new IllegalArgumentException("Invalid framebuffer");
        int left = (int) Math.floor(rect.getX() * guiScale);
        int right = (int) Math.ceil(rect.getRight() * guiScale);
        int top = (int) Math.floor(rect.getY() * guiScale);
        int bottom = (int) Math.ceil(rect.getBottom() * guiScale);
        return new ScissorBox(left, framebufferHeight - bottom, right - left, bottom - top);
    }
}
