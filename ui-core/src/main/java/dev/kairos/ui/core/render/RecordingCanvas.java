package dev.kairos.ui.core.render;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.render.UiCanvas;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RecordingCanvas implements UiCanvas {
    private final List<RenderCommand> commands = new ArrayList<RenderCommand>();
    private int clipDepth;

    @Override public void fillRect(Rect rect, int argb) { add(RenderCommand.Type.FILL_RECT, rect, null, 0f, argb); }
    @Override public void roundedRect(Rect rect, float radius, int argb) { add(RenderCommand.Type.ROUNDED_RECT, rect, null, radius, argb); }
    @Override public void glass(Rect rect, float cornerRadius, float blurRadius, int tintArgb) {
        add(RenderCommand.Type.GLASS, rect, "blur:" + blurRadius, cornerRadius, tintArgb);
    }
    @Override public void text(String fontId, String text, float x, float baseline, float size, int argb) {
        add(RenderCommand.Type.TEXT, new Rect(x, baseline - size, 0f, size), fontId + ":" + text, 0f, argb);
    }
    @Override public void image(String textureId, Rect rect, int tintArgb) { add(RenderCommand.Type.IMAGE, rect, textureId, 0f, tintArgb); }
    @Override public void pushClip(Rect rect) { clipDepth++; add(RenderCommand.Type.PUSH_CLIP, rect, null, 0f, 0); }
    @Override public void popClip() {
        if (clipDepth <= 0) throw new IllegalStateException("Unbalanced clip stack");
        clipDepth--;
        add(RenderCommand.Type.POP_CLIP, Rect.ZERO, null, 0f, 0);
    }

    private void add(RenderCommand.Type type, Rect rect, String text, float radius, int color) {
        commands.add(new RenderCommand(type, rect, text, radius, color));
    }

    public List<RenderCommand> getCommands() { return Collections.unmodifiableList(commands); }
    public int getClipDepth() { return clipDepth; }
}
