package dev.kairos.ui.render.opengl;

import dev.kairos.ui.api.geometry.Rect;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BlurPlan {
    private final boolean enabled;
    private final int textureWidth;
    private final int textureHeight;
    private final int passes;
    private final List<Rect> regions;

    public BlurPlan(boolean enabled, int textureWidth, int textureHeight, int passes, List<Rect> regions) {
        this.enabled = enabled;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.passes = passes;
        this.regions = Collections.unmodifiableList(new ArrayList<Rect>(regions));
    }

    public boolean isEnabled() { return enabled; }
    public int getTextureWidth() { return textureWidth; }
    public int getTextureHeight() { return textureHeight; }
    public int getPasses() { return passes; }
    public List<Rect> getRegions() { return regions; }
}
