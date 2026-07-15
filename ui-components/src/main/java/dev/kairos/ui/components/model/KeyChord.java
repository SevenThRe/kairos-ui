package dev.kairos.ui.components.model;

import java.util.Objects;

public final class KeyChord {
    public static final KeyChord NONE = new KeyChord(0, 0, "None");
    private final int key;
    private final int modifiers;
    private final String displayName;

    public KeyChord(int key, int modifiers, String displayName) {
        this.key = key;
        this.modifiers = modifiers;
        this.displayName = displayName;
    }

    public int getKey() { return key; }
    public int getModifiers() { return modifiers; }
    public String getDisplayName() { return displayName; }
    @Override public String toString() { return displayName; }
    @Override public boolean equals(Object value) {
        if (this == value) return true;
        if (!(value instanceof KeyChord)) return false;
        KeyChord other = (KeyChord) value;
        return key == other.key && modifiers == other.modifiers;
    }
    @Override public int hashCode() { return Objects.hash(key, modifiers); }
}
