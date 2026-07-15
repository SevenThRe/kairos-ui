package dev.kairos.ui.core.animation;

public final class AnimatedFloat {
    private float value;
    private float start;
    private float target;
    private long startNanos;
    private long durationNanos;
    private Easing easing = Easing.EASE_OUT_CUBIC;

    public AnimatedFloat(float initialValue) {
        value = initialValue;
        start = initialValue;
        target = initialValue;
    }

    public void animateTo(float nextTarget, long durationMs, long nowNanos) {
        update(nowNanos);
        if (Float.compare(nextTarget, target) == 0) return;
        start = value;
        target = nextTarget;
        startNanos = nowNanos;
        durationNanos = Math.max(1L, durationMs) * 1_000_000L;
    }

    public float update(long nowNanos) {
        if (Float.compare(value, target) == 0) return value;
        float progress = Math.min(1f, Math.max(0f, (float) (nowNanos - startNanos) / durationNanos));
        value = start + (target - start) * easing.apply(progress);
        if (progress >= 1f) value = target;
        return value;
    }

    public float getValue() { return value; }
    public float getTarget() { return target; }
    public void setEasing(Easing easing) {
        if (easing == null) throw new IllegalArgumentException("easing");
        this.easing = easing;
    }
}
