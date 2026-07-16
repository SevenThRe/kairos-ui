package dev.kairos.ui.esp;

public final class ScreenPoint {
    public final float x;
    public final float y;
    public final float depth;
    public final boolean visible;

    public ScreenPoint(float x, float y, float depth, boolean visible) {
        this.x = x;
        this.y = y;
        this.depth = depth;
        this.visible = visible;
    }

    public static ScreenPoint hidden() { return new ScreenPoint(0f, 0f, 1f, false); }
}
