package dev.kairos.ui.components.hud;

import dev.kairos.ui.api.geometry.Rect;

public final class HudWidgetState {
    public final String id;
    public Rect bounds;
    public boolean visible = true;
    public float scale = 1f;

    public HudWidgetState(String id, Rect bounds) {
        this.id = id;
        this.bounds = bounds;
    }
}
