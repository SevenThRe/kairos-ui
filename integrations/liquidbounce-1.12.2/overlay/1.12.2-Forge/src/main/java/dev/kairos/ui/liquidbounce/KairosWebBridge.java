package dev.kairos.ui.liquidbounce;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Locale;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.DoubleValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.ccbluex.liquidbounce.value.TextValue;
import net.ccbluex.liquidbounce.value.Value;
import net.minecraft.client.Minecraft;
import net.montoyo.mcef.api.API;
import net.montoyo.mcef.api.IBrowser;
import net.montoyo.mcef.api.IJSQueryCallback;
import net.montoyo.mcef.api.IJSQueryHandler;
import net.montoyo.mcef.api.MCEFApi;
import org.lwjgl.input.Keyboard;

/** Origin-checked bridge over the live LiquidBounce module and value registries. */
final class KairosWebBridge implements IJSQueryHandler {
    static final String UI_ROOT = "kairos://ui/";
    static final String UI_URL = UI_ROOT + "index.html";

    private static final KairosWebBridge INSTANCE = new KairosWebBridge();
    private static API api;
    private static boolean schemePrepared;
    private static boolean queryHandlerRegistered;

    private KairosWebBridge() {
    }

    static synchronized void prepareScheme() {
        if (schemePrepared) {
            return;
        }
        api = MCEFApi.getAPI();
        if (api != null && !api.isSchemeRegistered("kairos")) {
            api.registerScheme("kairos", KairosResourceScheme.class,
                true, true, false, true, true, false, false);
        }
        schemePrepared = true;
    }

    static synchronized API api() {
        prepareScheme();
        if (api != null && !api.isVirtual() && !queryHandlerRegistered) {
            api.registerJSQueryHandler(INSTANCE);
            queryHandlerRegistered = true;
        }
        return api;
    }

