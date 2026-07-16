package dev.kairos.ui.components.hud;

/** A rendered module-list line. Suffixes stay separate so they can use a quieter color. */
public final class HudModuleEntry {
    private final String name;
    private final String suffix;
    private final int accentArgb;

    public HudModuleEntry(String name, String suffix, int accentArgb) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("name");
        this.name = name;
        this.suffix = suffix == null ? "" : suffix;
        this.accentArgb = accentArgb;
    }

    public HudModuleEntry(String name) {
        this(name, "", 0);
    }

    public String getName() { return name; }
    public String getSuffix() { return suffix; }
    public int getAccentArgb() { return accentArgb; }
    public String getDisplayText() { return suffix.isEmpty() ? name : name + " " + suffix; }
}
