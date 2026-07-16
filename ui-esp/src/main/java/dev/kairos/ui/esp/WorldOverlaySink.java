package dev.kairos.ui.esp;

/** Native endpoint hook called during the world render pass with depth policy controlled by the module. */
public interface WorldOverlaySink {
    void drawBox(WorldBounds bounds, int argb, float lineWidth, boolean throughWalls);
}
