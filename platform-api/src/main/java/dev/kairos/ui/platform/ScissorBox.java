package dev.kairos.ui.platform;

import java.util.Objects;

/** Integer OpenGL scissor box in bottom-left framebuffer coordinates. */
public final class ScissorBox {
    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public ScissorBox(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
    }

    @Override public boolean equals(Object value) {
        if (this == value) return true;
        if (!(value instanceof ScissorBox)) return false;
        ScissorBox other = (ScissorBox) value;
        return x == other.x && y == other.y && width == other.width && height == other.height;
    }

    @Override public int hashCode() { return Objects.hash(x, y, width, height); }
    @Override public String toString() { return "ScissorBox{" + x + "," + y + "," + width + "," + height + "}"; }
}
