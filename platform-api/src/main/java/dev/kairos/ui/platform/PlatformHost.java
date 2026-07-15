package dev.kairos.ui.platform;

import dev.kairos.ui.api.render.UiCanvas;

public interface PlatformHost {
    int getScaledWidth();
    int getScaledHeight();
    float getScaleFactor();
    long nowNanos();
    RenderCapabilities getCapabilities();
    UiCanvas beginFrame();
    void endFrame(UiCanvas canvas);
}
