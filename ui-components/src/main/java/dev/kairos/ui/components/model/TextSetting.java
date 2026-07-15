package dev.kairos.ui.components.model;

public final class TextSetting extends UiSetting<String> {
    private final int maxLength;
    public TextSetting(String id, String name, ValueAccessor<String> value, int maxLength) {
        super(id, name, value);
        if (maxLength < 1) throw new IllegalArgumentException("maxLength");
        this.maxLength = maxLength;
        validate(value.get());
    }
    @Override protected void validate(String next) {
        if (next == null || next.length() > maxLength) throw new IllegalArgumentException("Invalid text value");
    }
    public int getMaxLength() { return maxLength; }
}
