package dev.kairos.ui.components.hud;

/** Version-neutral item reference. The host resolves visualId to the native ItemStack. */
public final class EquipmentVisual {
    private final String visualId;
    private final String label;
    private final int durabilityPercent;

    public EquipmentVisual(String visualId, String label, int durabilityPercent) {
        this.visualId = visualId == null ? "" : visualId;
        this.label = label == null ? "" : label;
        this.durabilityPercent = Math.max(0, Math.min(100, durabilityPercent));
    }

    public String getVisualId() { return visualId; }
    public String getLabel() { return label; }
    public int getDurabilityPercent() { return durabilityPercent; }
}
