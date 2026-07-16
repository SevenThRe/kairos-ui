package dev.kairos.ui.components.scene;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.input.EventResult;
import dev.kairos.ui.api.input.PointerAction;
import dev.kairos.ui.api.input.PointerEvent;
import dev.kairos.ui.api.render.UiCanvas;
import dev.kairos.ui.api.theme.ThemePack;
import dev.kairos.ui.api.theme.ThemeRegistry;
import dev.kairos.ui.api.theme.ThemeTokens;
import dev.kairos.ui.components.model.BooleanSetting;
import dev.kairos.ui.components.model.ColorSetting;
import dev.kairos.ui.components.model.EnumSetting;
import dev.kairos.ui.components.model.ModuleCatalog;
import dev.kairos.ui.components.model.MultiSelectSetting;
import dev.kairos.ui.components.model.NumberSetting;
import dev.kairos.ui.components.model.RangeSetting;
import dev.kairos.ui.components.model.RangeValue;
import dev.kairos.ui.components.model.UiCategory;
import dev.kairos.ui.components.model.UiModule;
import dev.kairos.ui.components.model.UiSetting;
import dev.kairos.ui.core.node.UiNode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Compact draggable category windows. Left click toggles modules, right click expands settings,
 * right click on a header collapses it, and numeric settings support direct dragging.
 */
public final class PanelDesktop extends UiNode {
    private static final float HEADER_HEIGHT = 34f;
    private static final float MODULE_HEIGHT = 30f;
    private static final float MAX_PANEL_HEIGHT = 620f;
    private static final float SCROLL_STEP = 30f;
    private final ModuleCatalog catalog;
    private volatile ThemeTokens theme;
    private final Map<String, PanelState> states = new LinkedHashMap<String, PanelState>();
    private final Set<String> expandedModules = new LinkedHashSet<String>();
    private final List<String> zOrder = new ArrayList<String>();
    private PanelState dragging;
    private SettingHit adjustingSetting;
    private float dragOffsetX;
    private float dragOffsetY;
    private boolean initiallyArranged;

    public PanelDesktop(ModuleCatalog catalog, ThemeTokens theme) {
        this.catalog = catalog;
        this.theme = theme;
        float x = 24f;
        for (UiCategory category : catalog.getCategories()) {
            states.put(category.getId(), new PanelState(x, 28f, 196f));
            zOrder.add(category.getId());
            x += 212f;
        }
    }

    public PanelDesktop(ModuleCatalog catalog, ThemeRegistry themes) {
        this(catalog, themes.getActive().getTokens());
        themes.addListener(new ThemeRegistry.Listener() {
            @Override public void onThemeChanged(ThemePack next) { PanelDesktop.this.theme = next.getTokens(); }
        });
    }

