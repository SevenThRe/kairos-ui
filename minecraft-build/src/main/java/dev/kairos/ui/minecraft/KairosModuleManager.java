package dev.kairos.ui.minecraft;

import dev.kairos.ui.components.model.BooleanSetting;
import dev.kairos.ui.components.model.ModuleCatalog;
import dev.kairos.ui.components.model.MutableValue;
import dev.kairos.ui.components.model.NumberSetting;
import dev.kairos.ui.components.model.UiCategory;
import dev.kairos.ui.components.model.UiModule;
import dev.kairos.ui.components.model.UiSetting;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.lang.reflect.Field;
//#if MC<11600
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.opengl.GL11;
//#endif

/** Forge-facing runtime for the first usable 1.12.2 client build. */
final class KairosModuleManager implements RuntimeUiModule.StateListener {
    private final Map<String, RuntimeUiModule> modules = new LinkedHashMap<String, RuntimeUiModule>();
    private final ModuleCatalog catalog;
    private final File configFile;
    private final KairosNotificationQueue notifications = new KairosNotificationQueue();
    private float previousGamma = 1f;
    private boolean gammaCaptured;
    private Field rightClickDelayField;

    KairosModuleManager(File dataDirectory) {
        UiCategory movement = new UiCategory("movement", "Movement", "move");
        UiCategory player = new UiCategory("player", "Player", "user");
        UiCategory render = new UiCategory("render", "Render", "eye");
        UiCategory misc = new UiCategory("misc", "Misc", "settings");

        add("sprint", "Sprint", "Automatically sprint while moving", movement, true);
        add("auto-jump", "AutoJump", "Jump when walking into a block", movement, false);
        add("auto-respawn", "AutoRespawn", "Respawn immediately after death", player, true);
        add("fast-place", "FastPlace", "Removes the right-click placement delay", player, false,
            new NumberSetting("delay", "Delay", new MutableValue<Double>(0d), 0d, 4d, 1d));
        add("full-bright", "FullBright", "Keeps the world at maximum brightness", render, false);
        add("player-esp", "PlayerESP", "Outlined player bounding boxes", render, true,
            new BooleanSetting("through-walls", "Through Walls", new MutableValue<Boolean>(true)),
            new NumberSetting("line-width", "Line Width", new MutableValue<Double>(1.5d), 1d, 4d, 0.5d));
        add("hud", "HUD", "Kairos watermark and client information", render, true);
        add("module-list", "ModuleList", "Right-aligned enabled module list", render, true,
            new BooleanSetting("background", "Background", new MutableValue<Boolean>(true)));
        add("coordinates", "Coordinates", "Position and direction overlay", render, true);
        add("notifications", "Notifications", "Module state notifications", misc, true);

        catalog = new ModuleCatalog(Arrays.asList(movement, player, render, misc),
            new ArrayList<UiModule>(modules.values()));
        configFile = new File(dataDirectory, "modules.properties");
        load();
    }

    private void add(String id, String name, String description, UiCategory category, boolean enabled,
                     UiSetting<?>... settings) {
        modules.put(id, new RuntimeUiModule(id, name, description, category, enabled,
            Arrays.asList(settings), this));
    }

    ModuleCatalog getCatalog() { return catalog; }
    RuntimeUiModule get(String id) { return modules.get(id); }
    KairosNotificationQueue getNotifications() { return notifications; }

    String listModules() {
        StringBuilder text = new StringBuilder();
        for (RuntimeUiModule module : modules.values()) {
            if (text.length() > 0) text.append(", ");
            text.append(module.getId());
        }
        return text.toString();
    }

    boolean toggle(String id) {
        RuntimeUiModule module = modules.get(id == null ? "" : id.toLowerCase());
        if (module == null) return false;
        module.setEnabled(!module.isEnabled());
        return true;
    }

