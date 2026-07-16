package dev.kairos.ui.platform;

import dev.kairos.ui.api.theme.ThemePack;
import dev.kairos.ui.api.theme.ThemeRegistry;
import java.io.File;
import java.nio.file.Files;

public final class ThemeDirectoryTest {
    public static void main(String[] args) throws Exception {
        File root = Files.createTempDirectory("kairos-theme-test").toFile();
        ThemeRegistry source = ThemeRegistry.kairosDefaults();
        ThemeDirectory directory = new ThemeDirectory(root);
        ThemePack custom = new ThemePack("custom-test", "Custom Test",
            source.getActive().getTokens().withAccent(0xFF123456));
        directory.saveTheme(custom);
        source.register(custom);
        directory.select(source, "custom-test");

        ThemeRegistry restored = ThemeRegistry.kairosDefaults();
        require(directory.loadInto(restored) == 1, "one custom theme loaded");
        require("custom-test".equals(restored.getActive().getId()), "selected theme restored");
        require(restored.getActive().getTokens().accent == 0xFF123456, "custom palette restored");
        System.out.println("ThemeDirectoryTest passed");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
