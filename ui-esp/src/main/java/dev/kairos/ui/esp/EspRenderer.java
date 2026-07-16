package dev.kairos.ui.esp;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.render.UiCanvas;
import dev.kairos.ui.api.theme.ThemeTokens;
import java.util.List;
import java.util.Locale;

/** Shared 2D ESP renderer. Entity discovery and matrices stay in version-specific adapters. */
public final class EspRenderer {
    public void render(UiCanvas canvas, List<EspEntity> entities, WorldToScreenProjector projector,
                       EspStyle style, ThemeTokens theme) {
        for (EspEntity entity : entities) {
            if (entity.isInvisible() && !style.showInvisible) continue;
            Rect screen = project(entity.getBounds(), projector);
            if (screen == null || screen.getWidth() < 2f || screen.getHeight() < 4f) continue;
            int color = entity.isFriend() ? style.friendColor : style.enemyColor;
            if (style.fill) canvas.fillRect(screen, style.fillColor);
            if (style.boxMode == EspStyle.BoxMode.CORNERS) corners(canvas, screen, color, style.lineWidth);
            else outline(canvas, screen, color, style.lineWidth);
            if (style.healthBar) health(canvas, screen, entity);
            if (style.name) label(canvas, screen, entity, style, theme);
        }
    }

    private Rect project(WorldBounds b, WorldToScreenProjector projector) {
        double[] xs = {b.minX, b.maxX};
        double[] ys = {b.minY, b.maxY};
        double[] zs = {b.minZ, b.maxZ};
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        int visible = 0;
        for (double x : xs) for (double y : ys) for (double z : zs) {
            ScreenPoint point = projector.project(x, y, z);
            if (!point.visible) continue;
            minX = Math.min(minX, point.x);
            minY = Math.min(minY, point.y);
            maxX = Math.max(maxX, point.x);
            maxY = Math.max(maxY, point.y);
            visible++;
        }
        return visible < 2 ? null : new Rect(minX, minY, maxX - minX, maxY - minY);
    }

    private void outline(UiCanvas canvas, Rect r, int color, float width) {
        canvas.fillRect(new Rect(r.getX(), r.getY(), r.getWidth(), width), color);
        canvas.fillRect(new Rect(r.getX(), r.getBottom() - width, r.getWidth(), width), color);
        canvas.fillRect(new Rect(r.getX(), r.getY(), width, r.getHeight()), color);
        canvas.fillRect(new Rect(r.getRight() - width, r.getY(), width, r.getHeight()), color);
    }

    private void corners(UiCanvas canvas, Rect r, int color, float width) {
        float armX = Math.max(6f, r.getWidth() * 0.28f);
        float armY = Math.max(7f, r.getHeight() * 0.20f);
        line(canvas, r.getX(), r.getY(), armX, width, color, true, true);
        line(canvas, r.getRight(), r.getY(), armX, width, color, false, true);
        line(canvas, r.getX(), r.getBottom(), armX, width, color, true, false);
        line(canvas, r.getRight(), r.getBottom(), armX, width, color, false, false);
        canvas.fillRect(new Rect(r.getX(), r.getY(), width, armY), color);
        canvas.fillRect(new Rect(r.getRight() - width, r.getY(), width, armY), color);
        canvas.fillRect(new Rect(r.getX(), r.getBottom() - armY, width, armY), color);
        canvas.fillRect(new Rect(r.getRight() - width, r.getBottom() - armY, width, armY), color);
    }

    private void line(UiCanvas canvas, float x, float y, float arm, float width, int color,
                      boolean right, boolean down) {
        canvas.fillRect(new Rect(right ? x : x - arm, down ? y : y - width, arm, width), color);
    }

    private void health(UiCanvas canvas, Rect r, EspEntity entity) {
        float ratio = entity.getMaxHealth() <= 0f ? 0f
            : Math.max(0f, Math.min(1f, entity.getHealth() / entity.getMaxHealth()));
        canvas.fillRect(new Rect(r.getX() - 6f, r.getY(), 3f, r.getHeight()), 0xB9000000);
        float height = r.getHeight() * ratio;
        int green = Math.round(210f * ratio);
        int red = Math.round(225f * (1f - ratio));
        int color = 0xFF000000 | (red << 16) | (green << 8) | 0x54;
        canvas.fillRect(new Rect(r.getX() - 6f, r.getBottom() - height, 3f, height), color);
    }

    private void label(UiCanvas canvas, Rect r, EspEntity entity, EspStyle style, ThemeTokens theme) {
        String text = entity.getDisplayName();
        if (style.distance) text += "  " + String.format(Locale.ROOT, "%.1fm", entity.getDistance());
        float size = 10f;
        float width = canvas.measureText(theme.fontMedium, text, size);
        float x = r.getX() + (r.getWidth() - width) * 0.5f;
        canvas.roundedRect(new Rect(x - 5f, r.getY() - 17f, width + 10f, 14f), 4f, 0xB80A0D12);
        canvas.text(theme.fontMedium, text, x, r.getY() - 7f, size, theme.textPrimary);
    }
}
