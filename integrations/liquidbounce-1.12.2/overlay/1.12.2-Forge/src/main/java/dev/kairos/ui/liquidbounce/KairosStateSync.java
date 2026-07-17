package dev.kairos.ui.liquidbounce;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.kairos.ui.web.WebSurface;
import java.util.HashMap;
import java.util.Map;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.value.Value;

/** Revisioned Java-to-web state channel for keybinds and non-GUI mutations. */
final class KairosStateSync {
    private static final long MODULE_SCAN_NS = 50_000_000L;
    private static final long VALUE_SCAN_NS = 250_000_000L;

    private final Map<String, Boolean> moduleStates = new HashMap<String, Boolean>();
    private long lastModuleScan;
    private long lastValueScan;
    private long revision;
    private int valueFingerprint;
    private boolean initialized;

    void tick(WebSurface surface) {
        long now = System.nanoTime();
        if (now - lastModuleScan >= MODULE_SCAN_NS) {
            lastModuleScan = now;
            pushModuleChanges(surface);
        }
        if (now - lastValueScan >= VALUE_SCAN_NS) {
            lastValueScan = now;
            detectValueChanges(surface);
        }
    }

    private void pushModuleChanges(WebSurface surface) {
        JsonArray changes = new JsonArray();
        for (Module module : LiquidBounce.INSTANCE.getModuleManager().getModules()) {
            Boolean previous = moduleStates.put(module.getName(), module.getState());
            if (initialized && previous != null && previous.booleanValue() != module.getState()) {
                JsonObject change = new JsonObject();
                change.addProperty("id", module.getName());
                change.addProperty("enabled", module.getState());
                changes.add(change);
            }
        }
        if (!initialized) {
            initialized = true;
            valueFingerprint = valueFingerprint();
            return;
        }
        if (changes.size() == 0) return;

        JsonObject patch = new JsonObject();
        patch.addProperty("revision", ++revision);
        patch.add("modules", changes);
        surface.executeJavaScript("window.KairosRuntime&&window.KairosRuntime.applyPatch(" + patch + ");");
    }

    private void detectValueChanges(WebSurface surface) {
        if (!initialized) return;
        int current = valueFingerprint();
        if (current == valueFingerprint) return;
        valueFingerprint = current;
        surface.executeJavaScript("window.KairosRuntime&&window.KairosRuntime.requestRefresh();");
    }

    private int valueFingerprint() {
        int hash = 1;
        for (Module module : LiquidBounce.INSTANCE.getModuleManager().getModules()) {
            hash = 31 * hash + module.getName().hashCode();
            for (Value<?> value : module.getValues()) {
                Object current = value.get();
                hash = 31 * hash + value.getName().hashCode();
                hash = 31 * hash + (current == null ? 0 : current.hashCode());
            }
        }
        return hash;
    }
}
