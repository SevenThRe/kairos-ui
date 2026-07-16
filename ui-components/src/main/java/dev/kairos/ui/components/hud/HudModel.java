package dev.kairos.ui.components.hud;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/** Mutable values supplied by the host client. No Minecraft classes leak into the shared HUD. */
public final class HudModel {
    public String clientName = "Kairos";
    public String clientVersion = "v1.2.0";
    public String minecraftVersion = "MC 1.12.2";
    public int fps = 128;
    public String sessionTime = "00:16:45";
    public int kills = 24;
    public int deaths = 3;
    public int wins = 2;
    public String targetName = "Steve";
    public float targetHealth = 18.5f;
    public float targetMaxHealth = 20f;
    public float targetDistance = 4.2f;
    public boolean moduleListBackground = true;
    public boolean moduleListRightBar = true;
    private final List<HudModuleEntry> moduleEntries = new ArrayList<HudModuleEntry>();
    private final NotificationCenter notificationCenter = new NotificationCenter();
    private final List<String> enabledModuleNames = new ModuleNameView();
    private final List<String> notificationMessages = new NotificationMessageView();

    public HudModel() { this(System.currentTimeMillis()); }

    /** Deterministic constructor used by visual regression previews. */
    public HudModel(long nowMillis) {
        moduleEntries.add(new HudModuleEntry("HitSelect", "SECOND", 0xFF55D48B));
        moduleEntries.add(new HudModuleEntry("LagRange", "150ms", 0xFF52C69D));
        moduleEntries.add(new HudModuleEntry("KillAura", "Switch", 0xFF62C3C6));
        moduleEntries.add(new HudModuleEntry("AutoClicker", "20", 0xFF6DAAEE));
        moduleEntries.add(new HudModuleEntry("Velocity", "Jump", 0xFF987AEF));
        moduleEntries.add(new HudModuleEntry("NoSlow", "", 0xFFD66FBE));
        notificationCenter.pushAt("Module enabled", "KillAura is now active", NotificationKind.SUCCESS,
            4200L, nowMillis - 900L);
        notificationCenter.pushAt("Configuration", "Hypixel profile loaded", NotificationKind.INFO,
            5200L, nowMillis - 250L);
    }

    public List<HudModuleEntry> getModuleEntries() { return moduleEntries; }
    public NotificationCenter getNotificationCenter() { return notificationCenter; }

    /** Compatibility view for integrations that previously supplied module names only. */
    public List<String> getEnabledModules() { return enabledModuleNames; }

    /** Compatibility view; new integrations should call getNotificationCenter().push(...). */
    public List<String> getNotifications() { return notificationMessages; }

    private final class ModuleNameView extends AbstractList<String> {
        @Override public String get(int index) { return moduleEntries.get(index).getName(); }
        @Override public int size() { return moduleEntries.size(); }
        @Override public void add(int index, String value) { moduleEntries.add(index, new HudModuleEntry(value)); }
        @Override public String set(int index, String value) {
            return moduleEntries.set(index, new HudModuleEntry(value)).getName();
        }
        @Override public String remove(int index) { return moduleEntries.remove(index).getName(); }
    }

    private final class NotificationMessageView extends AbstractList<String> {
        @Override public String get(int index) {
            return notificationCenter.snapshot(System.currentTimeMillis()).get(index).getMessage();
        }
        @Override public int size() { return notificationCenter.snapshot(System.currentTimeMillis()).size(); }
        @Override public boolean add(String value) {
            notificationCenter.push("Kairos", value, NotificationKind.INFO, 3600L);
            return true;
        }
        @Override public String remove(int index) {
            KairosNotification item = notificationCenter.snapshot(System.currentTimeMillis()).get(index);
            notificationCenter.dismiss(item.getId());
            return item.getMessage();
        }
        @Override public void clear() { notificationCenter.clear(); }
    }
}
