package dev.kairos.ui.components.hud;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.render.UiCanvas;

/** Explicit headless fallback: reserves native visual slots without drawing fake game content. */
public final class NoopGameVisualRenderer implements GameVisualRenderer {
    @Override public void drawEntity(UiCanvas canvas, String entityVisualId, Rect bounds, float yaw, int tintArgb) {}
    @Override public void drawItem(UiCanvas canvas, EquipmentVisual item, Rect bounds, int tintArgb) {}
}
