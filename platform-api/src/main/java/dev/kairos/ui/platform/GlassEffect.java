package dev.kairos.ui.platform;

import dev.kairos.ui.api.geometry.Rect;

public interface GlassEffect {
    /** Captures the scene once, then renders all registered regions from the shared blur pass. */
    void beginSharedPass();
    void addRegion(Rect region, float radius, float opacity);
    void endSharedPass();
}
