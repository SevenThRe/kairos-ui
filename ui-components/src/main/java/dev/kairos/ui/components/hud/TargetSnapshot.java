package dev.kairos.ui.components.hud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Complete immutable target state captured on the game thread. */
public final class TargetSnapshot {
    private final String entityVisualId;
    private final String name;
    private final float health;
    private final float maxHealth;
    private final float previousHealth;
    private final float absorption;
    private final float distance;
    private final boolean friend;
    private final int hurtTicks;
    private final EquipmentVisual heldItem;
    private final List<EquipmentVisual> armor;

    public TargetSnapshot(String entityVisualId, String name, float health, float maxHealth,
                          float previousHealth, float absorption, float distance, boolean friend,
                          int hurtTicks, EquipmentVisual heldItem, List<EquipmentVisual> armor) {
        this.entityVisualId = entityVisualId == null ? "" : entityVisualId;
        this.name = name == null ? "Unknown" : name;
        this.health = health;
        this.maxHealth = Math.max(0.001f, maxHealth);
        this.previousHealth = previousHealth;
        this.absorption = Math.max(0f, absorption);
        this.distance = Math.max(0f, distance);
        this.friend = friend;
        this.hurtTicks = Math.max(0, hurtTicks);
        this.heldItem = heldItem == null ? new EquipmentVisual("", "", 0) : heldItem;
        this.armor = Collections.unmodifiableList(new ArrayList<EquipmentVisual>(armor == null
            ? Collections.<EquipmentVisual>emptyList() : armor));
    }

    public String getEntityVisualId() { return entityVisualId; }
    public String getName() { return name; }
    public float getHealth() { return health; }
    public float getMaxHealth() { return maxHealth; }
    public float getPreviousHealth() { return previousHealth; }
    public float getAbsorption() { return absorption; }
    public float getDistance() { return distance; }
    public boolean isFriend() { return friend; }
    public int getHurtTicks() { return hurtTicks; }
    public EquipmentVisual getHeldItem() { return heldItem; }
    public List<EquipmentVisual> getArmor() { return armor; }
    public float healthRatio() { return Math.max(0f, Math.min(1f, health / maxHealth)); }
    public float healthDelta() { return health - previousHealth; }
}
