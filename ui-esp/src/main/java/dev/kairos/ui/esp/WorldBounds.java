package dev.kairos.ui.esp;

public final class WorldBounds {
    public final double minX;
    public final double minY;
    public final double minZ;
    public final double maxX;
    public final double maxY;
    public final double maxZ;

    public WorldBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if (maxX < minX || maxY < minY || maxZ < minZ) throw new IllegalArgumentException("bounds");
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }
}
