package dev.kairos.ui.platform.modern;

import dev.kairos.ui.platform.PlatformHost;

/** Forge and Fabric entry modules can both delegate their screen/input events here. */
public interface Modern120Bindings extends PlatformHost {
    void onGlfwPointer(double x, double y, int button, int action, double scrollY);
    void onGlfwKey(int key, int scanCode, int action, int modifiers);
    void restoreModernRenderState();
}
