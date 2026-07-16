package dev.kairos.ui.esp;

/** Block/item/object highlight shared by 2D preview and native 3D world pass. */
public final class WorldObjectEsp {
    public enum Kind { BED, CHEST, ITEM, PROJECTILE, CUSTOM }
    private final String id;
    private final String label;
    private final Kind kind;
    private final WorldBounds bounds;
    private final int color;

    public WorldObjectEsp(String id, String label, Kind kind, WorldBounds bounds, int color) {
        if (id == null || kind == null || bounds == null) throw new IllegalArgumentException("world object");
        this.id = id;
        this.label = label == null ? "" : label;
        this.kind = kind;
        this.bounds = bounds;
        this.color = color;
    }
    public String getId() { return id; }
    public String getLabel() { return label; }
    public Kind getKind() { return kind; }
    public WorldBounds getBounds() { return bounds; }
    public int getColor() { return color; }
}
