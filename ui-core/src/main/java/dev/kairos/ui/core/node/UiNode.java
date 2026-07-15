package dev.kairos.ui.core.node;

import dev.kairos.ui.api.geometry.Constraints;
import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.geometry.Size;
import dev.kairos.ui.api.input.EventResult;
import dev.kairos.ui.api.input.PointerEvent;
import dev.kairos.ui.api.input.UiKeyEvent;
import dev.kairos.ui.api.render.UiCanvas;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UiNode {
    private UiNode parent;
    private final List<UiNode> children = new ArrayList<UiNode>();
    private Rect bounds = Rect.ZERO;
    private boolean visible = true;
    private boolean enabled = true;
    private boolean focusable;

    public UiNode add(UiNode child) {
        if (child == null || child == this || child.parent != null) {
            throw new IllegalArgumentException("Node must be non-null, detached, and not self");
        }
        child.parent = this;
        children.add(child);
        return this;
    }

    public void remove(UiNode child) {
        if (children.remove(child)) child.parent = null;
    }

    public Size measure(Constraints constraints) {
        return new Size(constraints.constrainWidth(0f), constraints.constrainHeight(0f));
    }

    public void layout(Rect bounds) { this.bounds = bounds; }

    public final void renderTree(UiCanvas canvas) {
        if (!visible) return;
        render(canvas);
        for (UiNode child : children) child.renderTree(canvas);
    }

    protected void render(UiCanvas canvas) {}
    public EventResult onPointer(PointerEvent event) { return EventResult.IGNORED; }
    public EventResult onKey(UiKeyEvent event) { return EventResult.IGNORED; }

    public UiNode hitTest(float x, float y) {
        if (!visible || !enabled || !bounds.contains(x, y)) return null;
        for (int i = children.size() - 1; i >= 0; i--) {
            UiNode match = children.get(i).hitTest(x, y);
            if (match != null) return match;
        }
        return this;
    }

    public UiNode getParent() { return parent; }
    public List<UiNode> getChildren() { return Collections.unmodifiableList(children); }
    public Rect getBounds() { return bounds; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isFocusable() { return focusable; }
    public void setFocusable(boolean focusable) { this.focusable = focusable; }
}
