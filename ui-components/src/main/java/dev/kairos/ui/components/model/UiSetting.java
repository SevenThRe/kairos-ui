package dev.kairos.ui.components.model;

public abstract class UiSetting<T> {
    private final String id;
    private final String displayName;
    private final ValueAccessor<T> value;
    private VisibilityRule visibility = VisibilityRule.ALWAYS;
    private String group = "General";

    protected UiSetting(String id, String displayName, ValueAccessor<T> value) {
        if (id == null || id.isEmpty() || displayName == null || value == null) {
            throw new IllegalArgumentException("Setting id, name and value are required");
        }
        this.id = id;
        this.displayName = displayName;
        this.value = value;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public T getValue() { return value.get(); }
    public void setValue(T next) { validate(next); value.set(next); }
    protected void validate(T next) {}
    public boolean isVisible() { return visibility.isVisible(); }
    public String getGroup() { return group; }
    public UiSetting<T> inGroup(String nextGroup) {
        if (nextGroup == null || nextGroup.trim().isEmpty()) throw new IllegalArgumentException("group");
        group = nextGroup;
        return this;
    }
    public UiSetting<T> visibleWhen(VisibilityRule rule) {
        if (rule == null) throw new IllegalArgumentException("rule");
        visibility = rule;
        return this;
    }
}
