package dev.kairos.ui.components.model;

public interface ValueAccessor<T> {
    T get();
    void set(T value);
}
