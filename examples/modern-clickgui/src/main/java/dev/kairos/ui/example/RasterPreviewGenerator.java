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
        panels.layout(new Rect(0f, 0f, 1280f, 720f));
        panels.getExpandedModules().add("kill-aura");
        AwtCanvas panelCanvas = new AwtCanvas(1280, 720);
        panelCanvas.paintBackdrop();
        panels.renderTree(panelCanvas);
        ImageIO.write(panelCanvas.getImage(), "png", new File(output, "panel-desktop.png"));
        panelCanvas.dispose();

        HudScene hud = new HudScene(new HudModel(), theme);
        hud.layout(new Rect(0f, 0f, 1280f, 720f));
        AwtCanvas hudCanvas = new AwtCanvas(1280, 720);
        hudCanvas.paintBackdrop();
        hud.renderTree(hudCanvas);
        ImageIO.write(hudCanvas.getImage(), "png", new File(output, "hud.png"));
        hudCanvas.dispose();
        System.out.println("Raster previews: " + output.getAbsolutePath());
    }
}
