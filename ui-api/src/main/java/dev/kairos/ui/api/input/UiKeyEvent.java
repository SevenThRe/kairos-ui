package dev.kairos.ui.api.input;

public final class UiKeyEvent {
    public static final int KEY_BACKSPACE = 8;
    public static final int KEY_ESCAPE = 27;
    public static final int KEY_ENTER = 13;

    private final int logicalKey;
    private final int scanCode;
    private final int modifiers;
    private final char typedCharacter;
    private final KeyAction action;

    public UiKeyEvent(int logicalKey, int scanCode, int modifiers, char typedCharacter, KeyAction action) {
        this.logicalKey = logicalKey;
        this.scanCode = scanCode;
        this.modifiers = modifiers;
        this.typedCharacter = typedCharacter;
        this.action = action;
    }

    public int getLogicalKey() { return logicalKey; }
    public int getScanCode() { return scanCode; }
    public int getModifiers() { return modifiers; }
    public char getTypedCharacter() { return typedCharacter; }
    public KeyAction getAction() { return action; }
}
