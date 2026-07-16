package dev.kairos.ui.components;

import dev.kairos.ui.components.model.EnumSetting;
import dev.kairos.ui.components.model.ModuleCatalog;
import dev.kairos.ui.components.model.UiModule;
import dev.kairos.ui.components.model.UiSetting;
import dev.kairos.ui.example.DemoCatalog;

public final class ComponentSmokeTest {
    public static void main(String[] args) {
        ModuleCatalog catalog = DemoCatalog.create();
        UiModule killAura = catalog.getModules().get(0);
        int defaultVisible = visibleSettings(killAura);
        ((EnumSetting) killAura.getSettings().get(0)).setValue("Hypixel");
        require(visibleSettings(killAura) == defaultVisible + 1, "mode-specific setting becomes visible");

        boolean wasEnabled = killAura.isEnabled();
        killAura.setEnabled(!wasEnabled);
        require(killAura.isEnabled() != wasEnabled, "left click toggles module without a pill control");
        require(catalog.filter("combat", "velocity").size() == 1, "catalog filtering remains available to Web hosts");
        System.out.println("ComponentSmokeTest passed");
    }

    private static int visibleSettings(UiModule module) {
        int count = 0;
        for (UiSetting<?> setting : module.getSettings()) if (setting.isVisible()) count++;
        return count;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
