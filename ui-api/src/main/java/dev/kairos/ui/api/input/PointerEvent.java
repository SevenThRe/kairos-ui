package dev.kairos.ui.api.input;

public final class PointerEvent {
    private final float x;
    private final float y;
    private final int button;
    private final float scrollY;
    private final PointerAction action;

    public PointerEvent(float x, float y, int button, float scrollY, PointerAction action) {
        this.x = x;
        this.y = y;
        this.button = button;
        this.scrollY = scrollY;
        this.action = action;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getButton() { return button; }
    public float getScrollY() { return scrollY; }
    public PointerAction getAction() { return action; }
}
