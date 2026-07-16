package dev.kairos.ui.components.hud;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.render.UiCanvas;

/** Portable fallback; texture IDs are resolved by native backends or preview canvases. */
public final class SemanticGameVisualRenderer implements GameVisualRenderer {
    @Override public void drawEntity(UiCanvas canvas, String id, Rect bounds, float yaw, int tintArgb) {
        canvas.image("entity:" + id + ":yaw=" + Math.round(yaw), bounds, tintArgb);
    }

    @Override public void drawItem(UiCanvas canvas, EquipmentVisual item, Rect bounds, int tintArgb) {
        canvas.image("item:" + item.getVisualId(), bounds, tintArgb);
    }
}
