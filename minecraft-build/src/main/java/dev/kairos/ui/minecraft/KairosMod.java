package dev.kairos.ui.minecraft;

import dev.kairos.ui.api.theme.ThemeRegistry;
import dev.kairos.ui.api.theme.ThemePack;
import dev.kairos.ui.platform.KairosClientCommand;
import dev.kairos.ui.platform.KairosGuiActivation;
import dev.kairos.ui.platform.ThemeDirectory;
import java.io.File;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;
//#if MC<11600
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
//#endif
import net.minecraftforge.common.MinecraftForge;
//#if MC>=11600
//$$ import net.minecraftforge.event.TickEvent;
//#else
import net.minecraftforge.fml.common.gameevent.TickEvent;
//#endif
import net.minecraftforge.fml.common.Mod;
//#if MC<11600
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//#endif
//#if MC>=11600
//$$ import com.mojang.blaze3d.platform.InputConstants;
//$$ import net.minecraft.network.chat.Component;
//#else
import org.lwjgl.input.Keyboard;
import net.minecraft.util.text.TextComponentString;
//#endif

//#if MC>=11600
//$$ @Mod(KairosMod.MOD_ID)
//#else
@Mod(modid = KairosMod.MOD_ID, name = "Kairos Client", version = "0.4.0", clientSideOnly = true,
    dependencies = "required-after:mcef")
//#endif
public final class KairosMod {
    public static final String MOD_ID = "kairos_ui";
    private static final ThemeRegistry THEMES = ThemeRegistry.kairosDefaults();
    private static ThemeDirectory themeDirectory;
    private static KairosModuleManager moduleManager;
    private static int openKeyCode =
        //#if MC>=11600
        //$$ KairosGuiActivation.GLFW_RIGHT_CONTROL;
        //#else
        KairosGuiActivation.LWJGL2_RIGHT_CONTROL;
        //#endif
    private boolean openKeyWasDown;

    public KairosMod() {
        // Launchers set user.dir to the active game directory on both supported endpoints.
        File dataDirectory = new File(System.getProperty("user.dir"), "kairos-ui");
        themeDirectory = new ThemeDirectory(dataDirectory);
        moduleManager = new KairosModuleManager(dataDirectory);
        try { themeDirectory.loadInto(THEMES); }
        catch (IOException exception) { System.err.println("Kairos theme load failed: " + exception.getMessage()); }
        //#if MC>=11600
        //$$ MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
        //$$ MinecraftForge.EVENT_BUS.addListener(this::onClientChat);
        //#else
        MinecraftForge.EVENT_BUS.register(this);
        //#endif
    }

    //#if MC<11600
    @Mod.EventHandler
    public void onPreInitialization(FMLPreInitializationEvent event) {
        // Custom schemes must be registered before MCEF creates CefApp during Forge init.
        KairosWebBridge.prepareScheme();
    }
    //#endif

