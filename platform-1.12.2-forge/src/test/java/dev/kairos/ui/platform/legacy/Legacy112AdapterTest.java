package dev.kairos.ui.platform.legacy;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.input.PointerAction;
import dev.kairos.ui.api.input.PointerEvent;
import dev.kairos.ui.api.input.UiKeyEvent;
import dev.kairos.ui.platform.ScissorBox;

public final class Legacy112AdapterTest {
    public static void main(String[] args) {
        Legacy112InputAdapter input = new Legacy112InputAdapter(1080, 2f);
        PointerEvent click = input.pointer(400, 679, 0, true, 0);
        require(close(click.getX(), 200f) && close(click.getY(), 200f), "legacy pointer scale and Y inversion");
        require(click.getAction() == PointerAction.DOWN, "legacy press mapping");
        PointerEvent wheel = input.pointer(400, 679, -1, false, -120);
        require(wheel.getAction() == PointerAction.SCROLL && wheel.getScrollY() == -1f, "legacy wheel normalization");
        require(input.key(1, '\0', true).getLogicalKey() == UiKeyEvent.KEY_ESCAPE, "legacy escape mapping");
        require(Legacy112ScissorMapper.map(new Rect(10f, 20f, 100f, 50f), 2f, 1080)
            .equals(new ScissorBox(20, 940, 200, 100)), "legacy scissor conversion");
        System.out.println("Legacy112AdapterTest passed");
    }

    private static boolean close(float a, float b) { return Math.abs(a - b) < 0.001f; }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
