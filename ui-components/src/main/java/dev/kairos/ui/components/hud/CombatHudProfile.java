package dev.kairos.ui.components.hud;

/** Independent HUD skin. ClickGUI and combat overlays intentionally do not share surface geometry. */
public final class CombatHudProfile {
    public final String font;
    public final int panel;
    public final int border;
    public final int text;
    public final int muted;
    public final int enemy;
    public final int friend;
    public final int health;
    public final int damage;
    public final boolean pixelSnap;

    public CombatHudProfile(String font, int panel, int border, int text, int muted,
                            int enemy, int friend, int health, int damage, boolean pixelSnap) {
        this.font = font;
        this.panel = panel;
        this.border = border;
        this.text = text;
        this.muted = muted;
        this.enemy = enemy;
        this.friend = friend;
        this.health = health;
        this.damage = damage;
        this.pixelSnap = pixelSnap;
    }

    public static CombatHudProfile competitivePixel() {
        return new CombatHudProfile("jetbrains-mono", 0xD5141518, 0xE8E9EDF2,
            0xFFF4F4F4, 0xFFC1C5CA, 0xFFFF535A, 0xFF56E18A, 0xFF52E57D,
            0xFFFFC447, true);
    }
}
