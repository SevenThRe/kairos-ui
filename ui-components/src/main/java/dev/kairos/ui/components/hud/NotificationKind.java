package dev.kairos.ui.components.hud;

public enum NotificationKind {
    INFO(0xFF5D8FF5),
    SUCCESS(0xFF3BC990),
    WARNING(0xFFF2B84B),
    ERROR(0xFFF06A73);

    private final int accentArgb;
    NotificationKind(int accentArgb) { this.accentArgb = accentArgb; }
    public int getAccentArgb() { return accentArgb; }
}
