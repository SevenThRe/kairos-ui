package dev.kairos.ui.components.scene;

public final class PanelState {
    public float x;
    public float y;
    public float width;
    public boolean collapsed;
    public float scrollOffset;

    public PanelState(float x, float y, float width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }
}
