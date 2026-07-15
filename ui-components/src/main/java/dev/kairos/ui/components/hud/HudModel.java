package dev.kairos.ui.components.hud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private final List<String> enabledModules = new ArrayList<String>();
    private final List<String> notifications = new ArrayList<String>();

    public HudModel() {
        Collections.addAll(enabledModules, "KillAura", "Velocity", "AutoClicker", "AimAssist", "Sprint");
        Collections.addAll(notifications, "KillAura · Enabled", "Config · Loaded");
    }

    public List<String> getEnabledModules() { return enabledModules; }
    public List<String> getNotifications() { return notifications; }
}