    @Override
    public boolean handleQuery(IBrowser browser, long queryId, String query,
                               boolean persistent, final IJSQueryCallback callback) {
        if (browser == null || browser.getURL() == null || !browser.getURL().startsWith(UI_ROOT)) {
            return false;
        }

        final JsonObject request;
        try {
            JsonElement parsed = new JsonParser().parse(query);
            if (!parsed.isJsonObject()) {
                throw new IllegalArgumentException("request must be a JSON object");
            }
            request = parsed.getAsJsonObject();
        } catch (RuntimeException exception) {
            callback.failure(400, "Invalid Kairos request");
            return true;
        }

        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            @Override
            public void run() {
                process(request, callback);
            }
        });
        return true;
    }

    @Override
    public void cancelQuery(IBrowser browser, long queryId) {
    }

    private void process(JsonObject request, IJSQueryCallback callback) {
        try {
            String type = string(request, "type");
            if ("bootstrap".equals(type) || "refresh".equals(type)) {
                callback.success(state().toString());
                return;
            }
            if ("toggle".equals(type)) {
                Module module = module(string(request, "module"));
                if (module == null) {
                    callback.failure(404, "Unknown module");
                    return;
                }
                if (!"ClickGUI".equalsIgnoreCase(module.getName())) {
                    module.toggle();
                }
                callback.success(state().toString());
                return;
            }
            if ("setting".equals(type)) {
                if (!setValue(string(request, "module"), string(request, "setting"), request.get("value"))) {
                    callback.failure(400, "Invalid or read-only setting value");
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
        } catch (Throwable throwable) {
            callback.failure(500, "Kairos bridge error: " + throwable.getClass().getSimpleName());
        }
    }

    private JsonObject state() {
        JsonObject root = new JsonObject();
        root.addProperty("version", "b73 · Kairos r1");
        root.addProperty("minecraft", "1.12.2 Forge");
        root.addProperty("player", Minecraft.getMinecraft().getSession().getUsername());
        root.addProperty("prefix", String.valueOf(LiquidBounce.INSTANCE.getCommandManager().getPrefix()));

        JsonArray categories = new JsonArray();
        for (ModuleCategory category : ModuleCategory.values()) {
            JsonObject item = new JsonObject();
            item.addProperty("id", category.name().toLowerCase(Locale.ROOT));
            item.addProperty("name", category.getDisplayName());
            item.addProperty("icon", category.name().toLowerCase(Locale.ROOT));
            categories.add(item);
        }
        root.add("categories", categories);

        JsonArray modules = new JsonArray();
        for (Module module : LiquidBounce.INSTANCE.getModuleManager().getModules()) {
            JsonObject item = new JsonObject();
            item.addProperty("id", module.getName());
            item.addProperty("name", module.getName());
            item.addProperty("description", module.getDescription());
            item.addProperty("category", module.getCategory().name().toLowerCase(Locale.ROOT));
            item.addProperty("enabled", module.getState());
            item.addProperty("toggleable", !"ClickGUI".equalsIgnoreCase(module.getName()));
            item.addProperty("keybind", module.getKeyBind() == Keyboard.KEY_NONE
                ? "None" : Keyboard.getKeyName(module.getKeyBind()));
            item.addProperty("array", module.getArray());

            JsonArray settings = new JsonArray();
            for (Value<?> value : module.getValues()) {
                settings.add(valueJson(value));
            }
            item.add("settings", settings);
            modules.add(item);
        }
        root.add("modules", modules);

        Module clickGui = module("ClickGUI");
        root.addProperty("theme", themeId(clickGui == null ? null : clickGui.getValue("Theme")));
        root.addProperty("animations", booleanValue(clickGui, "Animations", true));
        return root;
    }

    private JsonObject valueJson(Value<?> value) {
        JsonObject item = new JsonObject();
        item.addProperty("id", value.getName());
        item.addProperty("name", value.getName());
        item.addProperty("group", valueGroup(value.getName()));

        if (value instanceof BoolValue) {
            item.addProperty("type", "boolean");
            item.addProperty("value", ((BoolValue) value).get());
        } else if (value instanceof IntegerValue) {
            IntegerValue number = (IntegerValue) value;
            item.addProperty("type", "number");
            item.addProperty("value", number.get());
            item.addProperty("min", number.getMinimum());
            item.addProperty("max", number.getMaximum());
            item.addProperty("step", 1);
        } else if (value instanceof FloatValue) {
            FloatValue number = (FloatValue) value;
            item.addProperty("type", "number");
            item.addProperty("value", number.get());
            item.addProperty("min", number.getMinimum());
            item.addProperty("max", number.getMaximum());
            item.addProperty("step", decimalStep(number.getMinimum(), number.getMaximum()));
        } else if (value instanceof DoubleValue) {
            DoubleValue number = (DoubleValue) value;
            item.addProperty("type", "number");
            item.addProperty("value", number.get());
            item.addProperty("min", number.getMinimum());
            item.addProperty("max", number.getMaximum());
            item.addProperty("step", decimalStep(number.getMinimum(), number.getMaximum()));
        } else if (value instanceof ListValue) {
            ListValue list = (ListValue) value;
            item.addProperty("type", "list");
            item.addProperty("value", list.get());
            JsonArray options = new JsonArray();
            for (String option : list.getValues()) {
                options.add(option);
            }
            item.add("options", options);
        } else if (value instanceof TextValue) {
            item.addProperty("type", "text");
            item.addProperty("value", ((TextValue) value).get());
        } else {
            item.addProperty("type", "readonly");
            item.addProperty("value", String.valueOf(value.get()));
        }
        return item;
    }

    private boolean setValue(String moduleName, String valueName, JsonElement input) {
        Module module = module(moduleName);
        Value<?> value = module == null ? null : module.getValue(valueName);
        if (value == null || input == null || input.isJsonNull()) {
            return false;
        }

        try {
            if (value instanceof BoolValue) {
                ((BoolValue) value).set(input.getAsBoolean());
                return true;
            }
            if (value instanceof IntegerValue) {
                IntegerValue number = (IntegerValue) value;
                number.set(clamp(input.getAsInt(), number.getMinimum(), number.getMaximum()));
                return true;
            }
            if (value instanceof FloatValue) {
                FloatValue number = (FloatValue) value;
                number.set((float) clamp(input.getAsDouble(), number.getMinimum(), number.getMaximum()));
                return true;
            }
            if (value instanceof DoubleValue) {
                DoubleValue number = (DoubleValue) value;
                number.set(clamp(input.getAsDouble(), number.getMinimum(), number.getMaximum()));
                return true;
            }
            if (value instanceof ListValue) {
                String candidate = input.getAsString();
                ListValue list = (ListValue) value;
                if (!list.contains(candidate)) {
                    return false;
                }
                list.set(candidate);
                return true;
            }
            if (value instanceof TextValue) {
                String text = input.getAsString();
                if (text.length() > 512) {
                    return false;
                }
                ((TextValue) value).set(text);
                return true;
            }
        } catch (RuntimeException ignored) {
            return false;
        }
        return false;
    }

    private Module module(String name) {
        return LiquidBounce.INSTANCE.getModuleManager().getModule(name);
    }

    private static String string(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? "" : value.getAsString();
    }

    private static String themeId(Value<?> value) {
        if (value == null) return "violet";
        String raw = String.valueOf(value.get()).toLowerCase(Locale.ROOT);
        if (raw.contains("cyan")) return "cyan";
        if (raw.contains("rose")) return "rose";
        return "violet";
    }

    private static boolean booleanValue(Module module, String name, boolean fallback) {
        if (module == null) return fallback;
        Value<?> value = module.getValue(name);
        return value instanceof BoolValue ? ((BoolValue) value).get() : fallback;
    }

    private static String valueGroup(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.contains("target") || lower.contains("player") || lower.contains("mob")
            || lower.contains("animal") || lower.contains("invisible") || lower.contains("dead")) {
            return "Targets";
        }
        if (lower.contains("color") || lower.equals("r") || lower.equals("g") || lower.equals("b")
            || lower.contains("rainbow") || lower.contains("font") || lower.contains("outline")) {
            return "Visual";
        }
        if (lower.contains("delay") || lower.contains("cps") || lower.contains("aps")
            || lower.contains("speed") || lower.contains("timer")) {
            return "Timing";
        }
        if (lower.contains("legacy") || lower.contains("maxelements")) {
            return "Compatibility";
        }
        return "General";
    }

    private static double decimalStep(double min, double max) {
        double range = Math.abs(max - min);
        if (range <= 2.0D) return 0.01D;
        if (range <= 20.0D) return 0.05D;
        if (range <= 100.0D) return 0.1D;
        return 1.0D;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}

