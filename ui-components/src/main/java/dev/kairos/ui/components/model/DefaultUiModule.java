package dev.kairos.ui.components.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DefaultUiModule implements UiModule {
    private final String id;
    private final String name;
    private final String description;
    private final UiCategory category;
    private final ValueAccessor<Boolean> enabled;
    private final List<UiSetting<?>> settings;

    public DefaultUiModule(String id, String name, String description, UiCategory category,
                           ValueAccessor<Boolean> enabled, List<UiSetting<?>> settings) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = enabled;
        this.settings = Collections.unmodifiableList(new ArrayList<UiSetting<?>>(settings));
    }

    @Override public String getId() { return id; }
    @Override public String getDisplayName() { return name; }
    @Override public String getDescription() { return description; }
    @Override public UiCategory getCategory() { return category; }
    @Override public boolean isEnabled() { return enabled.get(); }
    @Override public void setEnabled(boolean next) { enabled.set(next); }
    @Override public List<UiSetting<?>> getSettings() { return settings; }
}
