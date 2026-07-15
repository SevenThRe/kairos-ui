package dev.kairos.ui.core.layout;

import dev.kairos.ui.api.geometry.Constraints;
import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.geometry.Size;
import dev.kairos.ui.core.node.UiNode;

public final class ColumnNode extends UiNode {
    private final float gap;
    private final float padding;

    public ColumnNode(float gap, float padding) {
        this.gap = gap;
        this.padding = padding;
    }

    @Override public Size measure(Constraints constraints) {
        float width = 0f;
        float height = padding * 2f;
        int visible = 0;
        for (UiNode child : getChildren()) {
            if (!child.isVisible()) continue;
            Size childSize = child.measure(new Constraints(0f, constraints.getMaxWidth(), 0f, constraints.getMaxHeight()));
            width = Math.max(width, childSize.getWidth());
            height += childSize.getHeight();
            visible++;
        }
        if (visible > 1) height += gap * (visible - 1);
        return new Size(constraints.constrainWidth(width + padding * 2f), constraints.constrainHeight(height));
    }

    @Override public void layout(Rect bounds) {
        super.layout(bounds);
        float y = bounds.getY() + padding;
        float width = Math.max(0f, bounds.getWidth() - padding * 2f);
        for (UiNode child : getChildren()) {
            if (!child.isVisible()) continue;
            Size size = child.measure(new Constraints(0f, width, 0f, bounds.getHeight()));
            child.layout(new Rect(bounds.getX() + padding, y, width, size.getHeight()));
            y += size.getHeight() + gap;
        }
    }
}
