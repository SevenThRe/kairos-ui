package dev.kairos.ui.components.hud;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.input.EventResult;
import dev.kairos.ui.api.input.PointerAction;
import dev.kairos.ui.api.input.PointerEvent;
import dev.kairos.ui.api.render.UiCanvas;
import dev.kairos.ui.api.theme.ThemePack;
import dev.kairos.ui.api.theme.ThemeRegistry;
import dev.kairos.ui.api.theme.ThemeTokens;
import dev.kairos.ui.core.node.UiNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Shared HUD renderer and editor state; live game values are supplied through HudModel. */
public final class HudScene extends UiNode {
    private final HudModel model;
    private volatile ThemeTokens theme;
    private final List<HudWidgetState> widgets = new ArrayList<HudWidgetState>();
    private HudWidgetState dragging;
    private float offsetX;
    private float offsetY;
    private long fixedNowMillis = Long.MIN_VALUE;

    public HudScene(HudModel model, ThemeTokens theme) {
        this.model = model;
        this.theme = theme;
        widgets.add(new HudWidgetState("watermark", new Rect(24f, 22f, 190f, 58f)));
        widgets.add(new HudWidgetState("module-list", new Rect(982f, 24f, 270f, 190f)));
        widgets.add(new HudWidgetState("session", new Rect(24f, 548f, 210f, 142f)));
        widgets.add(new HudWidgetState("target", new Rect(480f, 570f, 320f, 120f)));
        widgets.add(new HudWidgetState("notifications", new Rect(928f, 432f, 324f, 258f)));
    }

    public HudScene(HudModel model, ThemeRegistry themes) {
        this(model, themes.getActive().getTokens());
        themes.addListener(new ThemeRegistry.Listener() {
            @Override public void onThemeChanged(ThemePack next) { HudScene.this.theme = next.getTokens(); }
        });
    }

    @Override protected void render(UiCanvas canvas) {
        for (HudWidgetState widget : widgets) {
            if (!widget.visible) continue;
            if ("watermark".equals(widget.id)) renderWatermark(canvas, widget.bounds);
            else if ("module-list".equals(widget.id)) renderModuleList(canvas, widget.bounds);
            else if ("session".equals(widget.id)) renderSession(canvas, widget.bounds);
            else if ("target".equals(widget.id)) renderTarget(canvas, widget.bounds);
            else if ("notifications".equals(widget.id)) renderNotifications(canvas, widget.bounds);
        }
    }

    private void panel(UiCanvas canvas, Rect rect) {
        canvas.glass(rect, theme.windowRadius, theme.glassBlurRadius, theme.window);
    }

    private void renderWatermark(UiCanvas canvas, Rect r) {
        panel(canvas, r);
        canvas.roundedRect(new Rect(r.getX() + 12f, r.getY() + 12f, 34f, 34f), 8f, 0xCC5B3FD0);
        canvas.text(theme.fontSemibold, "K", r.getX() + 22f, r.getY() + 36f, 18f, theme.textPrimary);
        canvas.text(theme.fontSemibold, model.clientName + " " + model.clientVersion,
            r.getX() + 56f, r.getY() + 25f, 12f, theme.textPrimary);
        canvas.text(theme.fontRegular, model.fps + " FPS  ·  " + model.minecraftVersion,
            r.getX() + 56f, r.getY() + 43f, 9f, theme.textSecondary);
    }

    private void renderModuleList(UiCanvas canvas, Rect r) {
        final float size = 10.5f;
        List<HudModuleEntry> entries = new ArrayList<HudModuleEntry>(model.getModuleEntries());
        Collections.sort(entries, new Comparator<HudModuleEntry>() {
            @Override public int compare(HudModuleEntry left, HudModuleEntry right) {
                float l = canvas.measureText(theme.fontMedium, left.getDisplayText(), size);
                float rr = canvas.measureText(theme.fontMedium, right.getDisplayText(), size);
                return Float.compare(rr, l);
            }
        });
        float y = r.getY();
        for (HudModuleEntry entry : entries) {
            String suffix = entry.getSuffix().isEmpty() ? "" : "  " + entry.getSuffix();
            float nameWidth = canvas.measureText(theme.fontMedium, entry.getName(), size);
            float suffixWidth = canvas.measureText(theme.fontRegular, suffix, size);
            float rowWidth = Math.min(r.getWidth(), nameWidth + suffixWidth + 18f);
            float x = r.getRight() - rowWidth;
            int accent = entry.getAccentArgb() == 0 ? theme.accent : entry.getAccentArgb();
            if (model.moduleListBackground) {
                canvas.roundedRect(new Rect(x, y, rowWidth, 18f), 4f, 0xA90A0E12);
            }
            canvas.text(theme.fontMedium, entry.getName(), x + 7f, y + 12.5f, size, accent);
            if (!suffix.isEmpty()) {
                canvas.text(theme.fontRegular, suffix, x + 7f + nameWidth, y + 12.5f, size, 0xFFD6DAE0);
            }
            if (model.moduleListRightBar) {
                canvas.roundedRect(new Rect(r.getRight() - 2f, y + 2f, 2f, 14f), 1f, accent);
            }
            y += 20f;
        }
    }

