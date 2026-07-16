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
            0xA3070A0E, 0xD90D1218, 0xE6171E25, 0xFF202832,
            0x2EFFFFFF, 0xFFF4F6FA, 0xFFA1A9B5, 0xFF7B5CFA, 11f, 6f, 8f, 20f, 180L);
    }

    public ThemeTokens withAccent(int nextAccent) {
        return new ThemeTokens(fontRegular, fontMedium, fontSemibold, fontCjkFallback,
            backdrop, window, surface, surfaceHover, border, textPrimary, textSecondary,
            nextAccent, windowRadius, componentRadius, spacing, glassBlurRadius, fastMotionMs);
    }

    public ThemeTokens withEffects(float nextWindowRadius, float nextComponentRadius,
                                   float nextSpacing, float nextBlurRadius, long nextMotionMs) {
        return new ThemeTokens(fontRegular, fontMedium, fontSemibold, fontCjkFallback,
            backdrop, window, surface, surfaceHover, border, textPrimary, textSecondary,
            accent, nextWindowRadius, nextComponentRadius, nextSpacing, nextBlurRadius, nextMotionMs);
    }
}
