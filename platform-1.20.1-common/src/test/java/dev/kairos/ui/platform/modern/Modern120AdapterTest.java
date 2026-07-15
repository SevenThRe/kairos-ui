package dev.kairos.ui.platform.modern;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.input.KeyAction;
import dev.kairos.ui.api.input.PointerAction;
import dev.kairos.ui.api.input.PointerEvent;
import dev.kairos.ui.api.input.UiKeyEvent;
import dev.kairos.ui.platform.ScissorBox;

public final class Modern120AdapterTest {
    public static void main(String[] args) {
        Modern120InputAdapter input = new Modern120InputAdapter();
        PointerEvent click = input.pointer(201.5d, 88.25d, 1, 1, 0d);
        require(click.getAction() == PointerAction.DOWN && click.getButton() == 1, "modern press mapping");
        require(input.key(259, 14, 2, 3).getLogicalKey() == UiKeyEvent.KEY_BACKSPACE, "modern key mapping");
        require(input.key(259, 14, 2, 3).getAction() == KeyAction.REPEAT, "modern repeat mapping");
        require(input.character('k', 0).getAction() == KeyAction.TYPED, "modern character mapping");
        require(Modern120ScissorMapper.map(new Rect(10f, 20f, 100f, 50f), 1.5f, 900)
            .equals(new ScissorBox(15, 795, 150, 75)), "modern scissor conversion");
        System.out.println("Modern120AdapterTest passed");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
