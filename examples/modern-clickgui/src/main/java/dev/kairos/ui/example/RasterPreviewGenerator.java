package dev.kairos.ui.example;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.theme.ThemeTokens;
import dev.kairos.ui.components.model.ModuleCatalog;
import dev.kairos.ui.components.scene.ModernWorkbench;
import dev.kairos.ui.components.scene.PanelDesktop;
import dev.kairos.ui.components.scene.WorkbenchState;
import dev.kairos.ui.preview.awt.AwtCanvas;
import dev.kairos.ui.components.hud.HudModel;
import dev.kairos.ui.components.hud.HudScene;
import dev.kairos.ui.components.hud.CombatHudProfile;
import dev.kairos.ui.components.hud.CombatHudScene;
import dev.kairos.ui.components.hud.EquipmentVisual;
import dev.kairos.ui.components.hud.NoopGameVisualRenderer;
import dev.kairos.ui.components.hud.TargetSnapshot;
import dev.kairos.ui.esp.EspOverlayScene;
import java.util.Arrays;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public final class RasterPreviewGenerator {
    public static void main(String[] args) throws IOException {
        System.setProperty("java.awt.headless", "true");
        File output = new File(args.length == 0 ? "out/previews" : args[0]);
        if (!output.exists() && !output.mkdirs()) throw new IOException("Cannot create " + output);
        ModuleCatalog catalog = DemoCatalog.create();
        ThemeTokens theme = ThemeTokens.kairosDark();

        WorkbenchState state = new WorkbenchState();
        state.setSelectedCategoryId("combat");
        state.setSelectedModuleId("kill-aura");
        ModernWorkbench workbench = new ModernWorkbench(catalog, state, theme);
        workbench.layout(new Rect(110f, 60f, 1060f, 600f));
        AwtCanvas workbenchCanvas = new AwtCanvas(1280, 720);
        workbenchCanvas.paintBackdrop();
        workbench.renderTree(workbenchCanvas);
        ImageIO.write(workbenchCanvas.getImage(), "png", new File(output, "workbench.png"));
        workbenchCanvas.dispose();

        PanelDesktop panels = new PanelDesktop(catalog, theme);
        panels.layout(new Rect(0f, 0f, 1600f, 900f));
        panels.getExpandedModules().add("kill-aura");
        AwtCanvas panelCanvas = new AwtCanvas(1600, 900);
        panelCanvas.paintBackdrop();
        panels.renderTree(panelCanvas);
        ImageIO.write(panelCanvas.getImage(), "png", new File(output, "panel-desktop.png"));
        panelCanvas.dispose();

        long previewNow = 100000L;
        HudScene hud = new HudScene(new HudModel(previewNow), theme).setNowMillis(previewNow);
        hud.layout(new Rect(0f, 0f, 1280f, 720f));
        AwtCanvas hudCanvas = new AwtCanvas(1280, 720);
        hudCanvas.paintBackdrop();
        hud.renderTree(hudCanvas);
        ImageIO.write(hudCanvas.getImage(), "png", new File(output, "hud.png"));
        hudCanvas.dispose();

        TargetSnapshot target = new TargetSnapshot("player:java-rival", "JavaRival", 12.9f, 20f,
            5.8f, 0f, 4.2f, false, 4,
            new EquipmentVisual("diamond_sword", "Diamond Sword", 86),
            Arrays.asList(new EquipmentVisual("diamond_helmet", "Helmet", 55),
                new EquipmentVisual("iron_chestplate", "Chestplate", 80),
                new EquipmentVisual("diamond_leggings", "Leggings", 75),
                new EquipmentVisual("iron_boots", "Boots", 38)));
        CombatHudScene combatHud = new CombatHudScene(target, CombatHudProfile.competitivePixel(),
            new NoopGameVisualRenderer());
        combatHud.layout(new Rect(0f, 0f, 1280f, 720f));
        AwtCanvas combatCanvas = new AwtCanvas(1280, 720);
        combatCanvas.paintBackdrop();
        combatHud.renderTree(combatCanvas);
        ImageIO.write(combatCanvas.getImage(), "png", new File(output, "combat-hud.png"));
        combatCanvas.dispose();

        EspOverlayScene esp = DemoEsp.create(theme);
        esp.layout(new Rect(0f, 0f, 1280f, 720f));
        AwtCanvas espCanvas = new AwtCanvas(1280, 720);
        espCanvas.paintBackdrop();
        esp.renderTree(espCanvas);
        ImageIO.write(espCanvas.getImage(), "png", new File(output, "esp.png"));
        ImageIO.write(espCanvas.getImage(), "png", new File(output, "competitive-esp.png"));
        espCanvas.dispose();
        System.out.println("Raster previews: " + output.getAbsolutePath());
    }
}
