package dev.kairos.ui.minecraft;

import dev.kairos.ui.components.model.BooleanSetting;
import dev.kairos.ui.components.model.DefaultUiModule;
import dev.kairos.ui.components.model.EnumSetting;
import dev.kairos.ui.components.model.ModuleCatalog;
import dev.kairos.ui.components.model.MutableValue;
import dev.kairos.ui.components.model.NumberSetting;
import dev.kairos.ui.components.model.UiCategory;
import dev.kairos.ui.components.model.UiModule;
import dev.kairos.ui.components.model.UiSetting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class KairosCatalog {
    private KairosCatalog() {}

    static ModuleCatalog create() {
        UiCategory combat = new UiCategory("combat", "Combat", "swords");
        UiCategory movement = new UiCategory("movement", "Movement", "move");
        UiCategory render = new UiCategory("render", "Render", "eye");
        List<UiModule> modules = new ArrayList<UiModule>();
        modules.add(module("kill-aura", "KillAura", "Advanced combat module", combat, true,
            new EnumSetting("mode", "Mode", new MutableValue<String>("Switch"), Arrays.asList("Switch", "Single", "Multi")),
            new NumberSetting("range", "Range", new MutableValue<Double>(3.4d), 1d, 6d, 0.1d),
            new NumberSetting("aps", "APS", new MutableValue<Double>(14d), 1d, 20d, 1d),
            new BooleanSetting("auto-block", "Auto Block", new MutableValue<Boolean>(true)),
            new BooleanSetting("walls", "Through Walls", new MutableValue<Boolean>(false))));
        modules.add(module("velocity", "Velocity", "Modify knockback", combat, true));
        modules.add(module("aim-assist", "AimAssist", "Improve your aim", combat, false));
        modules.add(module("sprint", "Sprint", "Automatic sprint", movement, true));
        modules.add(module("speed", "Speed", "Movement acceleration", movement, false));
        modules.add(module("esp", "ESP", "Entity highlighting", render, true));
        modules.add(module("full-bright", "FullBright", "World brightness", render, false));
        return new ModuleCatalog(Arrays.asList(combat, movement, render), modules);
    }

    private static UiModule module(String id, String name, String description, UiCategory category,
                                   boolean enabled, UiSetting<?>... settings) {
        return new DefaultUiModule(id, name, description, category, new MutableValue<Boolean>(enabled),
            Arrays.asList(settings));
    }
}
