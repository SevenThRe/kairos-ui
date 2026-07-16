package dev.kairos.ui.api.theme;

public final class ThemePack {
    private final String id;
    private final String displayName;
    private final ThemeTokens tokens;

    public ThemePack(String id, String displayName, ThemeTokens tokens) {
        if (id == null || !id.matches("[a-z0-9][a-z0-9._-]*")) throw new IllegalArgumentException("id");
        if (displayName == null || displayName.trim().isEmpty()) throw new IllegalArgumentException("displayName");
        if (tokens == null) throw new IllegalArgumentException("tokens");
        this.id = id;
        this.displayName = displayName;
        this.tokens = tokens;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public ThemeTokens getTokens() { return tokens; }
}
