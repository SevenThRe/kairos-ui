package dev.kairos.ui.esp;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.render.UiCanvas;
import java.util.List;

public final class WorldObjectEspRenderer {
    public void render2d(UiCanvas canvas, List<WorldObjectEsp> objects, WorldToScreenProjector projector) {
        EspRenderer projectorHelper = new EspRenderer();
        for (WorldObjectEsp object : objects) {
            Rect r = projectorHelper.projectBounds(object.getBounds(), projector);
            if (r == null) continue;
            outline(canvas, r, 0xE9000000, 3f);
            outline(canvas, r, object.getColor(), 1f);
            if (!object.getLabel().isEmpty()) {
                float w = canvas.measureText("jetbrains-mono", object.getLabel(), 8f);
                float x = r.getX() + (r.getWidth() - w) * .5f;
                canvas.fillRect(new Rect(x - 3f, r.getY() - 13f, w + 6f, 10f), 0xC9000000);
                canvas.text("jetbrains-mono", object.getLabel(), x, r.getY() - 5f, 8f, object.getColor());
            }
        }
    }

    public void render3d(List<WorldObjectEsp> objects, WorldOverlaySink sink, boolean throughWalls) {
        for (WorldObjectEsp object : objects) sink.drawBox(object.getBounds(), object.getColor(), 1.5f, throughWalls);
    }

    private void outline(UiCanvas canvas, Rect r, int color, float width) {
        canvas.fillRect(new Rect(r.getX(), r.getY(), r.getWidth(), width), color);
        canvas.fillRect(new Rect(r.getX(), r.getBottom() - width, r.getWidth(), width), color);
        canvas.fillRect(new Rect(r.getX(), r.getY(), width, r.getHeight()), color);
        canvas.fillRect(new Rect(r.getRight() - width, r.getY(), width, r.getHeight()), color);
    }
}
