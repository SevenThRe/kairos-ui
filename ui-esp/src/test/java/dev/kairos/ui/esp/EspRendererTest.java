package dev.kairos.ui.esp;

import dev.kairos.ui.api.theme.ThemeTokens;
import dev.kairos.ui.core.render.RecordingCanvas;
import java.util.Arrays;

public final class EspRendererTest {
    public static void main(String[] args) {
        MatrixProjector identity = new MatrixProjector(new float[] {
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        }, 0f, 0f, 1280f, 720f);
        ScreenPoint center = identity.project(0d, 0d, 0d);
        require(center.visible && center.x == 640f && center.y == 360f, "matrix projection maps origin to center");

        EspEntity player = new EspEntity("p1", "Steve",
            new WorldBounds(-0.08, -0.25, 0, 0.08, 0.25, 0), 17f, 20f, 4.2f, false, false);
        RecordingCanvas canvas = new RecordingCanvas();
        new EspRenderer().render(canvas, Arrays.asList(player), identity,
            EspStyle.kairosModern(ThemeTokens.kairosDark()), ThemeTokens.kairosDark());
        require(canvas.getCommands().size() >= 12, "ESP emits box, health and label commands");
        require(canvas.getClipDepth() == 0, "ESP keeps clip stack balanced");
        System.out.println("EspRendererTest passed");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
