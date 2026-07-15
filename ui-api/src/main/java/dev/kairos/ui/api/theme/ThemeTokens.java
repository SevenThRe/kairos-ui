package dev.kairos.ui.api.theme;

public final class ThemeTokens {
    public final String fontRegular;
    public final String fontMedium;
    public final String fontSemibold;
    public final String fontCjkFallback;
    public final int backdrop;
    public final int window;
    public final int surface;
    public final int surfaceHover;
    public final int border;
    public final int textPrimary;
    public final int textSecondary;
    public final int accent;
    public final float windowRadius;
    public final float componentRadius;
    public final float spacing;
    public final float glassBlurRadius;
    public final long fastMotionMs;

    public ThemeTokens(String fontRegular, String fontMedium, String fontSemibold, String fontCjkFallback,
                       int backdrop, int window, int surface, int surfaceHover, int border,
                       int textPrimary, int textSecondary, int accent, float windowRadius,
                       float componentRadius, float spacing, float glassBlurRadius, long fastMotionMs) {
        this.fontRegular = fontRegular;
        this.fontMedium = fontMedium;
        this.fontSemibold = fontSemibold;
        this.fontCjkFallback = fontCjkFallback;
        this.backdrop = backdrop;
        this.window = window;
        this.surface = surface;
        this.surfaceHover = surfaceHover;
        this.border = border;
        this.textPrimary = textPrimary;
        this.textSecondary = textSecondary;
        this.accent = accent;
        this.windowRadius = windowRadius;
        this.componentRadius = componentRadius;
        this.spacing = spacing;
        this.glassBlurRadius = glassBlurRadius;
        this.fastMotionMs = fastMotionMs;
    }

    public static ThemeTokens kairosDark() {
        return new ThemeTokens("inter-regular", "inter-medium", "inter-semibold", "noto-sans-cjk",
            0x99080B0F, 0xD911171C, 0xE61A2127, 0xFF222A32,
            0x24FFFFFF, 0xFFF4F5F7, 0xFF9CA3AF, 0xFF7657F6, 12f, 7f, 8f, 18f, 180L);
    }
}
