package dev.kairos.ui.components.model;

public final class RangeSetting extends UiSetting<RangeValue> {
    private final double min;
    private final double max;
    private final double step;
    public RangeSetting(String id, String name, ValueAccessor<RangeValue> value,
                        double min, double max, double step) {
        super(id, name, value);
        if (max < min || step <= 0d) throw new IllegalArgumentException("Invalid range bounds");
        this.min = min;
        this.max = max;
        this.step = step;
        validate(value.get());
    }
    @Override protected void validate(RangeValue next) {
        if (next == null || next.low < min || next.high > max) throw new IllegalArgumentException("Range outside bounds");
    }
    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getStep() { return step; }
}
