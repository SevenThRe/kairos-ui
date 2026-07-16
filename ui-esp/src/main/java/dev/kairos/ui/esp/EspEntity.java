package dev.kairos.ui.esp;

public final class EspEntity {
    private final String id;
    private final String displayName;
    private final WorldBounds bounds;
    private final float health;
    private final float maxHealth;
    private final float distance;
    private final boolean friend;
    private final boolean invisible;

    public EspEntity(String id, String displayName, WorldBounds bounds, float health, float maxHealth,
                     float distance, boolean friend, boolean invisible) {
        if (id == null || displayName == null || bounds == null) throw new IllegalArgumentException("entity");
        this.id = id;
        this.displayName = displayName;
        this.bounds = bounds;
        this.health = health;
        this.maxHealth = maxHealth;
        this.distance = distance;
        this.friend = friend;
        this.invisible = invisible;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public WorldBounds getBounds() { return bounds; }
    public float getHealth() { return health; }
    public float getMaxHealth() { return maxHealth; }
    public float getDistance() { return distance; }
    public boolean isFriend() { return friend; }
    public boolean isInvisible() { return invisible; }
}
