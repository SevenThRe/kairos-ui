package dev.kairos.ui.components.hud;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.input.EventResult;
import dev.kairos.ui.api.input.PointerAction;
import dev.kairos.ui.api.input.PointerEvent;
import dev.kairos.ui.api.render.UiCanvas;
import dev.kairos.ui.api.theme.ThemeTokens;
import dev.kairos.ui.core.node.UiNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Shared HUD renderer and editor state; live game values are supplied through HudModel. */
public final class HudScene extends UiNode {
    private final HudModel model;
    private final ThemeTokens theme;
    private final List<HudWidgetState> widgets = new ArrayList<HudWidgetState>();
    private HudWidgetState dragging;
    private float offsetX;
    private float offsetY;

    public HudScene(HudModel model, ThemeTokens theme) {
        this.model = model;
        this.theme = theme;
        widgets.add(new HudWidgetState("watermark", new Rect(24f, 22f, 190f, 58f)));
        widgets.add(new HudWidgetState("module-list", new Rect(1052f, 92f, 200f, 164f)));
        widgets.add(new HudWidgetState("session", new Rect(24f, 548f, 210f, 142f)));
        widgets.add(new HudWidgetState("target", new Rect(480f, 570f, 320f, 120f)));
        widgets.add(new HudWidgetState("notifications", new Rect(1010f, 530f, 242f, 160f)));
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
        panel(canvas, r);
        canvas.text(theme.fontSemibold, "Modules", r.getX() + 14f, r.getY() + 22f, 11f, theme.textPrimary);
        float y = r.getY() + 44f;
        for (String module : model.getEnabledModules()) {
            canvas.roundedRect(new Rect(r.getX() + 14f, y - 7f, 5f, 5f), 3f, theme.accent);
            canvas.text(theme.fontRegular, module, r.getX() + 27f, y, 10f, theme.textPrimary);
            y += 22f;
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
        panel(canvas, r);
        canvas.text(theme.fontSemibold, "Notifications", r.getX() + 14f, r.getY() + 22f, 11f, theme.textPrimary);
        float y = r.getY() + 38f;
        for (String notification : model.getNotifications()) {
            canvas.roundedRect(new Rect(r.getX() + 12f, y, r.getWidth() - 24f, 46f), 7f, theme.surface);
            canvas.roundedRect(new Rect(r.getX() + 22f, y + 14f, 18f, 18f), 9f, 0xFF299B69);
            canvas.text(theme.fontRegular, notification, r.getX() + 50f, y + 27f, 10f, theme.textPrimary);
            y += 54f;
        }
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
