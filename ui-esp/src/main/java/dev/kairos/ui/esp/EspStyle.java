package dev.kairos.ui.esp;

import dev.kairos.ui.api.theme.ThemeTokens;

public final class EspStyle {
    public enum BoxMode { FULL, CORNERS }

    public final BoxMode boxMode;
    public final boolean fill;
    public final boolean healthBar;
    public final boolean name;
    public final boolean distance;
    public final boolean showInvisible;
    public final float lineWidth;
    public final int enemyColor;
    public final int friendColor;
    public final int fillColor;
    public final boolean itemLabel;
    public final boolean armor;
    public final boolean hardOutline;
    public final String font;

    public EspStyle(BoxMode boxMode, boolean fill, boolean healthBar, boolean name, boolean distance,
                    boolean showInvisible, float lineWidth, int enemyColor, int friendColor, int fillColor) {
        this.boxMode = boxMode;
        this.fill = fill;
        this.healthBar = healthBar;
        this.name = name;
        this.distance = distance;
        this.showInvisible = showInvisible;
        this.lineWidth = Math.max(1f, lineWidth);
        this.enemyColor = enemyColor;
        this.friendColor = friendColor;
        this.fillColor = fillColor;
        this.itemLabel = false;
        this.armor = false;
        this.hardOutline = false;
        this.font = "inter-medium";
    }

    public EspStyle(BoxMode boxMode, boolean fill, boolean healthBar, boolean name, boolean distance,
                    boolean showInvisible, float lineWidth, int enemyColor, int friendColor, int fillColor,
                    boolean itemLabel, boolean armor, boolean hardOutline, String font) {
        this.boxMode = boxMode;
        this.fill = fill;
        this.healthBar = healthBar;
        this.name = name;
        this.distance = distance;
        this.showInvisible = showInvisible;
        this.lineWidth = Math.max(1f, lineWidth);
        this.enemyColor = enemyColor;
        this.friendColor = friendColor;
        this.fillColor = fillColor;
        this.itemLabel = itemLabel;
        this.armor = armor;
        this.hardOutline = hardOutline;
        this.font = font == null ? "jetbrains-mono" : font;
    }

    public static EspStyle kairosModern(ThemeTokens theme) {
        return new EspStyle(BoxMode.CORNERS, true, true, true, true, false,
            1.5f, theme.accent, 0xFF4AD7A2, 0x24110D22);
    }

    public static EspStyle competitivePixel() {
        return new EspStyle(BoxMode.FULL, false, true, true, true, false,
            1f, 0xFFFF4D55, 0xFF4BE37E, 0x20101010, true, true, true, "jetbrains-mono");
    }
}
