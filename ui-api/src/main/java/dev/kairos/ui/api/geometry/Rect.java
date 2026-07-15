package dev.kairos.ui.api.geometry;

import java.util.Objects;

public final class Rect {
    public static final Rect ZERO = new Rect(0f, 0f, 0f, 0f);

    private final float x;
    private final float y;
    private final float width;
    private final float height;

    public Rect(float x, float y, float width, float height) {
        if (width < 0f || height < 0f) {
            throw new IllegalArgumentException("Rect dimensions cannot be negative");
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public float getRight() { return x + width; }
    public float getBottom() { return y + height; }

    public boolean contains(float px, float py) {
        return px >= x && py >= y && px < getRight() && py < getBottom();
    }

    public Rect inset(float amount) {
        float nextWidth = Math.max(0f, width - amount * 2f);
        float nextHeight = Math.max(0f, height - amount * 2f);
        return new Rect(x + amount, y + amount, nextWidth, nextHeight);
    }

    @Override public boolean equals(Object value) {
        if (this == value) return true;
        if (!(value instanceof Rect)) return false;
        Rect other = (Rect) value;
        return Float.compare(x, other.x) == 0 && Float.compare(y, other.y) == 0
            && Float.compare(width, other.width) == 0 && Float.compare(height, other.height) == 0;
    }

    @Override public int hashCode() { return Objects.hash(x, y, width, height); }
    @Override public String toString() { return "Rect{" + x + "," + y + "," + width + "," + height + "}"; }
}
