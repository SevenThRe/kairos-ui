package dev.kairos.ui.render.opengl;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.core.render.RenderCommand;
import java.util.ArrayList;
import java.util.List;

/** CPU-side interleaved vertex batch compatible with a GLSL 1.20 SDF shader. */
public final class ShapeBatch {
    public static final int FLOATS_PER_VERTEX = 12;
    private static final int VERTICES_PER_QUAD = 6;
    private final List<Float> data = new ArrayList<Float>();
    private int shapes;

    public void add(RenderCommand command) {
        if (command.type != RenderCommand.Type.FILL_RECT
            && command.type != RenderCommand.Type.ROUNDED_RECT
            && command.type != RenderCommand.Type.GLASS) return;
        Rect r = command.rect;
        float radius = command.type == RenderCommand.Type.FILL_RECT ? 0f : command.radius;
        vertex(r.getX(), r.getY(), 0f, 0f, r, radius, command.color);
        vertex(r.getRight(), r.getY(), 1f, 0f, r, radius, command.color);
        vertex(r.getRight(), r.getBottom(), 1f, 1f, r, radius, command.color);
        vertex(r.getX(), r.getY(), 0f, 0f, r, radius, command.color);
        vertex(r.getRight(), r.getBottom(), 1f, 1f, r, radius, command.color);
        vertex(r.getX(), r.getBottom(), 0f, 1f, r, radius, command.color);
        shapes++;
    }

    private void vertex(float x, float y, float u, float v, Rect r, float radius, int argb) {
        data.add(x); data.add(y); data.add(u); data.add(v);
        data.add(r.getWidth()); data.add(r.getHeight()); data.add(radius);
        data.add(((argb >> 16) & 255) / 255f);
        data.add(((argb >> 8) & 255) / 255f);
        data.add((argb & 255) / 255f);
        data.add(((argb >>> 24) & 255) / 255f);
        data.add(0f); // reserved for border/effect flags
    }

    public float[] toArray() {
        float[] result = new float[data.size()];
        for (int i = 0; i < data.size(); i++) result[i] = data.get(i);
        return result;
    }

    public int getShapeCount() { return shapes; }
    public int getVertexCount() { return shapes * VERTICES_PER_QUAD; }
    public void clear() { data.clear(); shapes = 0; }
}