    boolean setSetting(String moduleId, String settingId, String rawValue) {
        RuntimeUiModule module = modules.get(moduleId == null ? "" : moduleId.toLowerCase());
        if (module == null || settingId == null) return false;
        for (UiSetting<?> setting : module.getSettings()) {
            if (!settingId.equals(setting.getId())) continue;
            try {
                if (setting instanceof BooleanSetting) {
                    if (!"true".equalsIgnoreCase(rawValue) && !"false".equalsIgnoreCase(rawValue)) return false;
                    ((BooleanSetting) setting).setValue(Boolean.valueOf(rawValue));
                } else if (setting instanceof NumberSetting) {
                    NumberSetting number = (NumberSetting) setting;
                    double value = Double.parseDouble(rawValue);
                    value = Math.max(number.getMin(), Math.min(number.getMax(), value));
                    value = number.getMin() + Math.round((value - number.getMin()) / number.getStep()) * number.getStep();
                    number.setValue(value);
                } else {
                    return false;
                }
                save();
                return true;
            } catch (IllegalArgumentException exception) {
                return false;
            }
        }
        return false;
    }

    @Override public void onStateChanged(RuntimeUiModule module, boolean enabled) {
        if ("full-bright".equals(module.getId()) && !enabled) restoreGamma();
        save();
        RuntimeUiModule notificationModule = modules.get("notifications");
        if (notificationModule != null && notificationModule.isEnabled() && !"notifications".equals(module.getId())) {
            notifications.push(module.getDisplayName(), enabled ? "Enabled" : "Disabled",
                enabled ? 0xFF55D891 : 0xFFFF6470);
        }
    }

    void tick() {
        //#if MC>=11600
        // Modern runtime modules are added per platform island; this release targets Forge 1.12.2.
        //#else
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayerSP player = minecraft.player;
        if (minecraft.world == null || player == null) {
            if (enabled("full-bright")) applyGamma(minecraft);
            return;
        }
        if (enabled("sprint") && !player.isSneaking() && !player.collidedHorizontally
            && player.movementInput != null && player.movementInput.moveForward > 0.05f) {
            player.setSprinting(true);
        }
        if (enabled("auto-jump") && player.onGround && player.collidedHorizontally) player.jump();
        if (enabled("auto-respawn") && player.isDead) player.respawnPlayer();
        if (enabled("fast-place")) setRightClickDelay(minecraft, intSetting("fast-place", "delay", 0));
        if (enabled("full-bright")) applyGamma(minecraft);
        //#endif
    }

    private void restoreGamma() {
        //#if MC<11600
        if (!gammaCaptured) return;
        Minecraft.getMinecraft().gameSettings.gammaSetting = previousGamma;
        gammaCaptured = false;
        //#endif
    }

    //#if MC<11600
    private void applyGamma(Minecraft minecraft) {
        if (!gammaCaptured) {
            previousGamma = minecraft.gameSettings.gammaSetting;
            gammaCaptured = true;
        }
        minecraft.gameSettings.gammaSetting = 16f;
    }

    private void setRightClickDelay(Minecraft minecraft, int delay) {
        try {
            if (rightClickDelayField == null) {
                try { rightClickDelayField = Minecraft.class.getDeclaredField("rightClickDelayTimer"); }
                catch (NoSuchFieldException ignored) {
                    rightClickDelayField = Minecraft.class.getDeclaredField("field_71467_ac");
                }
                rightClickDelayField.setAccessible(true);
            }
            rightClickDelayField.setInt(minecraft, delay);
        } catch (ReflectiveOperationException exception) {
            RuntimeUiModule fastPlace = modules.get("fast-place");
            if (fastPlace != null) fastPlace.loadEnabled(false);
            notifications.push("FastPlace", "Unsupported mapping", 0xFFFF6470);
        }
    }

