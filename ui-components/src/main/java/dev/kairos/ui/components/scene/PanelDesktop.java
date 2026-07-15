package dev.kairos.ui.components.scene;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.input.EventResult;
import dev.kairos.ui.api.input.PointerAction;
import dev.kairos.ui.api.input.PointerEvent;
import dev.kairos.ui.api.render.UiCanvas;
import dev.kairos.ui.api.theme.ThemeTokens;
import dev.kairos.ui.components.model.ModuleCatalog;
import dev.kairos.ui.components.model.UiCategory;
import dev.kairos.ui.components.model.UiModule;
import dev.kairos.ui.core.node.UiNode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PanelDesktop extends UiNode {
    private static final float HEADER_HEIGHT = 38f;
    private static final float MAX_PANEL_HEIGHT = 580f;
    private static final float SCROLL_STEP = 30f;
    private final ModuleCatalog catalog;
    private final ThemeTokens theme;
    private final Map<String, PanelState> states = new LinkedHashMap<String, PanelState>();
    private final Set<String> expandedModules = new LinkedHashSet<String>();
    private final List<String> zOrder = new ArrayList<String>();
    private PanelState dragging;
    private float dragOffsetX;
    private float dragOffsetY;
    private boolean initiallyArranged;

    public PanelDesktop(ModuleCatalog catalog, ThemeTokens theme) {
        this.catalog = catalog;
        this.theme = theme;
        float x = 24f;
        for (UiCategory category : catalog.getCategories()) {
            states.put(category.getId(), new PanelState(x, 32f, 210f));
            zOrder.add(category.getId());
            x += 228f;
        }
    }

    @Override public void layout(Rect bounds) {
        super.layout(bounds);
        if (initiallyArranged) return;
        float gap = 18f;
        float x = bounds.getX() + 24f;
        float y = bounds.getY() + 32f;
        for (UiCategory category : catalog.getCategories()) {
            PanelState state = states.get(category.getId());
            if (x + state.width > bounds.getRight() - 24f) {
                x = bounds.getX() + 24f;
                y += 190f;
            }
            state.x = x;
            state.y = y;
            x += state.width + gap;
        }
        initiallyArranged = true;
    }

    @Override protected void render(UiCanvas canvas) {
        canvas.fillRect(getBounds(), theme.backdrop);
        for (String categoryId : zOrder) {
            UiCategory category = categoryById(categoryId);
            renderPanel(canvas, category, states.get(categoryId));
        }
    }

    private void renderPanel(UiCanvas canvas, UiCategory category, PanelState state) {
        float desiredHeight = panelHeight(category);
        float height = state.collapsed ? HEADER_HEIGHT : visiblePanelHeight(state, desiredHeight);
        state.scrollOffset = clamp(state.scrollOffset, 0f, Math.max(0f, desiredHeight - height));
        Rect panel = new Rect(state.x, state.y, state.width, height);
        canvas.glass(panel, theme.windowRadius, theme.glassBlurRadius, theme.window);
        canvas.text(theme.fontSemibold, category.getDisplayName(), panel.getX() + 14f, panel.getY() + 25f,
            13f, theme.textPrimary);
        if (state.collapsed) return;
        Rect contentClip = new Rect(panel.getX(), panel.getY() + HEADER_HEIGHT,
            panel.getWidth(), Math.max(0f, panel.getHeight() - HEADER_HEIGHT));
        canvas.pushClip(contentClip);
        float y = panel.getY() + 42f - state.scrollOffset;
        for (UiModule module : catalog.filter(category.getId(), "")) {
            canvas.roundedRect(new Rect(panel.getX() + 8f, y, panel.getWidth() - 16f, 34f),
                theme.componentRadius, theme.surface);
            canvas.text(theme.fontRegular, module.getDisplayName(), panel.getX() + 18f, y + 22f,
                11f, theme.textPrimary);
            canvas.roundedRect(new Rect(panel.getRight() - 31f, y + 11f, 18f, 12f), 6f,
                module.isEnabled() ? theme.accent : 0xFF394149);
            y += 38f;
            if (expandedModules.contains(module.getId())) {
                for (dev.kairos.ui.components.model.UiSetting<?> setting : module.getSettings()) {
                    if (!setting.isVisible()) continue;
                    canvas.text(theme.fontRegular, setting.getDisplayName(), panel.getX() + 20f, y + 17f,
                        9f, theme.textSecondary);
                    canvas.text(theme.fontMedium, compact(setting.getValue()), panel.getRight() - 72f, y + 17f,
                        9f, theme.textPrimary);
                    y += 26f;
                }
                y += 4f;
            }
            y += 4f;
        }
        canvas.popClip();
        float overflow = desiredHeight - height;
        if (overflow > 0f) {
            float trackHeight = height - HEADER_HEIGHT - 12f;
            float thumbHeight = Math.max(28f, trackHeight * (height / desiredHeight));
            float thumbTravel = trackHeight - thumbHeight;
            float thumbY = panel.getY() + HEADER_HEIGHT + 6f
                + (overflow == 0f ? 0f : thumbTravel * (state.scrollOffset / overflow));
            canvas.roundedRect(new Rect(panel.getRight() - 5f, thumbY, 2f, thumbHeight), 1f, 0x887A63E8);
        }
    }

    public Map<String, PanelState> getPanelStates() { return states; }

    @Override public EventResult onPointer(PointerEvent event) {
        if (dragging != null) {
            if (event.getAction() == PointerAction.MOVE) {
                Rect desktop = getBounds();
                dragging.x = clamp(event.getX() - dragOffsetX, desktop.getX(), desktop.getRight() - dragging.width);
                dragging.y = clamp(event.getY() - dragOffsetY, desktop.getY(), desktop.getBottom() - HEADER_HEIGHT);
                return EventResult.HANDLED;
            }
            if (event.getAction() == PointerAction.UP) {
                dragging = null;
                return EventResult.HANDLED;
            }
        }
        if (event.getAction() == PointerAction.SCROLL) {
            UiCategory scrolled = panelAt(event.getX(), event.getY());
            if (scrolled == null) return EventResult.IGNORED;
            PanelState state = states.get(scrolled.getId());
            if (state.collapsed) return EventResult.IGNORED;
            float overflow = Math.max(0f, panelHeight(scrolled) - visiblePanelHeight(state, panelHeight(scrolled)));
            if (overflow == 0f) return EventResult.IGNORED;
            state.scrollOffset = clamp(state.scrollOffset - event.getScrollY() * SCROLL_STEP, 0f, overflow);
            return EventResult.HANDLED;
        }
        if (event.getAction() != PointerAction.DOWN) return EventResult.IGNORED;
        UiCategory hit = panelHeaderAt(event.getX(), event.getY());
        if (hit != null) {
            bringToFront(hit.getId());
            PanelState state = states.get(hit.getId());
            if (event.getButton() == 1) {
                state.collapsed = !state.collapsed;
                return EventResult.HANDLED;
            }
            if (event.getButton() == 0) {
                dragging = state;
                dragOffsetX = event.getX() - state.x;
                dragOffsetY = event.getY() - state.y;
                return EventResult.CAPTURE_POINTER;
            }
        }
        ModuleHit moduleHit = moduleAt(event.getX(), event.getY());
        if (moduleHit != null) bringToFront(moduleHit.categoryId);
        if (moduleHit != null && event.getButton() == 0) {
            moduleHit.module.setEnabled(!moduleHit.module.isEnabled());
            return EventResult.HANDLED;
        }
        if (moduleHit != null && event.getButton() == 1) {
            if (!expandedModules.add(moduleHit.module.getId())) expandedModules.remove(moduleHit.module.getId());
            return EventResult.HANDLED;
        }
        return EventResult.IGNORED;
    }

    private UiCategory panelHeaderAt(float x, float y) {
        UiCategory result = null;
        for (String categoryId : zOrder) {
            UiCategory category = categoryById(categoryId);
            PanelState state = states.get(categoryId);
            if (new Rect(state.x, state.y, state.width, HEADER_HEIGHT).contains(x, y)) result = category;
        }
        return result;
    }

    private UiCategory panelAt(float x, float y) {
        UiCategory result = null;
        for (String categoryId : zOrder) {
            UiCategory category = categoryById(categoryId);
            PanelState state = states.get(categoryId);
            float height = state.collapsed ? HEADER_HEIGHT : visiblePanelHeight(state, panelHeight(category));
            if (new Rect(state.x, state.y, state.width, height).contains(x, y)) result = category;
        }
        return result;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float panelHeight(UiCategory category) {
        float height = 46f;
        for (UiModule module : catalog.filter(category.getId(), "")) {
            height += 42f;
            if (expandedModules.contains(module.getId())) {
                for (dev.kairos.ui.components.model.UiSetting<?> setting : module.getSettings()) {
                    if (setting.isVisible()) height += 26f;
                }
                height += 4f;
            }
        }
        return height;
    }

    private float visiblePanelHeight(PanelState state, float desiredHeight) {
        float desktopLimit = Math.max(HEADER_HEIGHT, getBounds().getBottom() - state.y - 16f);
        return Math.min(desiredHeight, Math.min(MAX_PANEL_HEIGHT, desktopLimit));
    }

    private ModuleHit moduleAt(float x, float y) {
        ModuleHit result = null;
        for (String categoryId : zOrder) {
            UiCategory category = categoryById(categoryId);
            PanelState state = states.get(categoryId);
            if (state.collapsed || x < state.x || x >= state.x + state.width) continue;
            float visibleHeight = visiblePanelHeight(state, panelHeight(category));
            if (y < state.y + HEADER_HEIGHT || y >= state.y + visibleHeight) continue;
            float rowY = state.y + 42f - state.scrollOffset;
            for (UiModule module : catalog.filter(category.getId(), "")) {
                if (new Rect(state.x + 8f, rowY, state.width - 16f, 34f).contains(x, y)) {
                    result = new ModuleHit(categoryId, module);
                }
                rowY += 42f;
                if (expandedModules.contains(module.getId())) {
                    for (dev.kairos.ui.components.model.UiSetting<?> setting : module.getSettings()) {
                        if (setting.isVisible()) rowY += 26f;
                    }
                    rowY += 4f;
                }
            }
        }
        return result;
    }

    public Set<String> getExpandedModules() { return expandedModules; }

    private void bringToFront(String categoryId) {
        zOrder.remove(categoryId);
        zOrder.add(categoryId);
    }

    private UiCategory categoryById(String id) {
        for (UiCategory category : catalog.getCategories()) {
            if (category.getId().equals(id)) return category;
        }
        throw new IllegalStateException("Unknown category: " + id);
    }

    private static String compact(Object value) {
        String text = String.valueOf(value);
        return text.length() > 10 ? text.substring(0, 9) + "…" : text;
    }

    private static final class ModuleHit {
        final String categoryId;
        final UiModule module;
        ModuleHit(String categoryId, UiModule module) {
            this.categoryId = categoryId;
            this.module = module;
        }
    }
}
