//#if MC<11600
package dev.kairos.ui.minecraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.kairos.ui.components.model.BooleanSetting;
import dev.kairos.ui.components.model.NumberSetting;
import dev.kairos.ui.components.model.UiCategory;
import dev.kairos.ui.components.model.UiModule;
import dev.kairos.ui.components.model.UiSetting;
import net.minecraft.client.Minecraft;
import net.montoyo.mcef.api.API;
import net.montoyo.mcef.api.IBrowser;
import net.montoyo.mcef.api.IJSQueryCallback;
import net.montoyo.mcef.api.IJSQueryHandler;
import net.montoyo.mcef.api.MCEFApi;

/** Secure JavaScript bridge for the bundled kairos:// UI origin. */
final class KairosWebBridge implements IJSQueryHandler {
    static final String UI_URL = "kairos://ui/index.html";
    private static final KairosWebBridge INSTANCE = new KairosWebBridge();
    private static API api;
    private static boolean schemePrepared;
    private static boolean bridgeRegistered;

    private KairosWebBridge() {}

    static synchronized void prepareScheme() {
        if (schemePrepared) return;
        schemePrepared = true;
        api = MCEFApi.getAPI();
        if (api != null && !api.isSchemeRegistered("kairos")) {
            api.registerScheme("kairos", KairosResourceScheme.class,
                true, true, false, true, true, false, false);
        }
    }

    static synchronized API api() {
        prepareScheme();
        if (api != null && !api.isVirtual() && !bridgeRegistered) {
            api.registerJSQueryHandler(INSTANCE);
            bridgeRegistered = true;
        }
        return api;
    }

    static boolean available() {
        API value = api();
        return value != null && !value.isVirtual();
    }

    @Override public boolean handleQuery(final IBrowser browser, long queryId, String query,
                                         boolean persistent, final IJSQueryCallback callback) {
        if (browser == null || browser.getURL() == null || !browser.getURL().startsWith("kairos://ui/")) {
            return false;
        }
        final JsonObject request;
        try {
            JsonElement parsed = new JsonParser().parse(query);
            if (!parsed.isJsonObject()) throw new IllegalArgumentException("request must be an object");
            request = parsed.getAsJsonObject();
        } catch (RuntimeException exception) {
            callback.failure(400, "Invalid Kairos request");
            return true;
        }

        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            @Override public void run() { process(request, callback); }
        });
        return true;
    }

    private void process(JsonObject request, IJSQueryCallback callback) {
        String type = text(request, "type");
        try {
            if ("bootstrap".equals(type) || "refresh".equals(type)) {
                callback.success(state().toString());
                return;
            }
            if ("toggle".equals(type)) {
                if (!KairosMod.getModuleManager().toggle(text(request, "module"))) {
                    callback.failure(404, "Unknown module");
                    return;
                }
                callback.success(state().toString());
                return;
            }
            if ("setting".equals(type)) {
                JsonElement value = request.get("value");
                if (value == null || !KairosMod.getModuleManager().setSetting(
                    text(request, "module"), text(request, "setting"), value.getAsString())) {
                    callback.failure(400, "Invalid setting value");
                    return;
                }
                callback.success(state().toString());
                return;
            }
            if ("close".equals(type)) {
                callback.success("{\"ok\":true}");
                Minecraft.getMinecraft().displayGuiScreen(null);
                return;
            }
            callback.failure(400, "Unknown Kairos request");
        } catch (RuntimeException exception) {
            callback.failure(500, "Kairos bridge error: " + exception.getMessage());
        }
    }

    private JsonObject state() {
        JsonObject root = new JsonObject();
        root.addProperty("version", "0.4.0");
        root.addProperty("minecraft", "1.12.2");
        root.addProperty("player", Minecraft.getMinecraft().getSession().getUsername());
        JsonArray categories = new JsonArray();
        for (UiCategory category : KairosCatalog.create().getCategories()) {
            JsonObject item = new JsonObject();
            item.addProperty("id", category.getId());
            item.addProperty("name", category.getDisplayName());
            item.addProperty("icon", category.getIconId());
            categories.add(item);
        }
        root.add("categories", categories);

        JsonArray modules = new JsonArray();
        for (UiModule module : KairosCatalog.create().getModules()) {
            JsonObject item = new JsonObject();
            item.addProperty("id", module.getId());
            item.addProperty("name", module.getDisplayName());
            item.addProperty("description", module.getDescription());
            item.addProperty("category", module.getCategory().getId());
            item.addProperty("enabled", module.isEnabled());
            JsonArray settings = new JsonArray();
            for (UiSetting<?> setting : module.getSettings()) {
                if (!setting.isVisible()) continue;
                JsonObject settingJson = new JsonObject();
                settingJson.addProperty("id", setting.getId());
                settingJson.addProperty("name", setting.getDisplayName());
                settingJson.addProperty("group", setting.getGroup());
                if (setting instanceof BooleanSetting) {
                    settingJson.addProperty("type", "boolean");
                    settingJson.addProperty("value", ((BooleanSetting) setting).getValue());
                } else if (setting instanceof NumberSetting) {
                    NumberSetting number = (NumberSetting) setting;
                    settingJson.addProperty("type", "number");
                    settingJson.addProperty("value", number.getValue());
                    settingJson.addProperty("min", number.getMin());
                    settingJson.addProperty("max", number.getMax());
                    settingJson.addProperty("step", number.getStep());
                } else {
                    settingJson.addProperty("type", "text");
                    settingJson.addProperty("value", String.valueOf(setting.getValue()));
                }
                settings.add(settingJson);
            }
            item.add("settings", settings);
            modules.add(item);
        }
        root.add("modules", modules);
        return root;
    }

    private static String text(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? "" : value.getAsString();
    }

    @Override public void cancelQuery(IBrowser browser, long queryId) {}
}
//#endif
