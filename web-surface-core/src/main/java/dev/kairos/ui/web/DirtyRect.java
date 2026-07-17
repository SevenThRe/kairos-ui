package dev.kairos.ui.web;

import java.util.Objects;

/** Immutable pixel-space damage rectangle. */
public final class DirtyRect {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public DirtyRect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int x() { return x; }
    public int y() { return y; }
    public int width() { return width; }
    public int height() { return height; }
    public int right() { return x + width; }
    public int bottom() { return y + height; }
    public long area() { return (long) width * (long) height; }
    public boolean isEmpty() { return width <= 0 || height <= 0; }

    public DirtyRect clamp(int surfaceWidth, int surfaceHeight) {
        int left = Math.max(0, Math.min(surfaceWidth, x));
        int top = Math.max(0, Math.min(surfaceHeight, y));
        int right = Math.max(left, Math.min(surfaceWidth, right()));
        int bottom = Math.max(top, Math.min(surfaceHeight, bottom()));
        return new DirtyRect(left, top, right - left, bottom - top);
    }

    public boolean touches(DirtyRect other, int gap) {
        return x <= other.right() + gap && right() + gap >= other.x
            && y <= other.bottom() + gap && bottom() + gap >= other.y;
    }

    public DirtyRect union(DirtyRect other) {
        int left = Math.min(x, other.x);
        int top = Math.min(y, other.y);
        int right = Math.max(right(), other.right());
        int bottom = Math.max(bottom(), other.bottom());
        return new DirtyRect(left, top, right - left, bottom - top);
    }

    @Override
    public boolean equals(Object value) {
        if (this == value) return true;
        if (!(value instanceof DirtyRect)) return false;
        DirtyRect other = (DirtyRect) value;
        return x == other.x && y == other.y && width == other.width && height == other.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height);
    }

    @Override
    public String toString() {
        return "DirtyRect{" + x + ',' + y + ' ' + width + 'x' + height + '}';
    }
}
