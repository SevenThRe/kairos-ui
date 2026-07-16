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
    }

    public static EspStyle kairosModern(ThemeTokens theme) {
        return new EspStyle(BoxMode.CORNERS, true, true, true, true, false,
            1.5f, theme.accent, 0xFF4AD7A2, 0x24110D22);
    }
}
