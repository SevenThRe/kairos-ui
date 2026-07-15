package dev.kairos.ui.components.scene;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.render.UiCanvas;
import dev.kairos.ui.api.theme.ThemeTokens;
import dev.kairos.ui.components.model.ModuleCatalog;
import dev.kairos.ui.components.model.UiCategory;
import dev.kairos.ui.components.model.UiModule;
import dev.kairos.ui.core.node.UiNode;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PanelDesktop extends UiNode {
    private final ModuleCatalog catalog;
    private final ThemeTokens theme;
    private final Map<String, PanelState> states = new LinkedHashMap<String, PanelState>();

    public PanelDesktop(ModuleCatalog catalog, ThemeTokens theme) {
        this.catalog = catalog;
        this.theme = theme;
        float x = 24f;
        for (UiCategory category : catalog.getCategories()) {
            states.put(category.getId(), new PanelState(x, 32f, 210f));
            x += 228f;
        }
    }

    @Override protected void render(UiCanvas canvas) {
        canvas.fillRect(getBounds(), theme.backdrop);
        for (UiCategory category : catalog.getCategories()) renderPanel(canvas, category, states.get(category.getId()));
    }

    private void renderPanel(UiCanvas canvas, UiCategory category, PanelState state) {
        int moduleCount = catalog.filter(category.getId(), "").size();
        float height = state.collapsed ? 38f : 48f + moduleCount * 42f;
        Rect panel = new Rect(state.x, state.y, state.width, height);
        canvas.glass(panel, theme.windowRadius, theme.glassBlurRadius, theme.window);
        canvas.text(theme.fontSemibold, category.getDisplayName(), panel.getX() + 14f, panel.getY() + 25f,
            13f, theme.textPrimary);
        if (state.collapsed) return;
        float y = panel.getY() + 42f;
        for (UiModule module : catalog.filter(category.getId(), "")) {
            canvas.roundedRect(new Rect(panel.getX() + 8f, y, panel.getWidth() - 16f, 34f),
                theme.componentRadius, theme.surface);
            canvas.text(theme.fontRegular, module.getDisplayName(), panel.getX() + 18f, y + 22f,
                11f, theme.textPrimary);
            y += 42f;
        }
    }

    public Map<String, PanelState> getPanelStates() { return states; }
}
