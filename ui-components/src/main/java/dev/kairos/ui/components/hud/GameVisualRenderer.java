package dev.kairos.ui.components.hud;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.render.UiCanvas;

/** Hook implemented by Minecraft endpoints for native entity and ItemStack rendering. */
public interface GameVisualRenderer {
    void drawEntity(UiCanvas canvas, String entityVisualId, Rect bounds, float yaw, int tintArgb);
    void drawItem(UiCanvas canvas, EquipmentVisual item, Rect bounds, int tintArgb);
}
