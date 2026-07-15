package dev.kairos.ui.example;

import dev.kairos.ui.components.model.*;
import java.util.Arrays;
import java.util.Collections;

public final class DemoCatalog {
    private DemoCatalog() {}

    public static ModuleCatalog create() {
        UiCategory combat = new UiCategory("combat", "Combat", "swords");
        UiCategory movement = new UiCategory("movement", "Movement", "movement");
        final MutableValue<String> mode = new MutableValue<String>("Switch");

        EnumSetting modeSetting = new EnumSetting("mode", "Mode", mode,
            Arrays.asList("Switch", "Vanilla", "Hypixel"));
        NumberSetting range = new NumberSetting("range", "Range",
            new MutableValue<Double>(3.4d), 1d, 6d, 0.1d);
        NumberSetting boostDelay = new NumberSetting("boost-delay", "Boost Delay",
            new MutableValue<Double>(755d), 0d, 2000d, 5d);
        boostDelay.visibleWhen(new VisibilityRule() {
            @Override public boolean isVisible() { return "Hypixel".equals(mode.get()); }
        });
        BooleanSetting autoBlock = new BooleanSetting("auto-block", "Auto Block",
            new MutableValue<Boolean>(true));

        UiModule killAura = new DefaultUiModule("kill-aura", "KillAura", "Advanced combat module",
            combat, new MutableValue<Boolean>(true), Arrays.<UiSetting<?>>asList(modeSetting, range, boostDelay, autoBlock));
        UiModule velocity = new DefaultUiModule("velocity", "Velocity", "Modify knockback",
            combat, new MutableValue<Boolean>(true), Collections.<UiSetting<?>>emptyList());
        UiModule sprint = new DefaultUiModule("sprint", "Sprint", "Automatic sprinting",
            movement, new MutableValue<Boolean>(true), Collections.<UiSetting<?>>emptyList());

        return new ModuleCatalog(Arrays.asList(combat, movement), Arrays.asList(killAura, velocity, sprint));
    }
}
