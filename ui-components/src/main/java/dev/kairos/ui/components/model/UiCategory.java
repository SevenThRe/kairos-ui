package dev.kairos.ui.components.model;

public final class UiCategory {
    private final String id;
    private final String displayName;
    private final String iconId;

    public UiCategory(String id, String displayName, String iconId) {
        this.id = id;
        this.displayName = displayName;
        this.iconId = iconId;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getIconId() { return iconId; }
}