    //#if MC<11600
    @SubscribeEvent
    //#endif
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        //#if MC>=11600
        //$$ Minecraft minecraft = Minecraft.getInstance();
        //$$ boolean keyDown = InputConstants.isKeyDown(minecraft.getWindow().getWindow(), openKeyCode);
        //$$ if (keyDown && !openKeyWasDown
        //$$     && (minecraft.screen == null || minecraft.screen instanceof KairosScreen)) toggleGui();
        //#else
        Minecraft minecraft = Minecraft.getMinecraft();
        boolean keyDown = Keyboard.isKeyDown(openKeyCode);
        if (keyDown && !openKeyWasDown
            && (minecraft.currentScreen == null || minecraft.currentScreen instanceof KairosScreen)) toggleGui();
        //#endif
        openKeyWasDown = keyDown;
        moduleManager.tick();
    }

    //#if MC<11600
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) moduleManager.renderOverlay();
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        moduleManager.renderWorld(event.getPartialTicks());
    }
    //#endif

    //#if MC<11600
    @SubscribeEvent
    //#endif
    public void onClientChat(ClientChatEvent event) {
        String raw = event.getMessage() == null ? "" : event.getMessage().trim();
        if (raw.matches("^[^A-Za-z0-9]kairos\\s+modules$")) {
            event.setCanceled(true);
            systemMessage("Kairos modules: " + moduleManager.listModules());
            return;
        }
        if (raw.matches("^[^A-Za-z0-9]kairos\\s+toggle\\s+[A-Za-z0-9._-]+$")) {
            event.setCanceled(true);
            String id = raw.substring(raw.lastIndexOf(' ') + 1);
            systemMessage(moduleManager.toggle(id) ? "Toggled " + id : "Unknown module: " + id);
            return;
        }
        KairosClientCommand command = KairosClientCommand.parseAnyPunctuationPrefix(event.getMessage());
        if (command == null) return;
        event.setCanceled(true);
        execute(command);
    }

    public static int getOpenKeyCode() { return openKeyCode; }
    public static ThemeRegistry getThemes() { return THEMES; }
    static KairosModuleManager getModuleManager() {
        if (moduleManager == null) {
            moduleManager = new KairosModuleManager(new File(System.getProperty("user.dir"), "kairos-ui"));
        }
        return moduleManager;
    }

    /** Allows a consuming client config/keybind service to replace the Right Ctrl default. */
    public static void setOpenKeyCode(int keyCode) {
        if (keyCode < 0) throw new IllegalArgumentException("keyCode");
        openKeyCode = keyCode;
    }

    /** Prefix-aware command hook for clients that want Kairos to parse their raw command text. */
    public static boolean handleGuiCommand(String message, String customerPrefix) {
        if (!KairosGuiActivation.matches(message, customerPrefix)) return false;
        openGui();
        return true;
    }

    /** Complete command hook for a consuming client's custom prefix manager. */
    public static boolean handleCommand(String message, String customerPrefix) {
        KairosClientCommand command = KairosClientCommand.parse(message, customerPrefix);
        if (command == null) return false;
        execute(command);
        return true;
    }

    private static void execute(KairosClientCommand command) {
        if (command.getAction() == KairosClientCommand.Action.OPEN_GUI) {
            openGui();
            return;
        }
        if (command.getAction() == KairosClientCommand.Action.LIST_THEMES) {
            StringBuilder names = new StringBuilder("Kairos themes: ");
            for (ThemePack theme : THEMES.getThemes()) {
                if (names.length() > 15) names.append(", ");
                names.append(theme.getId());
            }
            systemMessage(names.toString());
            return;
        }
        if (command.getAction() == KairosClientCommand.Action.RELOAD_THEMES) {
            try {
                int loaded = themeDirectory == null ? 0 : themeDirectory.loadInto(THEMES);
                systemMessage("Reloaded " + loaded + " Kairos theme file(s)");
            } catch (IOException exception) {
                systemMessage("Kairos theme reload failed: " + exception.getMessage());
            }
            return;
        }
        try {
            ThemePack selected = themeDirectory == null
                ? THEMES.activate(command.getArgument())
                : themeDirectory.select(THEMES, command.getArgument());
            systemMessage("Kairos theme: " + selected.getDisplayName());
        } catch (IllegalArgumentException exception) {
            systemMessage("Unknown Kairos theme: " + command.getArgument() + " (use .kairos themes)");
        } catch (IOException exception) {
            systemMessage("Kairos could not save the selected theme: " + exception.getMessage());
        }
    }

    private static void systemMessage(String message) {
        //#if MC>=11600
        //$$ Minecraft minecraft = Minecraft.getInstance();
        //$$ minecraft.execute(() -> minecraft.gui.getChat().addMessage(Component.literal(message)));
        //#else
        final Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.addScheduledTask(new Runnable() {
            @Override public void run() {
                minecraft.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
            }
        });
        //#endif
    }

    static void reportWebEngineUnavailable() {
        //#if MC>=11600
        //$$ systemMessage("Kairos Web UI is unavailable: install the Forge 1.20.1 WebView adapter.");
        //#else
        systemMessage("Kairos Web UI is unavailable: MCEF 1.12.2-1.11 did not initialize. No native fallback is enabled.");
        //#endif
    }

    private static boolean webUiAvailable() {
        //#if MC>=11600
        //$$ return false;
        //#else
        return KairosWebBridge.available();
        //#endif
    }

    /** Integration hook for a consuming client's own prefix-aware command manager. */
    public static void openGui() {
        if (!webUiAvailable()) {
            reportWebEngineUnavailable();
            return;
        }
        //#if MC>=11600
        //$$ Minecraft minecraft = Minecraft.getInstance();
        //$$ minecraft.execute(() -> minecraft.setScreen(new KairosScreen()));
        //#else
        final Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.addScheduledTask(new Runnable() {
            @Override public void run() { minecraft.displayGuiScreen(new KairosScreen()); }
        });
        //#endif
    }

    public static void toggleGui() {
        //#if MC>=11600
        //$$ Minecraft minecraft = Minecraft.getInstance();
        //$$ if (minecraft.screen instanceof KairosScreen) minecraft.setScreen(null);
        //$$ else openGui();
        //#else
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.currentScreen instanceof KairosScreen) minecraft.displayGuiScreen(null);
        else openGui();
        //#endif
    }
}
