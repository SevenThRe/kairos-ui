package dev.kairos.ui.api.geometry;

public final class Size {
    public static final Size ZERO = new Size(0f, 0f);
    private final float width;
    private final float height;

    public Size(float width, float height) {
        if (width < 0f || height < 0f) throw new IllegalArgumentException("Size cannot be negative");
        this.width = width;
        this.height = height;
    }

    public float getWidth() { return width; }
    public float getHeight() { return height; }
}
