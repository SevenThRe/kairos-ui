package dev.kairos.ui.components.scene;

public final class WorkbenchState {
    private String selectedCategoryId;
    private String selectedModuleId;
    private String searchQuery = "";
    private String activeGroup = "General";

    public String getSelectedCategoryId() { return selectedCategoryId; }
    public void setSelectedCategoryId(String id) { selectedCategoryId = id; }
    public String getSelectedModuleId() { return selectedModuleId; }
    public void setSelectedModuleId(String id) { selectedModuleId = id; }
    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String query) { searchQuery = query == null ? "" : query; }
    public String getActiveGroup() { return activeGroup; }
    public void setActiveGroup(String group) { activeGroup = group == null ? "General" : group; }
}
