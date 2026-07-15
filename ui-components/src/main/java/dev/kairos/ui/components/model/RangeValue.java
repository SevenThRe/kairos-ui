package dev.kairos.ui.components.model;

public final class RangeValue {
    public final double low;
    public final double high;
    public RangeValue(double low, double high) {
        if (high < low) throw new IllegalArgumentException("high < low");
        this.low = low;
        this.high = high;
    }
    @Override public String toString() { return low + " – " + high; }
}
