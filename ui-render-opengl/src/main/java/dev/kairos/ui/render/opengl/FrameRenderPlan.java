package dev.kairos.ui.render.opengl;

import dev.kairos.ui.core.render.RenderCommand;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FrameRenderPlan {
    private final BlurPlan blur;
    private final List<RenderCommand> commands;
    private final int shapeCount;

    public FrameRenderPlan(BlurPlan blur, List<RenderCommand> commands, int shapeCount) {
        this.blur = blur;
        this.commands = Collections.unmodifiableList(new ArrayList<RenderCommand>(commands));
        this.shapeCount = shapeCount;
    }

    public BlurPlan getBlur() { return blur; }
    public List<RenderCommand> getCommands() { return commands; }
    public int getShapeCount() { return shapeCount; }
}
