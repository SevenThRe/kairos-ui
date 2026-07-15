package dev.kairos.ui.example;

import dev.kairos.ui.components.model.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class DemoCatalog {
    private DemoCatalog() {}

    public static ModuleCatalog create() {
        UiCategory combat = new UiCategory("combat", "Combat", "swords");
        UiCategory movement = new UiCategory("movement", "Movement", "movement");
        UiCategory player = new UiCategory("player", "Player", "player");
        UiCategory render = new UiCategory("render", "Render", "eye");
        UiCategory world = new UiCategory("world", "World", "globe");
        UiCategory misc = new UiCategory("misc", "Misc", "settings");
        UiCategory exploit = new UiCategory("exploit", "Exploit", "hex");
        final MutableValue<String> mode = new MutableValue<String>("Switch");

        EnumSetting modeSetting = new EnumSetting("mode", "Mode", mode,
            Arrays.asList("Switch", "Vanilla", "Hypixel"));
        NumberSetting range = new NumberSetting("range", "Range",
            new MutableValue<Double>(3.4d), 1d, 6d, 0.1d);
        RangeSetting aps = new RangeSetting("aps", "APS",
            new MutableValue<RangeValue>(new RangeValue(10d, 14d)), 1d, 20d, 1d);
        EnumSetting priority = new EnumSetting("priority", "Target Priority",
            new MutableValue<String>("Distance"), Arrays.asList("Distance", "Health", "Angle"));
        Set<String> initialTargets = new LinkedHashSet<String>(Arrays.asList("Players", "Animals"));
        MultiSelectSetting targets = new MultiSelectSetting("targets", "Targets",
            new MutableValue<Set<String>>(initialTargets), Arrays.asList("Players", "Animals", "Mobs", "Invisible"));
        BooleanSetting autoBlock = new BooleanSetting("auto-block", "Auto Block", new MutableValue<Boolean>(true));
        BooleanSetting throughWalls = new BooleanSetting("through-walls", "Through Walls", new MutableValue<Boolean>(false));
        BooleanSetting silentRotation = new BooleanSetting("silent-rotation", "Silent Rotation", new MutableValue<Boolean>(true));

        NumberSetting targetRange = new NumberSetting("target-range", "Target Range",
            new MutableValue<Double>(5.2d), 1d, 12d, 0.1d);
        targetRange.inGroup("Target");
        EnumSetting targetSort = new EnumSetting("target-sort", "Sort Strategy",
            new MutableValue<String>("Smart"), Arrays.asList("Smart", "Nearest", "Lowest HP"));
        targetSort.inGroup("Target");
        BooleanSetting teams = new BooleanSetting("teams", "Ignore Teams", new MutableValue<Boolean>(true));
        teams.inGroup("Target");

        EnumSetting rotation = new EnumSetting("rotation", "Rotation Mode",
            new MutableValue<String>("Smooth"), Arrays.asList("Smooth", "Snap", "None"));
        rotation.inGroup("Advanced");
        NumberSetting boostDelay = new NumberSetting("boost-delay", "Boost Delay",
            new MutableValue<Double>(755d), 0d, 2000d, 5d);
        boostDelay.inGroup("Advanced");
        boostDelay.visibleWhen(new VisibilityRule() {
            @Override public boolean isVisible() { return "Hypixel".equals(mode.get()); }
        });
        NumberSetting failRate = new NumberSetting("fail-rate", "Humanize Fail Rate",
            new MutableValue<Double>(2d), 0d, 25d, 1d);
        failRate.inGroup("Advanced");

        ColorSetting accent = new ColorSetting("accent", "Target Accent", new MutableValue<Integer>(0xFF7657F6));
        accent.inGroup("Visual");
        TextSetting label = new TextSetting("label", "HUD Label", new MutableValue<String>("Target"), 24);
        label.inGroup("Visual");
        KeybindSetting keybind = new KeybindSetting("keybind", "Keybind",
            new MutableValue<KeyChord>(new KeyChord(82, 0, "R")));
        keybind.inGroup("Visual");

        List<UiSetting<?>> auraSettings = Arrays.<UiSetting<?>>asList(modeSetting, range, aps, priority, targets,
            autoBlock, throughWalls, silentRotation, targetRange, targetSort, teams, rotation, boostDelay,
            failRate, accent, label, keybind);
        UiModule killAura = new DefaultUiModule("kill-aura", "KillAura", "Advanced combat module",
            combat, new MutableValue<Boolean>(true), auraSettings);

        List<UiModule> modules = new ArrayList<UiModule>();
        modules.add(killAura);
        modules.add(module("velocity", "Velocity", "Modify knockback", combat, true));
        modules.add(module("aim-assist", "AimAssist", "Improve your aim", combat, false));
        modules.add(module("auto-clicker", "AutoClicker", "Automatic clicking", combat, true));
        modules.add(module("criticals", "Criticals", "Always deal critical hits", combat, false));
        modules.add(module("sprint", "Sprint", "Automatic sprinting", movement, true));
        modules.add(module("speed", "Speed", "Movement acceleration", movement, false));
        modules.add(module("flight", "Flight", "Creative-like movement", movement, false));
        modules.add(module("inventory", "InventoryManager", "Organize inventory", player, true));
        modules.add(module("fast-use", "FastUse", "Reduce use delay", player, false));
        modules.add(module("esp", "ESP", "Highlight entities", render, true));
        modules.add(module("name-tags", "NameTags", "Modern name plates", render, true));
        modules.add(module("full-bright", "FullBright", "Improve visibility", render, false));
        modules.add(module("scaffold", "Scaffold", "Place blocks automatically", world, false));
        modules.add(module("breaker", "Breaker", "Break selected blocks", world, false));
        modules.add(module("notifications", "Notifications", "Client status toasts", misc, true));
        modules.add(module("profiles", "Profiles", "Manage configurations", misc, true));
        modules.add(module("disabler", "Disabler", "Protocol experiments", exploit, false));

        return new ModuleCatalog(Arrays.asList(combat, movement, player, render, world, misc, exploit), modules);
    }

    private static UiModule module(String id, String name, String description, UiCategory category, boolean enabled) {
        return new DefaultUiModule(id, name, description, category, new MutableValue<Boolean>(enabled),
            Collections.<UiSetting<?>>emptyList());
    }
}
