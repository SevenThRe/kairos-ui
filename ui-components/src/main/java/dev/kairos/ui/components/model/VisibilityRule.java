package dev.kairos.ui.components.model;

public interface VisibilityRule {
    boolean isVisible();
    VisibilityRule ALWAYS = new VisibilityRule() {
        @Override public boolean isVisible() { return true; }
    };
}
