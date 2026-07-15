package dev.kairos.ui.components.scene;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.input.EventResult;
import dev.kairos.ui.api.input.KeyAction;
import dev.kairos.ui.api.input.PointerAction;
import dev.kairos.ui.api.input.PointerEvent;
import dev.kairos.ui.api.input.UiKeyEvent;
import dev.kairos.ui.api.render.UiCanvas;
import dev.kairos.ui.api.theme.ThemeTokens;
import dev.kairos.ui.components.model.ModuleCatalog;
import dev.kairos.ui.components.model.UiCategory;
import dev.kairos.ui.components.model.UiModule;
import dev.kairos.ui.components.model.UiSetting;
import dev.kairos.ui.components.model.BooleanSetting;
import dev.kairos.ui.components.model.EnumSetting;
import dev.kairos.ui.components.model.NumberSetting;
import dev.kairos.ui.components.model.MultiSelectSetting;
import dev.kairos.ui.components.model.ColorSetting;
import dev.kairos.ui.components.model.KeybindSetting;
import dev.kairos.ui.components.model.RangeSetting;
import dev.kairos.ui.components.model.TextSetting;
import dev.kairos.ui.core.node.UiNode;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class ModernWorkbench extends UiNode {
    private final ModuleCatalog catalog;
    private final WorkbenchState state;
    private final ThemeTokens theme;
    private boolean searchFocused;

    public ModernWorkbench(ModuleCatalog catalog, WorkbenchState state, ThemeTokens theme) {
        this.catalog = catalog;
        this.state = state;
        this.theme = theme;
        setFocusable(true);
    }

    @Override protected void render(UiCanvas canvas) {
        Rect window = getBounds();
        canvas.glass(window, theme.windowRadius, theme.glassBlurRadius, theme.window);
        canvas.pushClip(window);

        float headerHeight = headerHeight();
        float sidebarWidth = sidebarWidth();
        float moduleWidth = moduleWidth();
        float contentY = window.getY() + headerHeight;
        float contentHeight = window.getHeight() - headerHeight;

        canvas.fillRect(new Rect(window.getX(), window.getY(), window.getWidth(), headerHeight), 0xD9161D24);
        canvas.text(theme.fontSemibold, "Kairos", window.getX() + 24f, window.getY() + 34f, 16f, theme.textPrimary);
        String searchText = state.getSearchQuery().isEmpty() ? "Search modules..." : state.getSearchQuery();
        canvas.text(theme.fontRegular, searchText, window.getX() + sidebarWidth + 28f,
            window.getY() + 33f, 12f, theme.textSecondary);
        if (searchFocused) {
            canvas.fillRect(new Rect(window.getX() + sidebarWidth + 24f, window.getY() + 43f,
                Math.min(260f, moduleWidth - 32f), 1f), theme.accent);
        }

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
        List<String> groups = groupsFor(module);
        if (!groups.contains(state.getActiveGroup())) state.setActiveGroup(groups.get(0));
        float groupX = area.getX() + 20f;
        for (String group : groups) {
            boolean active = group.equals(state.getActiveGroup());
            canvas.text(theme.fontMedium, group, groupX, area.getY() + 64f, 10f,
                active ? theme.textPrimary : theme.textSecondary);
            if (active) canvas.roundedRect(new Rect(groupX, area.getY() + 72f, 58f, 2f), 1f, theme.accent);
            groupX += 78f;
        }

        float y = area.getY() + 92f;
        for (UiSetting<?> setting : module.getSettings()) {
            if (!setting.isVisible() || !state.getActiveGroup().equals(setting.getGroup())) continue;
            canvas.text(theme.fontRegular, setting.getDisplayName(), area.getX() + 22f, y + 17f,
                11f, theme.textPrimary);
            renderControl(canvas, area, y, setting);
            canvas.fillRect(new Rect(area.getX() + 22f, y + 37f, area.getWidth() - 44f, 1f), theme.border);
            y += 46f;
        }
    }

    private void renderControl(UiCanvas canvas, Rect area, float y, UiSetting<?> setting) {
        float right = area.getRight() - 24f;
        if (setting instanceof BooleanSetting) {
            boolean enabled = ((BooleanSetting) setting).getValue();
            canvas.roundedRect(new Rect(right - 32f, y + 7f, 32f, 18f), 9f, enabled ? theme.accent : 0xFF3A424A);
            canvas.roundedRect(new Rect(right - (enabled ? 16f : 30f), y + 9f, 14f, 14f), 7f, 0xFFF6F7FA);
        } else if (setting instanceof NumberSetting) {
            NumberSetting number = (NumberSetting) setting;
            float start = area.getX() + 150f;
            float width = Math.max(70f, area.getWidth() - 250f);
            float progress = (float) ((number.getValue() - number.getMin()) / (number.getMax() - number.getMin()));
            canvas.roundedRect(new Rect(start, y + 14f, width, 4f), 2f, 0xFF313942);
            canvas.roundedRect(new Rect(start, y + 14f, width * progress, 4f), 2f, theme.accent);
            canvas.roundedRect(new Rect(start + width * progress - 4f, y + 12f, 8f, 8f), 4f, 0xFFF5F5FF);
            canvas.text(theme.fontMedium, compact(number.getValue()), right - 42f, y + 20f, 10f, theme.textSecondary);
        } else if (setting instanceof RangeSetting) {
            RangeSetting range = (RangeSetting) setting;
            float start = area.getX() + 150f;
            float width = Math.max(70f, area.getWidth() - 250f);
            float low = (float) ((range.getValue().low - range.getMin()) / (range.getMax() - range.getMin()));
            float high = (float) ((range.getValue().high - range.getMin()) / (range.getMax() - range.getMin()));
            canvas.roundedRect(new Rect(start, y + 14f, width, 4f), 2f, 0xFF313942);
            canvas.roundedRect(new Rect(start + width * low, y + 14f, width * (high - low), 4f), 2f, theme.accent);
            canvas.text(theme.fontMedium, compact(range.getValue().low) + "–" + compact(range.getValue().high),
                right - 55f, y + 20f, 10f, theme.textSecondary);
        } else if (setting instanceof ColorSetting) {
            int color = ((ColorSetting) setting).getValue();
            canvas.roundedRect(new Rect(right - 30f, y + 6f, 30f, 22f), 5f, color);
        } else {
            String value = formatValue(setting);
            float boxWidth = Math.min(150f, Math.max(62f, value.length() * 7f + 24f));
            canvas.roundedRect(new Rect(right - boxWidth, y + 4f, boxWidth, 26f), 5f, 0xFF222A31);
            canvas.text(setting instanceof KeybindSetting ? "jetbrains-mono" : theme.fontMedium,
                value, right - boxWidth + 10f, y + 21f, 10f, theme.textSecondary);
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

    @Override public EventResult onPointer(PointerEvent event) {
        if (event.getAction() != PointerAction.DOWN || event.getButton() != 0) return EventResult.IGNORED;
        Rect window = getBounds();
        float header = headerHeight();
        float sidebar = sidebarWidth();
        float moduleColumn = moduleWidth();

        Rect search = new Rect(window.getX() + sidebar + 18f, window.getY() + 8f,
            Math.min(280f, moduleColumn - 24f), header - 16f);
        searchFocused = search.contains(event.getX(), event.getY());
        if (searchFocused) return EventResult.HANDLED;

        if (event.getX() < window.getX() + sidebar && event.getY() >= window.getY() + header) {
            int index = (int) ((event.getY() - (window.getY() + header + 18f)) / 44f);
            if (index >= 0 && index < catalog.getCategories().size()) {
                String category = catalog.getCategories().get(index).getId();
                state.setSelectedCategoryId(category);
                List<UiModule> filtered = catalog.filter(category, state.getSearchQuery());
                state.setSelectedModuleId(filtered.isEmpty() ? null : filtered.get(0).getId());
                return EventResult.HANDLED;
            }
        }

        float modulesX = window.getX() + sidebar;
        float modulesY = window.getY() + header + 44f;
        if (event.getX() >= modulesX && event.getX() < modulesX + moduleColumn && event.getY() >= modulesY) {
            List<UiModule> modules = catalog.filter(state.getSelectedCategoryId(), state.getSearchQuery());
            int index = (int) ((event.getY() - modulesY) / 60f);
            if (index >= 0 && index < modules.size()) {
                UiModule module = modules.get(index);
                float localX = event.getX() - modulesX;
                if (localX >= moduleColumn - 66f) module.setEnabled(!module.isEnabled());
                else {
                    state.setSelectedModuleId(module.getId());
                    state.setActiveGroup("General");
                }
                return EventResult.HANDLED;
            }
        }

        UiModule module = selectedModule();
        float settingsX = modulesX + moduleColumn;
        float areaY = window.getY() + header;
        if (module != null && event.getX() >= settingsX && event.getY() >= areaY + 44f
            && event.getY() < areaY + 82f) {
            List<String> groups = groupsFor(module);
            int group = (int) ((event.getX() - (settingsX + 20f)) / 78f);
            if (group >= 0 && group < groups.size()) {
                state.setActiveGroup(groups.get(group));
                return EventResult.HANDLED;
            }
        }
        float settingStartY = areaY + 92f;
        if (module != null && event.getX() >= settingsX && event.getY() >= settingStartY) {
            int row = 0;
            for (UiSetting<?> setting : module.getSettings()) {
                if (!setting.isVisible() || !state.getActiveGroup().equals(setting.getGroup())) continue;
                float rowY = settingStartY + row * 46f;
                if (event.getY() >= rowY && event.getY() < rowY + 42f) {
                    changeSetting(setting, event.getX(), settingsX, window.getRight() - settingsX);
                    return EventResult.HANDLED;
                }
                row++;
            }
        }
        return EventResult.IGNORED;
    }

    @Override public EventResult onKey(UiKeyEvent event) {
        if (!searchFocused || (event.getAction() != KeyAction.DOWN && event.getAction() != KeyAction.TYPED)) {
            return EventResult.IGNORED;
        }
        if (event.getLogicalKey() == UiKeyEvent.KEY_ESCAPE || event.getLogicalKey() == UiKeyEvent.KEY_ENTER) {
            searchFocused = false;
            return EventResult.HANDLED;
        }
        if (event.getLogicalKey() == UiKeyEvent.KEY_BACKSPACE) {
            String query = state.getSearchQuery();
            if (!query.isEmpty()) state.setSearchQuery(query.substring(0, query.length() - 1));
            return EventResult.HANDLED;
        }
        char typed = event.getTypedCharacter();
        if (typed >= 32 && typed != 127) {
            state.setSearchQuery(state.getSearchQuery() + typed);
            return EventResult.HANDLED;
        }
        return EventResult.IGNORED;
    }

    private void changeSetting(UiSetting<?> setting, float pointerX, float settingsX, float settingsWidth) {
        if (setting instanceof BooleanSetting) {
            BooleanSetting toggle = (BooleanSetting) setting;
            toggle.setValue(!toggle.getValue());
        } else if (setting instanceof EnumSetting) {
            EnumSetting choice = (EnumSetting) setting;
            int index = choice.getOptions().indexOf(choice.getValue());
            choice.setValue(choice.getOptions().get((index + 1) % choice.getOptions().size()));
        } else if (setting instanceof NumberSetting) {
            NumberSetting number = (NumberSetting) setting;
            float start = settingsX + 150f;
            float end = settingsX + Math.max(160f, settingsWidth - 30f);
            double progress = Math.max(0d, Math.min(1d, (pointerX - start) / Math.max(1f, end - start)));
            double raw = number.getMin() + (number.getMax() - number.getMin()) * progress;
            double stepped = Math.round(raw / number.getStep()) * number.getStep();
            number.setValue(Math.max(number.getMin(), Math.min(number.getMax(), stepped)));
        } else if (setting instanceof MultiSelectSetting) {
            MultiSelectSetting multi = (MultiSelectSetting) setting;
            int index = multi.getValue().size() % multi.getOptions().size();
            multi.toggle(multi.getOptions().get(index));
        } else if (setting instanceof ColorSetting) {
            ColorSetting color = (ColorSetting) setting;
            int current = color.getValue();
            color.setValue((current & 0xFF000000) | (((current & 0x00FFFFFF) + 0x00251F47) & 0x00FFFFFF));
        }
    }

    private List<String> groupsFor(UiModule module) {
        Set<String> groups = new LinkedHashSet<String>();
        for (UiSetting<?> setting : module.getSettings()) if (setting.isVisible()) groups.add(setting.getGroup());
        if (groups.isEmpty()) groups.add("General");
        return new ArrayList<String>(groups);
    }

    private String formatValue(UiSetting<?> setting) {
        if (setting instanceof MultiSelectSetting) {
            Set<String> selected = ((MultiSelectSetting) setting).getValue();
            return selected.isEmpty() ? "None" : String.join(", ", selected);
        }
        return String.valueOf(setting.getValue());
    }

    private static String compact(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.00001d) return String.format(Locale.ROOT, "%.0f", value);
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private float headerHeight() { return 54f; }
    private float sidebarWidth() { return Math.min(190f, Math.max(150f, getBounds().getWidth() * 0.18f)); }
    private float moduleWidth() { return Math.min(360f, Math.max(260f, getBounds().getWidth() * 0.33f)); }
}
