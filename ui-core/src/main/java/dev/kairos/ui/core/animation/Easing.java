package dev.kairos.ui.core.animation;

public interface Easing {
    float apply(float progress);

    Easing LINEAR = new Easing() {
        @Override public float apply(float progress) { return progress; }
    };

    Easing EASE_OUT_CUBIC = new Easing() {
        @Override public float apply(float progress) {
            float inverse = 1f - progress;
            return 1f - inverse * inverse * inverse;
        }
    };
}
