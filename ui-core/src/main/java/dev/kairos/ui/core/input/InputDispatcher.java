package dev.kairos.ui.core.input;

import dev.kairos.ui.api.input.EventResult;
import dev.kairos.ui.api.input.PointerAction;
import dev.kairos.ui.api.input.PointerEvent;
import dev.kairos.ui.api.input.UiKeyEvent;
import dev.kairos.ui.core.node.UiNode;

public final class InputDispatcher {
    private final UiNode root;
    private UiNode capturedPointer;
    private UiNode focused;

    public InputDispatcher(UiNode root) {
        if (root == null) throw new IllegalArgumentException("root");
        this.root = root;
    }

    public EventResult dispatch(PointerEvent event) {
        UiNode target = capturedPointer != null ? capturedPointer : root.hitTest(event.getX(), event.getY());
        if (target == null) return EventResult.IGNORED;

        UiNode cursor = target;
        while (cursor != null) {
            EventResult result = cursor.onPointer(event);
            if (result == EventResult.CAPTURE_POINTER) {
                capturedPointer = cursor;
                if (cursor.isFocusable()) focused = cursor;
                return EventResult.HANDLED;
            }
            if (result == EventResult.HANDLED) {
                if (event.getAction() == PointerAction.DOWN && cursor.isFocusable()) focused = cursor;
                if (event.getAction() == PointerAction.UP && capturedPointer == cursor) capturedPointer = null;
                return result;
            }
            cursor = cursor.getParent();
        }

        if (event.getAction() == PointerAction.UP) capturedPointer = null;
        return EventResult.IGNORED;
    }

    public void releasePointer() { capturedPointer = null; }
    public EventResult dispatch(UiKeyEvent event) {
        UiNode cursor = focused;
        while (cursor != null) {
            EventResult result = cursor.onKey(event);
            if (result != EventResult.IGNORED) return EventResult.HANDLED;
            cursor = cursor.getParent();
        }
        return root.onKey(event);
    }
    public void focus(UiNode node) {
        if (node != null && !node.isFocusable()) throw new IllegalArgumentException("Node is not focusable");
        focused = node;
    }
    public UiNode getCapturedPointer() { return capturedPointer; }
    public UiNode getFocused() { return focused; }
}