    @Override public void layout(Rect bounds) {
        super.layout(bounds);
        if (initiallyArranged) return;
        float gap = 14f;
        float x = bounds.getX() + 22f;
        float y = bounds.getY() + 26f;
        for (UiCategory category : catalog.getCategories()) {
            PanelState state = states.get(category.getId());
            if (x + state.width > bounds.getRight() - 22f) {
                x = bounds.getX() + 22f;
                y += 176f;
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
            renderPanel(canvas, categoryById(categoryId), states.get(categoryId));
        }
    }

    private void renderPanel(UiCanvas canvas, UiCategory category, PanelState state) {
        float desiredHeight = panelHeight(category);
        float height = state.collapsed ? HEADER_HEIGHT : visiblePanelHeight(state, desiredHeight);
        state.scrollOffset = clamp(state.scrollOffset, 0f, Math.max(0f, desiredHeight - height));
        Rect panel = new Rect(state.x, state.y, state.width, height);
        canvas.glass(panel, 9f, 15f, 0xD40D1115);
        canvas.roundedRect(new Rect(panel.getX(), panel.getY(), panel.getWidth(), HEADER_HEIGHT),
            9f, 0xE90A0D10);
        canvas.text(theme.fontSemibold, category.getDisplayName(), panel.getX() + 13f,
            panel.getY() + 22f, 12f, theme.textPrimary);
        canvas.text(theme.fontMedium, state.collapsed ? "+" : "−", panel.getRight() - 17f,
            panel.getY() + 22f, 12f, theme.textSecondary);
        if (state.collapsed) return;

        Rect contentClip = new Rect(panel.getX(), panel.getY() + HEADER_HEIGHT,
            panel.getWidth(), Math.max(0f, panel.getHeight() - HEADER_HEIGHT));
        canvas.pushClip(contentClip);
        float y = panel.getY() + HEADER_HEIGHT + 4f - state.scrollOffset;
        for (UiModule module : catalog.filter(category.getId(), "")) {
            boolean expanded = expandedModules.contains(module.getId());
            Rect row = new Rect(panel.getX() + 6f, y, panel.getWidth() - 12f, MODULE_HEIGHT);
            if (expanded) canvas.roundedRect(row, 5f, 0xA61C2228);
            if (module.isEnabled()) {
                canvas.roundedRect(new Rect(row.getX() + 8f, row.getY() + 13f, 4f, 4f), 2f, 0xFF56B7D5);
            }
            canvas.text(theme.fontRegular, module.getDisplayName(), row.getX() + 18f,
                row.getY() + 20f, 10.5f, module.isEnabled() ? 0xFF72C5DF : theme.textPrimary);
            canvas.text(theme.fontMedium, "...", row.getRight() - 18f, row.getY() + 18f,
                10f, theme.textSecondary);
            y += MODULE_HEIGHT;
            if (expanded) {
                for (UiSetting<?> setting : module.getSettings()) {
                    if (!setting.isVisible()) continue;
                    float settingHeight = settingHeight(setting);
                    renderSetting(canvas, setting,
                        new Rect(panel.getX() + 10f, y, panel.getWidth() - 20f, settingHeight));
                    y += settingHeight;
                }
                y += 3f;
            }
            canvas.fillRect(new Rect(panel.getX() + 13f, y + 1f, panel.getWidth() - 26f, 1f), 0x0FFFFFFF);
            y += 4f;
        }
        canvas.popClip();

        float overflow = desiredHeight - height;
        if (overflow > 0f) {
            float trackHeight = height - HEADER_HEIGHT - 12f;
            float thumbHeight = Math.max(26f, trackHeight * (height / desiredHeight));
            float thumbTravel = trackHeight - thumbHeight;
            float thumbY = panel.getY() + HEADER_HEIGHT + 6f + thumbTravel * (state.scrollOffset / overflow);
            canvas.roundedRect(new Rect(panel.getRight() - 4f, thumbY, 2f, thumbHeight), 1f, 0xBB7657F6);
        }
    }

    private void renderSetting(UiCanvas canvas, UiSetting<?> setting, Rect row) {
        canvas.text(theme.fontRegular, setting.getDisplayName(), row.getX() + 5f, row.getY() + 17f,
            9f, theme.textSecondary);
        if (setting instanceof BooleanSetting) {
            boolean enabled = ((BooleanSetting) setting).getValue();
            Rect track = new Rect(row.getRight() - 33f, row.getY() + 7f, 28f, 14f);
            canvas.roundedRect(track, 7f, enabled ? theme.accent : 0xFF3C434B);
            float knobX = enabled ? track.getRight() - 12f : track.getX() + 2f;
            canvas.roundedRect(new Rect(knobX, track.getY() + 2f, 10f, 10f), 5f, 0xFFF5F6F8);
            return;
        }
        if (setting instanceof NumberSetting) {
            NumberSetting number = (NumberSetting) setting;
            canvas.text(theme.fontMedium, compact(number.getValue()), row.getRight() - 34f,
                row.getY() + 17f, 8.5f, theme.textPrimary);
            float ratio = (float) ((number.getValue() - number.getMin()) / (number.getMax() - number.getMin()));
            renderTrack(canvas, row, ratio, ratio);
            return;
        }
        if (setting instanceof RangeSetting) {
            RangeSetting range = (RangeSetting) setting;
            RangeValue value = range.getValue();
            canvas.text(theme.fontMedium, compact(value), row.getRight() - 48f,
                row.getY() + 17f, 8.5f, theme.textPrimary);
            float low = (float) ((value.low - range.getMin()) / (range.getMax() - range.getMin()));
            float high = (float) ((value.high - range.getMin()) / (range.getMax() - range.getMin()));
            renderTrack(canvas, row, low, high);
            return;
        }
        if (setting instanceof ColorSetting) {
            int color = ((ColorSetting) setting).getValue();
            canvas.roundedRect(new Rect(row.getRight() - 23f, row.getY() + 6f, 18f, 14f), 4f,
                0xFF000000 | color);
            return;
        }
        String value = compact(setting.getValue());
        float width = Math.min(72f, Math.max(34f, value.length() * 5.5f + 12f));
        canvas.roundedRect(new Rect(row.getRight() - width - 5f, row.getY() + 4f, width, 18f),
            4f, 0xC5232A31);
        canvas.text(theme.fontMedium, value, row.getRight() - width + 2f, row.getY() + 17f,
            8.5f, theme.textPrimary);
    }

    private void renderTrack(UiCanvas canvas, Rect row, float low, float high) {
        float x = row.getX() + 5f;
        float width = row.getWidth() - 10f;
        float y = row.getBottom() - 7f;
        canvas.roundedRect(new Rect(x, y, width, 3f), 1.5f, 0xFF333A42);
        canvas.roundedRect(new Rect(x + width * low, y, Math.max(3f, width * (high - low)), 3f),
            1.5f, theme.accent);
        canvas.roundedRect(new Rect(x + width * high - 3f, y - 2f, 7f, 7f), 3.5f, 0xFFF4F3FF);
    }

    public Map<String, PanelState> getPanelStates() { return states; }
    public Set<String> getExpandedModules() { return expandedModules; }

    @Override public EventResult onPointer(PointerEvent event) {
        if (adjustingSetting != null) {
            if (event.getAction() == PointerAction.MOVE) {
                updateContinuousSetting(adjustingSetting, event.getX());
                return EventResult.HANDLED;
            }
            if (event.getAction() == PointerAction.UP) {
                updateContinuousSetting(adjustingSetting, event.getX());
                adjustingSetting = null;
                return EventResult.HANDLED;
            }
        }
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
            float desired = panelHeight(scrolled);
            float overflow = Math.max(0f, desired - visiblePanelHeight(state, desired));
            if (overflow == 0f) return EventResult.IGNORED;
            state.scrollOffset = clamp(state.scrollOffset - event.getScrollY() * SCROLL_STEP, 0f, overflow);
            return EventResult.HANDLED;
        }
        if (event.getAction() != PointerAction.DOWN) return EventResult.IGNORED;

        UiCategory header = panelHeaderAt(event.getX(), event.getY());
        if (header != null) {
            bringToFront(header.getId());
            PanelState state = states.get(header.getId());
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

        SettingHit settingHit = settingAt(event.getX(), event.getY());
        if (settingHit != null && event.getButton() == 0) {
            bringToFront(settingHit.categoryId);
            if (activateSetting(settingHit, event.getX())) return EventResult.CAPTURE_POINTER;
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

    private boolean activateSetting(SettingHit hit, float pointerX) {
        UiSetting<?> setting = hit.setting;
        if (setting instanceof BooleanSetting) {
            BooleanSetting value = (BooleanSetting) setting;
            value.setValue(!value.getValue());
            return false;
        }
        if (setting instanceof EnumSetting) {
            EnumSetting value = (EnumSetting) setting;
            int index = value.getOptions().indexOf(value.getValue());
            value.setValue(value.getOptions().get((index + 1) % value.getOptions().size()));
            return false;
        }
        if (setting instanceof MultiSelectSetting) {
            MultiSelectSetting value = (MultiSelectSetting) setting;
            String choice = value.getOptions().get(0);
            for (String option : value.getOptions()) {
                if (!value.getValue().contains(option)) { choice = option; break; }
            }
            value.toggle(choice);
            return false;
        }
        if (setting instanceof NumberSetting || setting instanceof RangeSetting) {
            adjustingSetting = hit;
            updateContinuousSetting(hit, pointerX);
            return true;
        }
        return false;
    }

    private void updateContinuousSetting(SettingHit hit, float pointerX) {
        float ratio = clamp((pointerX - hit.bounds.getX() - 5f) / (hit.bounds.getWidth() - 10f), 0f, 1f);
        if (hit.setting instanceof NumberSetting) {
            NumberSetting number = (NumberSetting) hit.setting;
            number.setValue(snap(number.getMin() + ratio * (number.getMax() - number.getMin()),
                number.getMin(), number.getMax(), number.getStep()));
        } else if (hit.setting instanceof RangeSetting) {
            RangeSetting range = (RangeSetting) hit.setting;
            RangeValue old = range.getValue();
            double next = snap(range.getMin() + ratio * (range.getMax() - range.getMin()),
                range.getMin(), range.getMax(), range.getStep());
            if (Math.abs(next - old.low) <= Math.abs(next - old.high)) {
                range.setValue(new RangeValue(Math.min(next, old.high), old.high));
            } else {
                range.setValue(new RangeValue(old.low, Math.max(next, old.low)));
            }
        }
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

    private float panelHeight(UiCategory category) {
        float height = HEADER_HEIGHT + 8f;
        for (UiModule module : catalog.filter(category.getId(), "")) {
            height += MODULE_HEIGHT + 4f;
            if (expandedModules.contains(module.getId())) {
                for (UiSetting<?> setting : module.getSettings()) if (setting.isVisible()) height += settingHeight(setting);
                height += 3f;
            }
        }
        return height;
    }

    private float visiblePanelHeight(PanelState state, float desiredHeight) {
        float desktopLimit = Math.max(HEADER_HEIGHT, getBounds().getBottom() - state.y - 14f);
        return Math.min(desiredHeight, Math.min(MAX_PANEL_HEIGHT, desktopLimit));
    }

    private ModuleHit moduleAt(float x, float y) {
        ModuleHit result = null;
        for (String categoryId : zOrder) {
            UiCategory category = categoryById(categoryId);
            PanelState state = states.get(categoryId);
            if (!insideContent(state, category, x, y)) continue;
            float rowY = state.y + HEADER_HEIGHT + 4f - state.scrollOffset;
            for (UiModule module : catalog.filter(category.getId(), "")) {
                if (new Rect(state.x + 6f, rowY, state.width - 12f, MODULE_HEIGHT).contains(x, y)) {
                    result = new ModuleHit(categoryId, module);
                }
                rowY += MODULE_HEIGHT;
                if (expandedModules.contains(module.getId())) {
                    for (UiSetting<?> setting : module.getSettings()) if (setting.isVisible()) rowY += settingHeight(setting);
                    rowY += 3f;
                }
                rowY += 4f;
            }
        }
        return result;
    }

    private SettingHit settingAt(float x, float y) {
        SettingHit result = null;
        for (String categoryId : zOrder) {
            UiCategory category = categoryById(categoryId);
            PanelState state = states.get(categoryId);
            if (!insideContent(state, category, x, y)) continue;
            float rowY = state.y + HEADER_HEIGHT + 4f - state.scrollOffset;
            for (UiModule module : catalog.filter(category.getId(), "")) {
                rowY += MODULE_HEIGHT;
                if (expandedModules.contains(module.getId())) {
                    for (UiSetting<?> setting : module.getSettings()) {
                        if (!setting.isVisible()) continue;
                        Rect bounds = new Rect(state.x + 10f, rowY, state.width - 20f, settingHeight(setting));
                        if (bounds.contains(x, y)) result = new SettingHit(categoryId, setting, bounds);
                        rowY += bounds.getHeight();
                    }
                    rowY += 3f;
                }
                rowY += 4f;
            }
        }
        return result;
    }

    private boolean insideContent(PanelState state, UiCategory category, float x, float y) {
        if (state.collapsed || x < state.x || x >= state.x + state.width) return false;
        float visibleHeight = visiblePanelHeight(state, panelHeight(category));
        return y >= state.y + HEADER_HEIGHT && y < state.y + visibleHeight;
    }

    private void bringToFront(String categoryId) {
        zOrder.remove(categoryId);
        zOrder.add(categoryId);
    }

    private UiCategory categoryById(String id) {
        for (UiCategory category : catalog.getCategories()) if (category.getId().equals(id)) return category;
        throw new IllegalStateException("Unknown category: " + id);
    }

    private static float settingHeight(UiSetting<?> setting) {
        return setting instanceof NumberSetting || setting instanceof RangeSetting ? 34f : 27f;
    }

    private static double snap(double value, double min, double max, double step) {
        double snapped = min + Math.round((value - min) / step) * step;
        return Math.max(min, Math.min(max, snapped));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String compact(Object value) {
        if (value instanceof Double) return String.format(Locale.ROOT, "%.2f", (Double) value);
        if (value instanceof RangeValue) {
            RangeValue range = (RangeValue) value;
            return String.format(Locale.ROOT, "%.1f–%.1f", range.low, range.high);
        }
        String text = String.valueOf(value);
        return text.length() > 12 ? text.substring(0, 11) + "…" : text;
    }

    private static final class ModuleHit {
        final String categoryId;
        final UiModule module;
        ModuleHit(String categoryId, UiModule module) {
            this.categoryId = categoryId;
            this.module = module;
        }
    }

    private static final class SettingHit {
        final String categoryId;
        final UiSetting<?> setting;
        final Rect bounds;
        SettingHit(String categoryId, UiSetting<?> setting, Rect bounds) {
            this.categoryId = categoryId;
            this.setting = setting;
            this.bounds = bounds;
        }
    }
}
