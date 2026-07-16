package dev.kairos.ui.esp;

public interface WorldToScreenProjector {
    ScreenPoint project(double worldX, double worldY, double worldZ);
}
