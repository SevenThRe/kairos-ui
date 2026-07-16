package dev.kairos.ui.theme;

import dev.kairos.ui.api.theme.ThemeCodec;
import dev.kairos.ui.api.theme.ThemePack;
import dev.kairos.ui.api.theme.ThemeRegistry;
import java.io.StringReader;

public final class ThemeSystemTest {
    public static void main(String[] args) throws Exception {
        ThemeRegistry registry = ThemeRegistry.kairosDefaults();
        require(registry.getThemes().size() == 3, "three built-in themes");
        final boolean[] changed = {false};
        registry.addListener(new ThemeRegistry.Listener() {
            @Override public void onThemeChanged(ThemePack theme) { changed[0] = "arctic-glass".equals(theme.getId()); }
        });
        registry.activate("arctic-glass");
        require(changed[0], "theme switch notifies bound scenes");
        String encoded = ThemeCodec.encode(registry.getActive());
        ThemePack decoded = ThemeCodec.decode(new StringReader(encoded));
        require(decoded.getTokens().accent == registry.getActive().getTokens().accent, "theme color roundtrip");
        require(decoded.getTokens().glassBlurRadius == 20f, "theme metric roundtrip");
        System.out.println("ThemeSystemTest passed");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
