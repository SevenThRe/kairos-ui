package dev.kairos.ui.esp;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

/** Dependency-free ESP style codec; safe to load on Java 8 endpoints. */
public final class EspStyleCodec {
    private EspStyleCodec() {}

    public static String encode(EspStyle s) {
        return "boxMode=" + s.boxMode.name() + '\n'
            + "fill=" + s.fill + '\n' + "healthBar=" + s.healthBar + '\n'
            + "name=" + s.name + '\n' + "distance=" + s.distance + '\n'
            + "showInvisible=" + s.showInvisible + '\n' + "lineWidth=" + s.lineWidth + '\n'
            + "color.enemy=" + hex(s.enemyColor) + '\n' + "color.friend=" + hex(s.friendColor) + '\n'
            + "color.fill=" + hex(s.fillColor) + '\n' + "itemLabel=" + s.itemLabel + '\n'
            + "armor=" + s.armor + '\n' + "hardOutline=" + s.hardOutline + '\n'
            + "font=" + s.font + '\n';
    }

    public static EspStyle decode(Reader reader) throws IOException {
        Properties p = new Properties();
        p.load(reader);
        return new EspStyle(EspStyle.BoxMode.valueOf(required(p, "boxMode")), bool(p, "fill"),
            bool(p, "healthBar"), bool(p, "name"), bool(p, "distance"), bool(p, "showInvisible"),
            Float.parseFloat(required(p, "lineWidth")), color(p, "color.enemy"), color(p, "color.friend"),
            color(p, "color.fill"), bool(p, "itemLabel"), bool(p, "armor"), bool(p, "hardOutline"),
            required(p, "font"));
    }

    private static boolean bool(Properties p, String key) { return Boolean.parseBoolean(required(p, key)); }
    private static String hex(int value) { return String.format("%08X", value); }
    private static int color(Properties p, String key) {
        return (int) Long.parseLong(required(p, key).replace("#", ""), 16);
    }
    private static String required(Properties p, String key) {
        String value = p.getProperty(key);
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("Missing ESP style key: " + key);
        return value.trim();
    }
}
