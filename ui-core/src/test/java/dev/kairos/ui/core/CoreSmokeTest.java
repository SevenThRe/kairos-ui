package dev.kairos.ui.core;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.input.EventResult;
import dev.kairos.ui.api.input.PointerAction;
import dev.kairos.ui.api.input.PointerEvent;
import dev.kairos.ui.core.animation.AnimatedFloat;
import dev.kairos.ui.core.input.InputDispatcher;
import dev.kairos.ui.core.node.UiNode;

public final class CoreSmokeTest {
    public static void main(String[] args) {
        animationIsTimeBased();
        pointerCaptureSurvivesLeavingBounds();
        System.out.println("CoreSmokeTest passed");
    }

    private static void animationIsTimeBased() {
        AnimatedFloat value = new AnimatedFloat(0f);
        value.animateTo(1f, 200L, 1_000_000_000L);
        float middle = value.update(1_100_000_000L);
        require(middle > 0f && middle < 1f, "animation midpoint");
        require(value.update(1_200_000_000L) == 1f, "animation completion");
    }

    private static void pointerCaptureSurvivesLeavingBounds() {
        UiNode root = new UiNode();
        root.layout(new Rect(0f, 0f, 500f, 500f));
        CapturingNode child = new CapturingNode();
        child.layout(new Rect(20f, 20f, 100f, 100f));
        root.add(child);
        InputDispatcher input = new InputDispatcher(root);
        input.dispatch(new PointerEvent(30f, 30f, 0, 0f, PointerAction.DOWN));
        require(input.getCapturedPointer() == child, "pointer captured");
        input.dispatch(new PointerEvent(400f, 400f, 0, 0f, PointerAction.MOVE));
        require(child.events == 2, "captured move delivered outside bounds");
    }

    private static final class CapturingNode extends UiNode {
        int events;
        @Override public EventResult onPointer(PointerEvent event) {
            events++;
            return event.getAction() == PointerAction.DOWN ? EventResult.CAPTURE_POINTER : EventResult.HANDLED;
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
