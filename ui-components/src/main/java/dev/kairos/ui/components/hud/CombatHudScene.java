package dev.kairos.ui.components.hud;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.render.UiCanvas;
import dev.kairos.ui.core.node.UiNode;
import java.util.List;
import java.util.Locale;

/** Dense combat HUD matching the information hierarchy used by competitive clients. */
public final class CombatHudScene extends UiNode {
    private TargetSnapshot target;
    private final CombatHudProfile profile;
    private final GameVisualRenderer visuals;

    public CombatHudScene(TargetSnapshot target, CombatHudProfile profile, GameVisualRenderer visuals) {
        this.target = target;
        this.profile = profile;
        this.visuals = visuals;
    }

    public void setTarget(TargetSnapshot target) { this.target = target; }

    @Override protected void render(UiCanvas canvas) {
        if (target == null) return;
        Rect root = getBounds();
        renderInspector(canvas, new Rect(root.getX() + 24f, root.getY() + 34f, 262f, 154f));
        renderCompactTarget(canvas, new Rect(root.getX() + root.getWidth() * .5f - 126f,
            root.getBottom() - 91f, 252f, 58f));
    }

    private void frame(UiCanvas canvas, Rect r, int accent) {
        canvas.fillRect(new Rect(r.getX() + 3f, r.getY() + 4f, r.getWidth(), r.getHeight()), 0x70000000);
        canvas.fillRect(r, profile.border);
        canvas.fillRect(inset(r, 1f), 0xFF090A0C);
        canvas.fillRect(inset(r, 2f), profile.panel);
        canvas.fillRect(new Rect(r.getX() + 2f, r.getY() + 2f, 3f, r.getHeight() - 4f), accent);
    }

    private void renderInspector(UiCanvas canvas, Rect r) {
        int accent = target.isFriend() ? profile.friend : profile.enemy;
        frame(canvas, r, accent);
        canvas.text(profile.font, target.isFriend() ? "F" : "R", r.getX() + 12f, r.getY() + 21f, 13f, accent);
        canvas.text(profile.font, target.getName(), r.getX() + 29f, r.getY() + 21f, 13f, accent);
        String hp = String.format(Locale.ROOT, "%.1f", target.getHealth());
        canvas.text(profile.font, hp, r.getRight() - 12f - canvas.measureText(profile.font, hp, 13f),
            r.getY() + 21f, 13f, healthColor(target.healthRatio()));

        Rect portrait = new Rect(r.getX() + 12f, r.getY() + 31f, 76f, 103f);
        canvas.fillRect(portrait, 0xFF08090B);
        canvas.fillRect(inset(portrait, 1f), 0xFF23262B);
        visuals.drawEntity(canvas, target.getEntityVisualId(), inset(portrait, 3f), 18f,
            target.getHurtTicks() > 0 ? 0xFFFFA0A0 : 0xFFFFFFFF);

        float healthHeight = 94f * target.healthRatio();
        canvas.fillRect(new Rect(portrait.getRight() + 5f, portrait.getY(), 5f, 94f), 0xFF121417);
        canvas.fillRect(new Rect(portrait.getRight() + 6f, portrait.getY() + 93f - healthHeight,
            3f, healthHeight), healthColor(target.healthRatio()));

        List<EquipmentVisual> armor = target.getArmor();
        float ay = portrait.getY();
        for (int i = 0; i < armor.size() && i < 4; i++) {
            EquipmentVisual item = armor.get(i);
            visuals.drawItem(canvas, item, new Rect(r.getX() + 104f, ay, 18f, 18f), 0xFFFFFFFF);
            canvas.text(profile.font, String.valueOf(item.getDurabilityPercent()), r.getX() + 126f,
                ay + 13f, 8.5f, durabilityColor(item.getDurabilityPercent()));
            ay += 22f;
        }

        canvas.text(profile.font, target.getHeldItem().getLabel(), r.getX() + 162f, portrait.getY() + 12f,
            9f, profile.muted);
        Rect item = new Rect(r.getX() + 162f, portrait.getY() + 20f, 67f, 67f);
        canvas.fillRect(item, 0xFF08090B);
        canvas.fillRect(inset(item, 1f), 0xFF22252A);
        visuals.drawItem(canvas, target.getHeldItem(), inset(item, 5f), 0xFFFFFFFF);
        canvas.text(profile.font, String.format(Locale.ROOT, "%.1fm", target.getDistance()),
            r.getX() + 162f, portrait.getBottom() - 4f, 9f, profile.muted);
    }

    private void renderCompactTarget(UiCanvas canvas, Rect r) {
        int accent = target.isFriend() ? profile.friend : profile.enemy;
        frame(canvas, r, accent);
        Rect slot = new Rect(r.getX() + 7f, r.getY() + 7f, 42f, 42f);
        canvas.fillRect(slot, 0xFFF0F0F0);
        visuals.drawItem(canvas, target.getHeldItem(), inset(slot, 4f), 0xFFFFFFFF);
        canvas.text(profile.font, target.isFriend() ? "F" : "R", r.getX() + 58f, r.getY() + 18f, 11f, accent);
        canvas.text(profile.font, target.getName(), r.getX() + 75f, r.getY() + 18f, 11f, accent);
        canvas.text(profile.font, target.getArmor().isEmpty() ? "" : "▮", r.getRight() - 15f,
            r.getY() + 18f, 11f, profile.friend);
        String hp = String.format(Locale.ROOT, "%.1f♥", target.getHealth());
        canvas.text(profile.font, hp, r.getX() + 58f, r.getY() + 37f, 12f, profile.text);
        String delta = String.format(Locale.ROOT, "%+.1f", target.healthDelta());
        int deltaColor = target.healthDelta() >= 0f ? profile.health : profile.damage;
        canvas.text(profile.font, delta, r.getRight() - 12f - canvas.measureText(profile.font, delta, 11f),
            r.getY() + 36f, 11f, deltaColor);
        float barX = r.getX() + 57f;
        float barW = r.getWidth() - 66f;
        canvas.fillRect(new Rect(barX, r.getBottom() - 10f, barW, 5f), 0xFF3A1518);
        canvas.fillRect(new Rect(barX, r.getBottom() - 10f, barW * target.healthRatio(), 5f),
            healthColor(target.healthRatio()));
        for (int i = 1; i < 10; i++) canvas.fillRect(new Rect(barX + barW * i / 10f, r.getBottom() - 10f, 1f, 5f), 0x80000000);
    }

    private int healthColor(float ratio) {
        int red = Math.round(240f * (1f - ratio));
        int green = Math.round(225f * ratio + 25f);
        return 0xFF000000 | (red << 16) | (Math.min(255, green) << 8) | 0x42;
    }

    private int durabilityColor(int percent) {
        return percent < 30 ? profile.enemy : percent < 60 ? profile.damage : profile.text;
    }

    private static Rect inset(Rect r, float amount) {
        return new Rect(r.getX() + amount, r.getY() + amount,
            Math.max(0f, r.getWidth() - amount * 2f), Math.max(0f, r.getHeight() - amount * 2f));
    }
}
