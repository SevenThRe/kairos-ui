package dev.kairos.ui.components.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class MultiSelectSetting extends UiSetting<Set<String>> {
    private final List<String> options;

    public MultiSelectSetting(String id, String name, ValueAccessor<Set<String>> value, List<String> options) {
        super(id, name, value);
        if (options == null || options.isEmpty()) throw new IllegalArgumentException("options");
        this.options = Collections.unmodifiableList(new ArrayList<String>(options));
        validate(value.get());
    }

    @Override protected void validate(Set<String> next) {
        if (next == null || !options.containsAll(next)) throw new IllegalArgumentException("Unknown selection");
    }

    @Override public void setValue(Set<String> next) {
        super.setValue(Collections.unmodifiableSet(new LinkedHashSet<String>(next)));
    }

    public List<String> getOptions() { return options; }
    public void toggle(String option) {
        if (!options.contains(option)) throw new IllegalArgumentException("Unknown option: " + option);
        Set<String> next = new LinkedHashSet<String>(getValue());
        if (!next.add(option)) next.remove(option);
        setValue(next);
    }
}
