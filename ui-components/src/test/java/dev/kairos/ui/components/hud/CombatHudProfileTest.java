package dev.kairos.ui.components.hud;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.core.render.RecordingCanvas;
import java.io.StringReader;
import java.util.Arrays;

public final class CombatHudProfileTest {
    public static void main(String[] args) throws Exception {
        CombatHudProfile expected = CombatHudProfile.competitivePixel();
        CombatHudProfile decoded = CombatHudProfileCodec.decode(new StringReader(CombatHudProfileCodec.encode(expected)));
        if (decoded.enemy != expected.enemy || !decoded.pixelSnap) throw new AssertionError("HUD profile round trip");
        TargetSnapshot target = new TargetSnapshot("player:test", "Test", 8f, 20f, 12f, 0f, 3f,
            false, 2, new EquipmentVisual("diamond_sword", "Sword", 80),
            Arrays.asList(new EquipmentVisual("helmet", "Helmet", 50)));
        CombatHudScene scene = new CombatHudScene(target, decoded, new SemanticGameVisualRenderer());
        scene.layout(new Rect(0f, 0f, 800f, 450f));
        RecordingCanvas canvas = new RecordingCanvas();
        scene.renderTree(canvas);
        if (canvas.getCommands().size() < 30) throw new AssertionError("Combat HUD detail missing");
        System.out.println("CombatHudProfileTest passed");
    }
}
