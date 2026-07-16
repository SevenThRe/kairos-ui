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
        renderInspector(canvas, new Rect(root.getX() + 28f, root.getY() + 34f, 310f, 178f));
        renderCompactTarget(canvas, new Rect(root.getX() + root.getWidth() * .5f - 154f,
            root.getBottom() - 105f, 308f, 72f));
    }

    private void frame(UiCanvas canvas, Rect r, int accent) {
        canvas.fillRect(new Rect(r.getX() + 4f, r.getY() + 5f, r.getWidth(), r.getHeight()), 0x8A000000);
        canvas.fillRect(r, 0xF006080B);
        canvas.fillRect(inset(r, 1f), profile.border);
        canvas.fillRect(inset(r, 2f), profile.panel);
        canvas.fillRect(new Rect(r.getX() + 2f, r.getY() + 2f, 2f, r.getHeight() - 4f), accent);
        canvas.fillRect(new Rect(r.getX() + 4f, r.getY() + 2f, r.getWidth() - 6f, 1f), 0x1FFFFFFF);
    }

    private void renderInspector(UiCanvas canvas, Rect r) {
        int accent = target.isFriend() ? profile.friend : profile.enemy;
        frame(canvas, r, accent);
        canvas.text(profile.font, target.isFriend() ? "F" : "R", r.getX() + 14f, r.getY() + 24f, 14f, accent);
        canvas.text(profile.font, target.getName(), r.getX() + 34f, r.getY() + 24f, 14f, profile.text);
        String hp = String.format(Locale.ROOT, "%.1f", target.getHealth());
        canvas.text(profile.font, hp, r.getRight() - 14f - canvas.measureText(profile.font, hp, 14f),
            r.getY() + 24f, 14f, healthColor(target.healthRatio()));

        Rect portrait = new Rect(r.getX() + 14f, r.getY() + 37f, 88f, 123f);
        canvas.fillRect(portrait, 0xFF08090B);
        canvas.fillRect(inset(portrait, 1f), 0xFF20252B);
        canvas.fillRect(new Rect(portrait.getX() + 1f, portrait.getBottom() - 27f,
            portrait.getWidth() - 2f, 26f), 0xFF171B20);
        visuals.drawEntity(canvas, target.getEntityVisualId(), inset(portrait, 4f), 18f,
            target.getHurtTicks() > 0 ? 0xFFFFA0A0 : 0xFFFFFFFF);

        float healthHeight = 113f * target.healthRatio();
        canvas.fillRect(new Rect(portrait.getRight() + 6f, portrait.getY(), 6f, 115f), 0xFF080A0C);
        canvas.fillRect(new Rect(portrait.getRight() + 8f, portrait.getY() + 113f - healthHeight,
            3f, healthHeight), healthColor(target.healthRatio()));

        List<EquipmentVisual> armor = target.getArmor();
        float ay = portrait.getY() + 1f;
        for (int i = 0; i < armor.size() && i < 4; i++) {
            EquipmentVisual item = armor.get(i);
            Rect armorSlot = new Rect(r.getX() + 123f, ay, 24f, 24f);
            canvas.fillRect(armorSlot, 0xB9080A0D);
            visuals.drawItem(canvas, item, inset(armorSlot, 3f), 0xFFFFFFFF);
            canvas.text(profile.font, String.valueOf(item.getDurabilityPercent()), r.getX() + 153f,
                ay + 16f, 9.5f, durabilityColor(item.getDurabilityPercent()));
            ay += 29f;
        }

        float itemX = r.getX() + 194f;
        canvas.text(profile.font, target.getHeldItem().getLabel(), itemX, portrait.getY() + 14f,
            9.5f, profile.muted);
        Rect item = new Rect(itemX, portrait.getY() + 24f, 78f, 78f);
        canvas.fillRect(item, 0xFF08090B);
        canvas.fillRect(inset(item, 1f), 0xFF20252B);
        visuals.drawItem(canvas, target.getHeldItem(), inset(item, 8f), 0xFFFFFFFF);
        canvas.text(profile.font, String.format(Locale.ROOT, "%.1fm", target.getDistance()),
            itemX, portrait.getBottom() - 5f, 10f, profile.muted);
    }

    private void renderCompactTarget(UiCanvas canvas, Rect r) {
        int accent = target.isFriend() ? profile.friend : profile.enemy;
        frame(canvas, r, accent);
        Rect slot = new Rect(r.getX() + 9f, r.getY() + 9f, 52f, 52f);
        canvas.fillRect(slot, 0xFFE8EBEF);
        canvas.fillRect(inset(slot, 2f), 0xFFF8F9FA);
        visuals.drawItem(canvas, target.getHeldItem(), inset(slot, 7f), 0xFFFFFFFF);
        canvas.text(profile.font, target.isFriend() ? "F" : "R", r.getX() + 73f, r.getY() + 23f, 12f, accent);
        canvas.text(profile.font, target.getName(), r.getX() + 91f, r.getY() + 23f, 12f, profile.text);
        canvas.text(profile.font, target.getArmor().isEmpty() ? "" : "▮", r.getRight() - 15f,
            r.getY() + 23f, 11f, profile.friend);
        String hp = String.format(Locale.ROOT, "%.1f♥", target.getHealth());
        canvas.text(profile.font, hp, r.getX() + 73f, r.getY() + 45f, 13f, profile.text);
        String delta = String.format(Locale.ROOT, "%+.1f", target.healthDelta());
        int deltaColor = target.healthDelta() >= 0f ? profile.health : profile.damage;
        canvas.text(profile.font, delta, r.getRight() - 14f - canvas.measureText(profile.font, delta, 12f),
            r.getY() + 45f, 12f, deltaColor);
        float barX = r.getX() + 72f;
        float barW = r.getWidth() - 84f;
        canvas.fillRect(new Rect(barX, r.getBottom() - 12f, barW, 7f), 0xFF351619);
        canvas.fillRect(new Rect(barX, r.getBottom() - 12f, barW * target.healthRatio(), 7f),
            healthColor(target.healthRatio()));
        for (int i = 1; i < 10; i++) canvas.fillRect(new Rect(barX + barW * i / 10f, r.getBottom() - 12f, 1f, 7f), 0x90000000);
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
