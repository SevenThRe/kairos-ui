package dev.kairos.ui.components.model;

public final class MutableValue<T> implements ValueAccessor<T> {
    private T value;
    public MutableValue(T value) { this.value = value; }
    @Override public T get() { return value; }
    @Override public void set(T value) { this.value = value; }
}
