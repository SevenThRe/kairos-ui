package dev.kairos.ui.render.opengl;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.core.render.RenderCommand;
import dev.kairos.ui.platform.RenderCapabilities;
import java.util.ArrayList;
import java.util.List;

public final class FramePlanner {
    private final int downsample;
    private final int blurPasses;

    public FramePlanner(int downsample, int blurPasses) {
        if (downsample < 1 || blurPasses < 1) throw new IllegalArgumentException("Invalid blur quality");
        this.downsample = downsample;
        this.blurPasses = blurPasses;
    }

    public FrameRenderPlan plan(List<RenderCommand> commands, int framebufferWidth, int framebufferHeight,
                                RenderCapabilities capabilities) {
        List<Rect> glass = new ArrayList<Rect>();
        int shapes = 0;
        for (RenderCommand command : commands) {
            if (command.type == RenderCommand.Type.GLASS) glass.add(command.rect);
            if (command.type == RenderCommand.Type.FILL_RECT
                || command.type == RenderCommand.Type.ROUNDED_RECT
                || command.type == RenderCommand.Type.GLASS) shapes++;
        }
        boolean blur = !glass.isEmpty() && capabilities.framebuffer && capabilities.blur;
        BlurPlan blurPlan = new BlurPlan(blur,
            blur ? Math.max(1, framebufferWidth / downsample) : 0,
            blur ? Math.max(1, framebufferHeight / downsample) : 0,
            blur ? blurPasses : 0, glass);
        return new FrameRenderPlan(blurPlan, commands, shapes);
    }
}
