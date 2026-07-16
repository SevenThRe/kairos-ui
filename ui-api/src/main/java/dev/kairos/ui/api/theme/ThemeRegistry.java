package dev.kairos.ui.api.theme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Registry and hot-switch signal shared by ClickGUI, HUD and overlay renderers. */
public final class ThemeRegistry {
    public interface Listener { void onThemeChanged(ThemePack theme); }

    private final Map<String, ThemePack> themes = new LinkedHashMap<String, ThemePack>();
    private final List<Listener> listeners = new ArrayList<Listener>();
    private ThemePack active;

    public synchronized ThemeRegistry register(ThemePack theme) {
        themes.put(theme.getId(), theme);
        if (active == null) active = theme;
        return this;
    }

    public synchronized ThemePack activate(String id) {
        ThemePack next = themes.get(id);
        if (next == null) throw new IllegalArgumentException("Unknown theme: " + id);
        active = next;
        List<Listener> copy = new ArrayList<Listener>(listeners);
        for (Listener listener : copy) listener.onThemeChanged(next);
        return next;
    }

    public synchronized ThemePack getActive() {
        if (active == null) throw new IllegalStateException("No themes registered");
        return active;
    }

    public synchronized ThemePack get(String id) { return themes.get(id); }
    public synchronized List<ThemePack> getThemes() {
        return Collections.unmodifiableList(new ArrayList<ThemePack>(themes.values()));
    }
    public synchronized void addListener(Listener listener) {
        if (listener == null) throw new IllegalArgumentException("listener");
        listeners.add(listener);
    }
    public synchronized void removeListener(Listener listener) { listeners.remove(listener); }

    public static ThemeRegistry kairosDefaults() {
        ThemeTokens modern = ThemeTokens.kairosDark();
        ThemeRegistry registry = new ThemeRegistry();
        registry.register(new ThemePack("kairos-modern", "Kairos Modern", modern));
        registry.register(new ThemePack("obsidian-violet", "Obsidian Violet",
            palette(modern, 0xB505070B, 0xE20B0D12, 0xE714171E, 0xFF1D222B,
                0xFFF7F3FF, 0xFFAAA1B4, 0xFFA76CFF)));
        registry.register(new ThemePack("arctic-glass", "Arctic Glass",
            palette(modern, 0xA3091118, 0xD90E1922, 0xE6172630, 0xFF213541,
                0xFFF3FAFF, 0xFF9CB2C0, 0xFF4EC9E8)));
        registry.activate("kairos-modern");
        return registry;
    }

    private static ThemeTokens palette(ThemeTokens base, int backdrop, int window, int surface,
                                       int hover, int primary, int secondary, int accent) {
        return new ThemeTokens(base.fontRegular, base.fontMedium, base.fontSemibold, base.fontCjkFallback,
            backdrop, window, surface, hover, base.border, primary, secondary, accent,
            base.windowRadius, base.componentRadius, base.spacing, base.glassBlurRadius, base.fastMotionMs);
    }
}
