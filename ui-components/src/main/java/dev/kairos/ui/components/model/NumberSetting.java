package dev.kairos.ui.components.model;

public final class NumberSetting extends UiSetting<Double> {
    private final double min;
    private final double max;
    private final double step;

    public NumberSetting(String id, String name, ValueAccessor<Double> value, double min, double max, double step) {
        super(id, name, value);
        if (max < min || step <= 0d) throw new IllegalArgumentException("Invalid numeric range");
        this.min = min;
        this.max = max;
        this.step = step;
        validate(value.get());
    }

    @Override protected void validate(Double next) {
        if (next == null || next < min || next > max) throw new IllegalArgumentException("Number outside setting range");
    }

    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getStep() { return step; }
}
