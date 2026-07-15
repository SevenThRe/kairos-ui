package dev.kairos.ui.platform;

public final class RenderCapabilities {
    public final boolean shaders;
    public final boolean framebuffer;
    public final boolean blur;
    public final boolean stencil;
    public final boolean msdfText;

    public RenderCapabilities(boolean shaders, boolean framebuffer, boolean blur,
                              boolean stencil, boolean msdfText) {
        this.shaders = shaders;
        this.framebuffer = framebuffer;
        this.blur = blur;
        this.stencil = stencil;
        this.msdfText = msdfText;
    }

    public static RenderCapabilities basic() {
        return new RenderCapabilities(false, false, false, false, false);
    }
}
