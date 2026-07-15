package dev.kairos.ui.components.scene;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.render.UiCanvas;
import dev.kairos.ui.api.theme.ThemeTokens;
import dev.kairos.ui.components.model.ModuleCatalog;
import dev.kairos.ui.components.model.UiCategory;
import dev.kairos.ui.components.model.UiModule;
import dev.kairos.ui.components.model.UiSetting;
import dev.kairos.ui.core.node.UiNode;
import java.util.List;

public final class ModernWorkbench extends UiNode {
    private final ModuleCatalog catalog;
    private final WorkbenchState state;
    private final ThemeTokens theme;

    public ModernWorkbench(ModuleCatalog catalog, WorkbenchState state, ThemeTokens theme) {
        this.catalog = catalog;
        this.state = state;
        this.theme = theme;
    }

    @Override protected void render(UiCanvas canvas) {
        Rect window = getBounds();
        canvas.glass(window, theme.windowRadius, theme.glassBlurRadius, theme.window);
        canvas.pushClip(window);

        float headerHeight = 54f;
        float sidebarWidth = 180f;
        float moduleWidth = Math.max(260f, window.getWidth() * 0.33f);
        float contentY = window.getY() + headerHeight;
        float contentHeight = window.getHeight() - headerHeight;

        canvas.fillRect(new Rect(window.getX(), window.getY(), window.getWidth(), headerHeight), 0xD9161D24);
        canvas.text(theme.fontSemibold, "Kairos", window.getX() + 24f, window.getY() + 34f, 16f, theme.textPrimary);
        canvas.text(theme.fontRegular, "Search modules...", window.getX() + sidebarWidth + 28f,
            window.getY() + 33f, 12f, theme.textSecondary);

        Rect sidebar = new Rect(window.getX(), contentY, sidebarWidth, contentHeight);
        canvas.fillRect(sidebar, 0xB710161B);
        float categoryY = sidebar.getY() + 18f;
        for (UiCategory category : catalog.getCategories()) {
            boolean selected = category.getId().equals(state.getSelectedCategoryId());
            if (selected) canvas.roundedRect(new Rect(sidebar.getX() + 12f, categoryY, sidebarWidth - 24f, 38f),
                theme.componentRadius, 0xD94E36B8);
            canvas.text(theme.fontMedium, category.getDisplayName(), sidebar.getX() + 36f, categoryY + 24f,
                13f, selected ? theme.textPrimary : theme.textSecondary);
            categoryY += 44f;
        }

        Rect modulesRect = new Rect(sidebar.getRight(), contentY, moduleWidth, contentHeight);
        canvas.fillRect(modulesRect, 0xBD131A20);
        List<UiModule> modules = catalog.filter(state.getSelectedCategoryId(), state.getSearchQuery());
        canvas.text(theme.fontSemibold, selectedCategoryName(), modulesRect.getX() + 18f, modulesRect.getY() + 27f,
            14f, theme.textPrimary);
        float moduleY = modulesRect.getY() + 44f;
        for (UiModule module : modules) {
            boolean selected = module.getId().equals(state.getSelectedModuleId());
            int card = selected ? 0xEE252B36 : theme.surface;
            canvas.roundedRect(new Rect(modulesRect.getX() + 14f, moduleY, moduleWidth - 28f, 52f),
                theme.componentRadius, card);
            canvas.text(theme.fontMedium, module.getDisplayName(), modulesRect.getX() + 28f, moduleY + 21f,
                12f, theme.textPrimary);
            canvas.text(theme.fontRegular, module.getDescription(), modulesRect.getX() + 28f, moduleY + 39f,
                10f, theme.textSecondary);
            canvas.roundedRect(new Rect(modulesRect.getRight() - 52f, moduleY + 17f, 28f, 16f), 8f,
                module.isEnabled() ? theme.accent : 0xFF3B434B);
            moduleY += 60f;
        }

        Rect settings = new Rect(modulesRect.getRight(), contentY,
            Math.max(0f, window.getRight() - modulesRect.getRight()), contentHeight);
        canvas.fillRect(settings, 0xB8171E24);
        UiModule selected = selectedModule();
        if (selected != null) renderSettings(canvas, settings, selected);
        canvas.popClip();
    }

    private void renderSettings(UiCanvas canvas, Rect area, UiModule module) {
        canvas.text(theme.fontSemibold, module.getDisplayName(), area.getX() + 22f, area.getY() + 30f,
            15f, theme.textPrimary);
        float y = area.getY() + 62f;
        for (UiSetting<?> setting : module.getSettings()) {
            if (!setting.isVisible()) continue;
            canvas.text(theme.fontRegular, setting.getDisplayName(), area.getX() + 22f, y + 17f,
                11f, theme.textPrimary);
            canvas.text(theme.fontMedium, String.valueOf(setting.getValue()), area.getRight() - 92f, y + 17f,
                11f, theme.textSecondary);
            canvas.fillRect(new Rect(area.getX() + 22f, y + 31f, area.getWidth() - 44f, 1f), theme.border);
            y += 46f;
        }
    }

    private String selectedCategoryName() {
        for (UiCategory category : catalog.getCategories()) {
            if (category.getId().equals(state.getSelectedCategoryId())) return category.getDisplayName();
        }
        return "Modules";
    }

    private UiModule selectedModule() {
        for (UiModule module : catalog.getModules()) {
            if (module.getId().equals(state.getSelectedModuleId())) return module;
        }
        return null;
    }
}
