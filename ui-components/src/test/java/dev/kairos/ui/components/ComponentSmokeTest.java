package dev.kairos.ui.components;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.theme.ThemeTokens;
import dev.kairos.ui.components.model.EnumSetting;
import dev.kairos.ui.components.model.ModuleCatalog;
import dev.kairos.ui.components.model.UiModule;
import dev.kairos.ui.components.model.UiSetting;
import dev.kairos.ui.components.scene.ModernWorkbench;
import dev.kairos.ui.components.scene.WorkbenchState;
import dev.kairos.ui.core.render.RecordingCanvas;
import dev.kairos.ui.example.DemoCatalog;

public final class ComponentSmokeTest {
    public static void main(String[] args) {
        ModuleCatalog catalog = DemoCatalog.create();
        UiModule killAura = catalog.getModules().get(0);
        require(visibleSettings(killAura) == 3, "default mode setting visibility");
        ((EnumSetting) killAura.getSettings().get(0)).setValue("Hypixel");
        require(visibleSettings(killAura) == 4, "mode-specific setting becomes visible");

        WorkbenchState state = new WorkbenchState();
        state.setSelectedCategoryId("combat");
        state.setSelectedModuleId("kill-aura");
        ModernWorkbench scene = new ModernWorkbench(catalog, state, ThemeTokens.kairosDark());
        scene.layout(new Rect(100f, 50f, 1000f, 600f));
        RecordingCanvas canvas = new RecordingCanvas();
        scene.renderTree(canvas);
        require(canvas.getCommands().size() > 20, "workbench emitted render commands");
        require(canvas.getClipDepth() == 0, "clip stack balanced");
        System.out.println("ComponentSmokeTest passed");
    }

    private static int visibleSettings(UiModule module) {
        int count = 0;
        for (UiSetting<?> setting : module.getSettings()) if (setting.isVisible()) count++;
        return count;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