    void renderOverlay() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null || minecraft.gameSettings.showDebugInfo) return;
        ScaledResolution resolution = new ScaledResolution(minecraft);
        FontRenderer font = minecraft.fontRenderer;
        if (enabled("hud")) renderWatermark(font);
        if (enabled("module-list")) renderModuleList(font, resolution);
        if (enabled("coordinates")) renderCoordinates(font, resolution, minecraft.player);
        if (enabled("notifications")) renderNotifications(font, resolution);
    }

    private void renderWatermark(FontRenderer font) {
        String title = "Kairos  1.12.2";
        int width = font.getStringWidth(title);
        Gui.drawRect(7, 7, 22 + width, 28, 0xD511151B);
        Gui.drawRect(7, 7, 10, 28, 0xFF8B5CF6);
        font.drawStringWithShadow(title, 15, 14, 0xFFF2F4F7);
    }

    private void renderModuleList(FontRenderer font, ScaledResolution resolution) {
        List<RuntimeUiModule> active = new ArrayList<RuntimeUiModule>();
        for (RuntimeUiModule module : modules.values()) {
            if (module.isEnabled() && !"hud".equals(module.getId())
                && !"module-list".equals(module.getId()) && !"notifications".equals(module.getId())) active.add(module);
        }
        Collections.sort(active, new Comparator<RuntimeUiModule>() {
            @Override public int compare(RuntimeUiModule a, RuntimeUiModule b) {
                return font.getStringWidth(b.getDisplayName()) - font.getStringWidth(a.getDisplayName());
            }
        });
        boolean background = boolSetting("module-list", "background", true);
        int y = 7;
        for (int index = 0; index < active.size(); index++) {
            String name = active.get(index).getDisplayName();
            int width = font.getStringWidth(name);
            int x = resolution.getScaledWidth() - width - 9;
            int accent = gradient(index, Math.max(1, active.size()));
            if (background) Gui.drawRect(x - 6, y - 3, resolution.getScaledWidth(), y + 10, 0xB5101419);
            Gui.drawRect(resolution.getScaledWidth() - 2, y - 3, resolution.getScaledWidth(), y + 10, accent);
            font.drawStringWithShadow(name, x, y, accent);
            y += 13;
        }
    }

    private void renderCoordinates(FontRenderer font, ScaledResolution resolution, EntityPlayerSP player) {
        String xyz = String.format("XYZ  %.1f  %.1f  %.1f", player.posX, player.posY, player.posZ);
        String facing = player.getHorizontalFacing().getName().toUpperCase();
        int y = resolution.getScaledHeight() - 28;
        Gui.drawRect(7, y - 4, 20 + Math.max(font.getStringWidth(xyz), font.getStringWidth(facing)), y + 22, 0xB5101419);
        font.drawStringWithShadow(xyz, 13, y, 0xFFF0F2F5);
        font.drawStringWithShadow(facing, 13, y + 11, 0xFF9B7BFF);
    }

    private void renderNotifications(FontRenderer font, ScaledResolution resolution) {
        long now = System.currentTimeMillis();
        List<KairosNotificationQueue.Entry> entries = notifications.snapshot(now);
        int bottom = resolution.getScaledHeight() - 35;
        for (int index = entries.size() - 1; index >= 0; index--) {
            KairosNotificationQueue.Entry entry = entries.get(index);
            float life = Math.min(1f, (now - entry.createdAt) / 160f);
            float remaining = Math.max(0f, 1f - (now - entry.createdAt) / (float) entry.duration);
            int width = Math.max(154, Math.max(font.getStringWidth(entry.title), font.getStringWidth(entry.message)) + 28);
            int x = resolution.getScaledWidth() - Math.round(width * life) - 10;
            Gui.drawRect(x, bottom - 32, x + width, bottom, 0xE511151B);
            Gui.drawRect(x, bottom - 32, x + 3, bottom, entry.color);
            Gui.drawRect(x + 8, bottom - 3, x + 8 + Math.round((width - 16) * remaining), bottom - 1, entry.color);
            font.drawStringWithShadow(entry.title, x + 12, bottom - 24, 0xFFF2F4F7);
            font.drawString(entry.message, x + 12, bottom - 12, 0xFF9DA5AE);
            bottom -= 39;
        }
    }

    void renderWorld(float partialTicks) {
        if (!enabled("player-esp")) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null || minecraft.world == null) return;
        boolean throughWalls = boolSetting("player-esp", "through-walls", true);
        float lineWidth = (float) numberSetting("player-esp", "line-width", 1.5d);
        RenderManager manager = minecraft.getRenderManager();
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        if (throughWalls) GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GL11.glLineWidth(lineWidth);
        for (EntityPlayer target : minecraft.world.playerEntities) {
            if (target == minecraft.player || target.isDead) continue;
            double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * partialTicks - manager.viewerPosX;
            double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * partialTicks - manager.viewerPosY;
            double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * partialTicks - manager.viewerPosZ;
            AxisAlignedBB box = target.getEntityBoundingBox().offset(-target.posX, -target.posY, -target.posZ)
                .grow(0.05d).offset(x, y, z);
            float health = Math.max(0f, Math.min(1f, target.getHealth() / target.getMaxHealth()));
            RenderGlobal.drawSelectionBoundingBox(box, 1f - health * .65f, .25f + health * .65f, .38f, .95f);
        }
        GlStateManager.depthMask(true);
        if (throughWalls) GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
    //#endif

    private boolean enabled(String id) {
        RuntimeUiModule module = modules.get(id);
        return module != null && module.isEnabled();
    }

    private boolean boolSetting(String moduleId, String settingId, boolean fallback) {
        Object value = settingValue(moduleId, settingId);
        return value instanceof Boolean ? (Boolean) value : fallback;
    }

    private int intSetting(String moduleId, String settingId, int fallback) {
        return (int) Math.round(numberSetting(moduleId, settingId, fallback));
    }

    private double numberSetting(String moduleId, String settingId, double fallback) {
        Object value = settingValue(moduleId, settingId);
        return value instanceof Number ? ((Number) value).doubleValue() : fallback;
    }

    private Object settingValue(String moduleId, String settingId) {
        RuntimeUiModule module = modules.get(moduleId);
        if (module == null) return null;
        for (UiSetting<?> setting : module.getSettings()) {
            if (settingId.equals(setting.getId())) return setting.getValue();
        }
        return null;
    }

    private static int gradient(int index, int count) {
        float t = index / (float) Math.max(1, count - 1);
        int red = Math.round(139 + (91 - 139) * t);
        int green = Math.round(92 + (196 - 92) * t);
        int blue = Math.round(246 + (229 - 246) * t);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private void load() {
        if (!configFile.isFile()) return;
        Properties properties = new Properties();
        try {
            FileInputStream input = new FileInputStream(configFile);
            try { properties.load(input); } finally { input.close(); }
            for (RuntimeUiModule module : modules.values()) {
                String value = properties.getProperty("module." + module.getId() + ".enabled");
                if (value != null) module.loadEnabled(Boolean.parseBoolean(value));
                for (UiSetting<?> setting : module.getSettings()) {
                    String settingValue = properties.getProperty(settingKey(module, setting));
                    if (settingValue != null) loadSetting(setting, settingValue);
                }
            }
        } catch (IOException exception) {
            System.err.println("Kairos module config load failed: " + exception.getMessage());
        }
    }

    private void save() {
        File parent = configFile.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) return;
        Properties properties = new Properties();
        for (RuntimeUiModule module : modules.values()) {
            properties.setProperty("module." + module.getId() + ".enabled", String.valueOf(module.isEnabled()));
            for (UiSetting<?> setting : module.getSettings()) {
                properties.setProperty(settingKey(module, setting), String.valueOf(setting.getValue()));
            }
        }
        try {
            FileOutputStream output = new FileOutputStream(configFile);
            try { properties.store(output, "Kairos runtime modules"); } finally { output.close(); }
        } catch (IOException exception) {
            System.err.println("Kairos module config save failed: " + exception.getMessage());
        }
    }

    private static String settingKey(RuntimeUiModule module, UiSetting<?> setting) {
        return "module." + module.getId() + ".setting." + setting.getId();
    }

    private static void loadSetting(UiSetting<?> setting, String value) {
        try {
            if (setting instanceof BooleanSetting) {
                ((BooleanSetting) setting).setValue(Boolean.valueOf(value));
            } else if (setting instanceof NumberSetting) {
                ((NumberSetting) setting).setValue(Double.valueOf(value));
            }
        } catch (IllegalArgumentException ignored) {
            // Keep the declared default when a config entry is stale or malformed.
        }
    }
}
