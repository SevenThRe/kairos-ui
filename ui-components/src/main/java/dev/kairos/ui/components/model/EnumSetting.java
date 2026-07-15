package dev.kairos.ui.components.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EnumSetting extends UiSetting<String> {
    private final List<String> options;

    public EnumSetting(String id, String name, ValueAccessor<String> value, List<String> options) {
        super(id, name, value);
        if (options == null || options.isEmpty()) throw new IllegalArgumentException("Enum options are required");
        this.options = Collections.unmodifiableList(new ArrayList<String>(options));
        validate(value.get());
    }

    @Override protected void validate(String next) {
        if (next == null || !options.contains(next)) throw new IllegalArgumentException("Unknown enum option: " + next);
    }

    public List<String> getOptions() { return options; }
}
