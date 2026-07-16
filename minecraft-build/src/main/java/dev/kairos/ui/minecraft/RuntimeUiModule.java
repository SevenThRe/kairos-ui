package dev.kairos.ui.minecraft;

import dev.kairos.ui.components.model.UiCategory;
import dev.kairos.ui.components.model.UiModule;
import dev.kairos.ui.components.model.UiSetting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A UI module backed by the live client runtime instead of a preview-only boolean. */
final class RuntimeUiModule implements UiModule {
    interface StateListener { void onStateChanged(RuntimeUiModule module, boolean enabled); }

    private final String id;
    private final String name;
    private final String description;
    private final UiCategory category;
    private final List<UiSetting<?>> settings;
    private final StateListener listener;
    private boolean enabled;

    RuntimeUiModule(String id, String name, String description, UiCategory category,
                    boolean enabled, List<UiSetting<?>> settings, StateListener listener) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = enabled;
        this.settings = Collections.unmodifiableList(new ArrayList<UiSetting<?>>(settings));
        this.listener = listener;
    }

    @Override public String getId() { return id; }
    @Override public String getDisplayName() { return name; }
    @Override public String getDescription() { return description; }
    @Override public UiCategory getCategory() { return category; }
    @Override public boolean isEnabled() { return enabled; }
    @Override public List<UiSetting<?>> getSettings() { return settings; }

    @Override public void setEnabled(boolean next) {
        if (enabled == next) return;
        enabled = next;
        listener.onStateChanged(this, next);
    }

    void loadEnabled(boolean next) { enabled = next; }
}
