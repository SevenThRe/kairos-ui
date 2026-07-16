package dev.kairos.ui.components.hud;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

/** Properties codec so HUD styling can be changed without recompiling a client. */
public final class CombatHudProfileCodec {
    private CombatHudProfileCodec() {}

    public static String encode(CombatHudProfile p) {
        return "font=" + p.font + '\n'
            + "color.panel=" + hex(p.panel) + '\n'
            + "color.border=" + hex(p.border) + '\n'
            + "color.text=" + hex(p.text) + '\n'
            + "color.muted=" + hex(p.muted) + '\n'
            + "color.enemy=" + hex(p.enemy) + '\n'
            + "color.friend=" + hex(p.friend) + '\n'
            + "color.health=" + hex(p.health) + '\n'
            + "color.damage=" + hex(p.damage) + '\n'
            + "pixelSnap=" + p.pixelSnap + '\n';
    }

    public static CombatHudProfile decode(Reader reader) throws IOException {
        Properties p = new Properties();
        p.load(reader);
        return new CombatHudProfile(required(p, "font"), color(p, "color.panel"),
            color(p, "color.border"), color(p, "color.text"), color(p, "color.muted"),
            color(p, "color.enemy"), color(p, "color.friend"), color(p, "color.health"),
            color(p, "color.damage"), Boolean.parseBoolean(required(p, "pixelSnap")));
    }

    private static String hex(int value) { return String.format("%08X", value); }
    private static int color(Properties p, String key) {
        return (int) Long.parseLong(required(p, key).replace("#", ""), 16);
    }
    private static String required(Properties p, String key) {
        String value = p.getProperty(key);
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("Missing HUD theme key: " + key);
        return value.trim();
    }
}