    private void renderSession(UiCanvas canvas, Rect r) {
        panel(canvas, r);
        canvas.text(theme.fontSemibold, "Session", r.getX() + 16f, r.getY() + 23f, 11f, theme.textSecondary);
        canvas.text("jetbrains-mono", model.sessionTime, r.getX() + 16f, r.getY() + 53f, 20f, theme.textPrimary);
        canvas.text(theme.fontRegular, "Kills  " + model.kills, r.getX() + 16f, r.getY() + 82f, 10f, theme.textSecondary);
        canvas.text(theme.fontRegular, "Deaths  " + model.deaths, r.getX() + 110f, r.getY() + 82f, 10f, theme.textSecondary);
        float kd = model.deaths == 0 ? model.kills : (float) model.kills / model.deaths;
        canvas.text(theme.fontRegular, "K/D  " + String.format(Locale.ROOT, "%.2f", kd),
            r.getX() + 16f, r.getY() + 108f, 10f, theme.textSecondary);
        canvas.text(theme.fontRegular, "Wins  " + model.wins, r.getX() + 110f, r.getY() + 108f, 10f, theme.textSecondary);
    }

    private void renderTarget(UiCanvas canvas, Rect r) {
        panel(canvas, r);
        canvas.image("minecraft-head", new Rect(r.getX() + 16f, r.getY() + 18f, 46f, 46f), 0xFFB58662);
        canvas.text(theme.fontRegular, "Target", r.getX() + 76f, r.getY() + 29f, 9f, theme.textSecondary);
        canvas.text(theme.fontSemibold, model.targetName, r.getX() + 76f, r.getY() + 49f, 15f, theme.textPrimary);
        float health = Math.max(0f, Math.min(1f, model.targetHealth / model.targetMaxHealth));
        canvas.roundedRect(new Rect(r.getX() + 76f, r.getY() + 64f, r.getWidth() - 94f, 8f), 4f, 0xFF303841);
        canvas.roundedRect(new Rect(r.getX() + 76f, r.getY() + 64f, (r.getWidth() - 94f) * health, 8f), 4f, theme.accent);
        canvas.text(theme.fontRegular, "Distance  " + model.targetDistance + "   Health  "
            + Math.round(health * 100f) + "%", r.getX() + 76f, r.getY() + 94f, 9f, theme.textSecondary);
    }

    private void renderNotifications(UiCanvas canvas, Rect r) {
        long now = fixedNowMillis == Long.MIN_VALUE ? System.currentTimeMillis() : fixedNowMillis;
        List<KairosNotification> active = model.getNotificationCenter().snapshot(now);
        float cardWidth = Math.min(300f, r.getWidth());
        float bottom = r.getBottom();
        for (int index = active.size() - 1; index >= 0; index--) {
            KairosNotification notification = active.get(index);
            float visibility = notification.getVisibility(now);
            if (visibility <= 0f) continue;
            float height = 62f;
            bottom -= height;
            float x = r.getRight() - cardWidth + (1f - visibility) * (cardWidth + 16f);
            Rect card = new Rect(x, bottom, cardWidth, height);
            int surface = alpha(0xF20C1116, visibility);
            canvas.glass(card, 9f, theme.glassBlurRadius, surface);
            int accent = alpha(notification.getKind().getAccentArgb(), visibility);
            canvas.roundedRect(new Rect(x, bottom, 3f, height), 2f, accent);
            canvas.roundedRect(new Rect(x + 14f, bottom + 14f, 28f, 28f), 8f, alpha(0x33FFFFFF, visibility));
            canvas.roundedRect(new Rect(x + 23f, bottom + 23f, 10f, 10f), 5f, accent);
            canvas.text(theme.fontSemibold, notification.getTitle(), x + 52f, bottom + 24f,
                11f, alpha(theme.textPrimary, visibility));
            canvas.text(theme.fontRegular, notification.getMessage(), x + 52f, bottom + 43f,
                9.5f, alpha(theme.textSecondary, visibility));
            float remaining = notification.getRemainingFraction(now);
            canvas.roundedRect(new Rect(x + 10f, bottom + height - 4f, (cardWidth - 20f) * remaining, 2f),
                1f, accent);
            bottom -= 8f * visibility;
        }
    }

    public HudScene setNowMillis(long nowMillis) {
        fixedNowMillis = nowMillis;
        return this;
    }

    public HudScene useSystemTime() {
        fixedNowMillis = Long.MIN_VALUE;
        return this;
    }

    private static int alpha(int argb, float factor) {
        int a = (argb >>> 24) & 255;
        int next = Math.max(0, Math.min(255, Math.round(a * factor)));
        return (next << 24) | (argb & 0xFFFFFF);
    }

    @Override public EventResult onPointer(PointerEvent event) {
        if (dragging != null) {
            if (event.getAction() == PointerAction.MOVE) {
                Rect root = getBounds();
                Rect old = dragging.bounds;
                float x = Math.max(root.getX(), Math.min(root.getRight() - old.getWidth(), event.getX() - offsetX));
                float y = Math.max(root.getY(), Math.min(root.getBottom() - old.getHeight(), event.getY() - offsetY));
                dragging.bounds = new Rect(x, y, old.getWidth(), old.getHeight());
                return EventResult.HANDLED;
            }
            if (event.getAction() == PointerAction.UP) {
                dragging = null;
                return EventResult.HANDLED;
            }
        }
        if (event.getAction() == PointerAction.DOWN && event.getButton() == 0) {
            for (int i = widgets.size() - 1; i >= 0; i--) {
                HudWidgetState widget = widgets.get(i);
                if (widget.visible && widget.bounds.contains(event.getX(), event.getY())) {
                    dragging = widget;
                    offsetX = event.getX() - widget.bounds.getX();
                    offsetY = event.getY() - widget.bounds.getY();
                    return EventResult.CAPTURE_POINTER;
                }
            }
        }
        return EventResult.IGNORED;
    }

    public List<HudWidgetState> getWidgets() { return Collections.unmodifiableList(widgets); }
}
