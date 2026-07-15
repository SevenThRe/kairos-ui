package dev.kairos.ui.render.opengl;

import dev.kairos.ui.core.render.RenderCommand;

/** Preserves command order while batching adjacent shapes and sharing a single blur pass. */
public final class CommandRenderer {
    public void render(FrameRenderPlan plan, GlCommandSink sink) {
        if (plan.getBlur().isEnabled()) sink.captureAndBlur(plan.getBlur());
        ShapeBatch shapes = new ShapeBatch();
        for (RenderCommand command : plan.getCommands()) {
            if (isShape(command)) {
                if (command.type == RenderCommand.Type.GLASS && plan.getBlur().isEnabled()) {
                    flush(shapes, sink);
                    sink.drawBlurredRegion(command.rect, command.radius, command.color);
                } else {
                    shapes.add(command);
                }
            } else if (command.type == RenderCommand.Type.PUSH_CLIP) {
                flush(shapes, sink);
                sink.pushScissor(command.rect);
            } else if (command.type == RenderCommand.Type.POP_CLIP) {
                flush(shapes, sink);
                sink.popScissor();
            } else if (command.type == RenderCommand.Type.TEXT) {
                flush(shapes, sink);
                sink.drawText(command.text, command.rect, command.color);
            } else if (command.type == RenderCommand.Type.IMAGE) {
                flush(shapes, sink);
                sink.drawImage(command.text, command.rect, command.color);
            } else {
                flush(shapes, sink);
            }
        }
        flush(shapes, sink);
    }

    private static boolean isShape(RenderCommand command) {
        return command.type == RenderCommand.Type.FILL_RECT
            || command.type == RenderCommand.Type.ROUNDED_RECT
            || command.type == RenderCommand.Type.GLASS;
    }

    private static void flush(ShapeBatch shapes, GlCommandSink sink) {
        if (shapes.getShapeCount() == 0) return;
        sink.drawShapes(shapes.toArray(), shapes.getVertexCount());
        shapes.clear();
    }
}
