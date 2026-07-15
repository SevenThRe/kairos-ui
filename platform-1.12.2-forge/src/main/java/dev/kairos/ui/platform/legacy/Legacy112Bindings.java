package dev.kairos.ui.platform.legacy;

import dev.kairos.ui.platform.PlatformHost;

/**
 * Contract to be implemented inside the real Forge 1.12.2 workspace.
 * Keeping this interface Minecraft-free lets the architecture build before mappings are chosen.
 */
public interface Legacy112Bindings extends PlatformHost {
    void onLegacyMouse(int rawX, int rawY, int button, boolean pressed, int wheelDelta);
    void onLegacyKey(int lwjglKey, char typedCharacter, boolean pressed);
    void restoreLegacyGlState();
}
