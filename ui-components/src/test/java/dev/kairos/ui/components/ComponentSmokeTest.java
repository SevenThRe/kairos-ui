package dev.kairos.ui.components;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.theme.ThemeTokens;
import dev.kairos.ui.api.input.PointerAction;
import dev.kairos.ui.api.input.PointerEvent;
import dev.kairos.ui.components.model.EnumSetting;
import dev.kairos.ui.components.model.ModuleCatalog;
import dev.kairos.ui.components.model.UiModule;
import dev.kairos.ui.components.model.UiSetting;
import dev.kairos.ui.components.scene.ModernWorkbench;
import dev.kairos.ui.components.scene.PanelDesktop;
import dev.kairos.ui.components.scene.WorkbenchState;
import dev.kairos.ui.core.render.RecordingCanvas;
import dev.kairos.ui.example.DemoCatalog;

public final class ComponentSmokeTest {
    public static void main(String[] args) {
        ModuleCatalog catalog = DemoCatalog.create();
        UiModule killAura = catalog.getModules().get(0);
        int defaultVisible = visibleSettings(killAura);
        ((EnumSetting) killAura.getSettings().get(0)).setValue("Hypixel");
        require(visibleSettings(killAura) == defaultVisible + 1, "mode-specific setting becomes visible");

        WorkbenchState state = new WorkbenchState();
        state.setSelectedCategoryId("combat");
        state.setSelectedModuleId("kill-aura");
        ModernWorkbench scene = new ModernWorkbench(catalog, state, ThemeTokens.kairosDark());
        scene.layout(new Rect(100f, 50f, 1000f, 600f));
        RecordingCanvas canvas = new RecordingCanvas();
        scene.renderTree(canvas);
        require(canvas.getCommands().size() > 20, "workbench emitted render commands");
        require(canvas.getClipDepth() == 0, "clip stack balanced");
        boolean wasEnabled = killAura.isEnabled();
        require(scene.onPointer(new PointerEvent(300f, 160f, 0, 0f, PointerAction.DOWN))
            == dev.kairos.ui.api.input.EventResult.HANDLED, "module row handles left click");
        require(killAura.isEnabled() != wasEnabled, "left click toggles module without a pill control");
        UiModule velocity = catalog.getModules().get(1);
        boolean velocityEnabled = velocity.isEnabled();
        require(scene.onPointer(new PointerEvent(300f, 220f, 1, 0f, PointerAction.DOWN))
            == dev.kairos.ui.api.input.EventResult.HANDLED, "module row handles right click");
        require("velocity".equals(state.getSelectedModuleId()), "right click selects module settings");
        require(velocity.isEnabled() == velocityEnabled, "right click does not toggle module");

        PanelDesktop panels = new PanelDesktop(catalog, ThemeTokens.kairosDark());
        panels.layout(new Rect(0f, 0f, 1280f, 720f));
        float initialX = panels.getPanelStates().get("combat").x;
        require(panels.onPointer(new PointerEvent(initialX + 10f, 40f, 0, 0f, PointerAction.DOWN))
            == dev.kairos.ui.api.input.EventResult.CAPTURE_POINTER, "panel captures drag");
        panels.onPointer(new PointerEvent(420f, 180f, 0, 0f, PointerAction.MOVE));
        panels.onPointer(new PointerEvent(420f, 180f, 0, 0f, PointerAction.UP));
        require(panels.getPanelStates().get("combat").x != initialX, "panel position changes");

        panels.getExpandedModules().add("kill-aura");
        float combatX = panels.getPanelStates().get("combat").x;
        float combatY = panels.getPanelStates().get("combat").y;
        require(panels.onPointer(new PointerEvent(combatX + 100f, combatY + 200f, -1, -1f,
            PointerAction.SCROLL)) == dev.kairos.ui.api.input.EventResult.HANDLED, "panel consumes scroll");
        require(panels.getPanelStates().get("combat").scrollOffset > 0f, "panel scroll offset changes");
        RecordingCanvas panelCanvas = new RecordingCanvas();
        panels.renderTree(panelCanvas);
        require(panelCanvas.getClipDepth() == 0, "floating panel clip stack balanced");
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
