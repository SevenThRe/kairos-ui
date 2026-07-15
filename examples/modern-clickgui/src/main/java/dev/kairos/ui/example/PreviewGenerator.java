package dev.kairos.ui.example;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.theme.ThemeTokens;
import dev.kairos.ui.components.model.ModuleCatalog;
import dev.kairos.ui.components.scene.ModernWorkbench;
import dev.kairos.ui.components.scene.PanelDesktop;
import dev.kairos.ui.components.scene.WorkbenchState;
import dev.kairos.ui.preview.svg.SvgCanvas;
import dev.kairos.ui.components.hud.HudModel;
import dev.kairos.ui.components.hud.HudScene;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class PreviewGenerator {
    public static void main(String[] args) throws IOException {
        File output = new File(args.length == 0 ? "out/previews" : args[0]);
        if (!output.exists() && !output.mkdirs()) throw new IOException("Cannot create " + output);
        ModuleCatalog catalog = DemoCatalog.create();
        ThemeTokens theme = ThemeTokens.kairosDark();

        WorkbenchState state = new WorkbenchState();
        state.setSelectedCategoryId("combat");
        state.setSelectedModuleId("kill-aura");
        ModernWorkbench workbench = new ModernWorkbench(catalog, state, theme);
        workbench.layout(new Rect(110f, 60f, 1060f, 600f));
        SvgCanvas workbenchSvg = new SvgCanvas(1280, 720);
        workbenchSvg.fillRect(new Rect(0f, 0f, 1280f, 720f), 0xFF0A1117);
        workbench.renderTree(workbenchSvg);
        write(new File(output, "workbench.svg"), workbenchSvg.toSvg());

        PanelDesktop panels = new PanelDesktop(catalog, theme);
        panels.layout(new Rect(0f, 0f, 1280f, 720f));
        panels.getExpandedModules().add("kill-aura");
        SvgCanvas panelSvg = new SvgCanvas(1280, 720);
        panels.renderTree(panelSvg);
        write(new File(output, "panel-desktop.svg"), panelSvg.toSvg());

        HudScene hud = new HudScene(new HudModel(), theme);
        hud.layout(new Rect(0f, 0f, 1280f, 720f));
        SvgCanvas hudSvg = new SvgCanvas(1280, 720);
        hudSvg.fillRect(new Rect(0f, 0f, 1280f, 720f), 0xFF0A1117);
        hud.renderTree(hudSvg);
        write(new File(output, "hud.svg"), hudSvg.toSvg());
        System.out.println("SVG previews: " + output.getAbsolutePath());
    }

    private static void write(File file, String content) throws IOException {
        FileOutputStream stream = new FileOutputStream(file);
        try { stream.write(content.getBytes(StandardCharsets.UTF_8)); }
        finally { stream.close(); }
    }
}
