package dev.kairos.ui.api.geometry;

public final class Constraints {
    private final float minWidth;
    private final float maxWidth;
    private final float minHeight;
    private final float maxHeight;

    public Constraints(float minWidth, float maxWidth, float minHeight, float maxHeight) {
        if (minWidth < 0f || minHeight < 0f || maxWidth < minWidth || maxHeight < minHeight) {
            throw new IllegalArgumentException("Invalid layout constraints");
        }
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
    }

    public static Constraints tight(float width, float height) {
        return new Constraints(width, width, height, height);
    }

    public float constrainWidth(float value) { return Math.max(minWidth, Math.min(maxWidth, value)); }
    public float constrainHeight(float value) { return Math.max(minHeight, Math.min(maxHeight, value)); }
    public float getMaxWidth() { return maxWidth; }
    public float getMaxHeight() { return maxHeight; }
}
