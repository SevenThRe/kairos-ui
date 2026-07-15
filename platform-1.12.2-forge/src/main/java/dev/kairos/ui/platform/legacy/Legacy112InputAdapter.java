package dev.kairos.ui.platform.legacy;

import dev.kairos.ui.api.input.KeyAction;
import dev.kairos.ui.api.input.PointerAction;
import dev.kairos.ui.api.input.PointerEvent;
import dev.kairos.ui.api.input.UiKeyEvent;

/** Pure LWJGL2-to-Kairos mapping, isolated from Minecraft and safe to unit test. */
public final class Legacy112InputAdapter {
    private final int displayHeight;
    private final float guiScale;

    public Legacy112InputAdapter(int displayHeight, float guiScale) {
        if (displayHeight <= 0 || guiScale <= 0f) throw new IllegalArgumentException("Invalid legacy viewport");
        this.displayHeight = displayHeight;
        this.guiScale = guiScale;
    }

    public PointerEvent pointer(int rawX, int rawY, int button, boolean pressed, int wheelDelta) {
        float x = rawX / guiScale;
        float y = (displayHeight - rawY - 1f) / guiScale;
        PointerAction action;
        if (wheelDelta != 0) action = PointerAction.SCROLL;
        else if (button < 0) action = PointerAction.MOVE;
        else action = pressed ? PointerAction.DOWN : PointerAction.UP;
        float scroll = wheelDelta == 0 ? 0f : (wheelDelta > 0 ? 1f : -1f);
        return new PointerEvent(x, y, button, scroll, action);
    }

    public UiKeyEvent key(int lwjglKey, char typedCharacter, boolean pressed) {
        return new UiKeyEvent(logicalKey(lwjglKey, typedCharacter), lwjglKey, 0, typedCharacter,
            pressed ? KeyAction.DOWN : KeyAction.UP);
    }

    private static int logicalKey(int lwjglKey, char typedCharacter) {
        if (lwjglKey == 1) return UiKeyEvent.KEY_ESCAPE;
        if (lwjglKey == 14) return UiKeyEvent.KEY_BACKSPACE;
        if (lwjglKey == 28 || lwjglKey == 156) return UiKeyEvent.KEY_ENTER;
        return typedCharacter == 0 ? lwjglKey : Character.toUpperCase(typedCharacter);
    }
}
