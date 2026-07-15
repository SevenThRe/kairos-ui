package dev.kairos.ui.components.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class ModuleCatalog {
    private final List<UiCategory> categories;
    private final List<UiModule> modules;

    public ModuleCatalog(List<UiCategory> categories, List<UiModule> modules) {
        this.categories = Collections.unmodifiableList(new ArrayList<UiCategory>(categories));
        this.modules = Collections.unmodifiableList(new ArrayList<UiModule>(modules));
    }

    public List<UiCategory> getCategories() { return categories; }
    public List<UiModule> getModules() { return modules; }

    public List<UiModule> filter(String categoryId, String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<UiModule> result = new ArrayList<UiModule>();
        for (UiModule module : modules) {
            boolean categoryMatches = categoryId == null || categoryId.equals(module.getCategory().getId());
            boolean queryMatches = normalized.isEmpty()
                || module.getDisplayName().toLowerCase(Locale.ROOT).contains(normalized)
                || module.getDescription().toLowerCase(Locale.ROOT).contains(normalized);
            if (categoryMatches && queryMatches) result.add(module);
        }
        return result;
    }
}
