package dev.kairos.ui.components.model;

import java.util.List;

public interface UiModule {
    String getId();
    String getDisplayName();
    String getDescription();
    UiCategory getCategory();
    boolean isEnabled();
    void setEnabled(boolean enabled);
    List<UiSetting<?>> getSettings();
}
