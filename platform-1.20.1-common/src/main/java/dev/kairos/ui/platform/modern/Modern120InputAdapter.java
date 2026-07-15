package dev.kairos.ui.platform.modern;

import dev.kairos.ui.api.input.KeyAction;
import dev.kairos.ui.api.input.PointerAction;
import dev.kairos.ui.api.input.PointerEvent;
import dev.kairos.ui.api.input.UiKeyEvent;

/** Pure GLFW-to-Kairos mapping for modern Screen implementations. */
public final class Modern120InputAdapter {
    public PointerEvent pointer(double guiX, double guiY, int button, int glfwAction, double scrollY) {
        PointerAction action;
        if (scrollY != 0d) action = PointerAction.SCROLL;
        else if (button < 0) action = PointerAction.MOVE;
        else action = glfwAction == 1 ? PointerAction.DOWN : PointerAction.UP;
        return new PointerEvent((float) guiX, (float) guiY, button, (float) scrollY, action);
    }

    public UiKeyEvent key(int glfwKey, int scanCode, int glfwAction, int modifiers) {
        KeyAction action = glfwAction == 1 ? KeyAction.DOWN : glfwAction == 2 ? KeyAction.REPEAT : KeyAction.UP;
        return new UiKeyEvent(logicalKey(glfwKey), scanCode, modifiers, '\0', action);
    }

    public UiKeyEvent character(int codePoint, int modifiers) {
        char typed = codePoint >= Character.MIN_VALUE && codePoint <= Character.MAX_VALUE ? (char) codePoint : '\0';
        return new UiKeyEvent(Character.toUpperCase(typed), 0, modifiers, typed, KeyAction.TYPED);
    }

    private static int logicalKey(int glfwKey) {
        if (glfwKey == 256) return UiKeyEvent.KEY_ESCAPE;
        if (glfwKey == 259) return UiKeyEvent.KEY_BACKSPACE;
        if (glfwKey == 257 || glfwKey == 335) return UiKeyEvent.KEY_ENTER;
        return glfwKey;
    }
}
