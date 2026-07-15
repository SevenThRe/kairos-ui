package dev.kairos.ui.example;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.theme.ThemeTokens;
import dev.kairos.ui.components.model.ModuleCatalog;
import dev.kairos.ui.components.scene.ModernWorkbench;
import dev.kairos.ui.components.scene.PanelDesktop;
import dev.kairos.ui.components.scene.WorkbenchState;
import dev.kairos.ui.core.render.RecordingCanvas;

public final class WorkbenchExample {
    public static void main(String[] args) {
        ModuleCatalog catalog = DemoCatalog.create();
        ThemeTokens theme = ThemeTokens.kairosDark();
        WorkbenchState state = new WorkbenchState();
        state.setSelectedCategoryId("combat");
        state.setSelectedModuleId("kill-aura");

        ModernWorkbench workbench = new ModernWorkbench(catalog, state, theme);
        workbench.layout(new Rect(120f, 70f, 1040f, 620f));
        RecordingCanvas workbenchCanvas = new RecordingCanvas();
        workbench.renderTree(workbenchCanvas);

        PanelDesktop panels = new PanelDesktop(catalog, theme);
        panels.layout(new Rect(0f, 0f, 1280f, 720f));
        RecordingCanvas panelCanvas = new RecordingCanvas();
        panels.renderTree(panelCanvas);

        System.out.println("Kairos workbench commands: " + workbenchCanvas.getCommands().size());
        System.out.println("Kairos panel desktop commands: " + panelCanvas.getCommands().size());
    }
}
