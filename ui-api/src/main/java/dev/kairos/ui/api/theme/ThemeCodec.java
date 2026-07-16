package dev.kairos.ui.api.theme;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

/** Dependency-free theme persistence supported on both Java 8 and modern runtimes. */
public final class ThemeCodec {
    private ThemeCodec() {}

    public static String encode(ThemePack pack) {
        ThemeTokens t = pack.getTokens();
        StringBuilder out = new StringBuilder();
        line(out, "id", pack.getId());
        line(out, "name", pack.getDisplayName());
        line(out, "font.regular", t.fontRegular);
        line(out, "font.medium", t.fontMedium);
        line(out, "font.semibold", t.fontSemibold);
        line(out, "font.cjk", t.fontCjkFallback);
        line(out, "color.backdrop", hex(t.backdrop));
        line(out, "color.window", hex(t.window));
        line(out, "color.surface", hex(t.surface));
        line(out, "color.surfaceHover", hex(t.surfaceHover));
        line(out, "color.border", hex(t.border));
        line(out, "color.textPrimary", hex(t.textPrimary));
        line(out, "color.textSecondary", hex(t.textSecondary));
        line(out, "color.accent", hex(t.accent));
        line(out, "metric.windowRadius", t.windowRadius);
        line(out, "metric.componentRadius", t.componentRadius);
        line(out, "metric.spacing", t.spacing);
        line(out, "effect.blurRadius", t.glassBlurRadius);
        line(out, "motion.fastMs", t.fastMotionMs);
        return out.toString();
    }

    public static ThemePack decode(Reader reader) throws IOException {
        Properties p = new Properties();
        p.load(reader);
        ThemeTokens t = new ThemeTokens(
            required(p, "font.regular"), required(p, "font.medium"), required(p, "font.semibold"),
            required(p, "font.cjk"), color(p, "color.backdrop"), color(p, "color.window"),
            color(p, "color.surface"), color(p, "color.surfaceHover"), color(p, "color.border"),
            color(p, "color.textPrimary"), color(p, "color.textSecondary"), color(p, "color.accent"),
            decimal(p, "metric.windowRadius"), decimal(p, "metric.componentRadius"),
            decimal(p, "metric.spacing"), decimal(p, "effect.blurRadius"),
            Long.parseLong(required(p, "motion.fastMs")));
        return new ThemePack(required(p, "id"), required(p, "name"), t);
    }

    private static void line(StringBuilder out, String key, Object value) {
        out.append(key).append('=').append(value).append('\n');
    }
    private static String hex(int value) { return String.format("%08X", value); }
    private static String required(Properties p, String key) {
        String value = p.getProperty(key);
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("Missing theme key: " + key);
        return value.trim();
    }
    private static int color(Properties p, String key) {
        return (int) Long.parseLong(required(p, key).replace("#", ""), 16);
    }
    private static float decimal(Properties p, String key) { return Float.parseFloat(required(p, key)); }
}
