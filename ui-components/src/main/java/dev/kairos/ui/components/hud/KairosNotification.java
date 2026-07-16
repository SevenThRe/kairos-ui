package dev.kairos.ui.components.hud;

public final class KairosNotification {
    private static final long ENTER_MS = 180L;
    private static final long EXIT_MS = 220L;
    private final long id;
    private final String title;
    private final String message;
    private final NotificationKind kind;
    private final long createdAtMillis;
    private final long durationMillis;

    KairosNotification(long id, String title, String message, NotificationKind kind,
                       long createdAtMillis, long durationMillis) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.kind = kind;
        this.createdAtMillis = createdAtMillis;
        this.durationMillis = durationMillis;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public NotificationKind getKind() { return kind; }
    public long getCreatedAtMillis() { return createdAtMillis; }
    public long getDurationMillis() { return durationMillis; }

    public boolean isExpired(long nowMillis) {
        return nowMillis >= createdAtMillis + durationMillis;
    }

    public float getRemainingFraction(long nowMillis) {
        return clamp(1f - (nowMillis - createdAtMillis) / (float) durationMillis);
    }

    public float getVisibility(long nowMillis) {
        long age = nowMillis - createdAtMillis;
        if (age < 0L || isExpired(nowMillis)) return 0f;
        if (age < ENTER_MS) return smooth(age / (float) ENTER_MS);
        long remaining = createdAtMillis + durationMillis - nowMillis;
        if (remaining < EXIT_MS) return smooth(remaining / (float) EXIT_MS);
        return 1f;
    }

    private static float smooth(float value) {
        float t = clamp(value);
        return t * t * (3f - 2f * t);
    }

    private static float clamp(float value) { return Math.max(0f, Math.min(1f, value)); }
}
