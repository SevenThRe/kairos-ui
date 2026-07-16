package dev.kairos.ui.minecraft;

import dev.kairos.ui.platform.KairosGuiActivation;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
//#if MC>=11600
//$$ import net.minecraftforge.event.TickEvent;
//#else
import net.minecraftforge.fml.common.gameevent.TickEvent;
//#endif
import net.minecraftforge.fml.common.Mod;
//#if MC<11600
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//#endif
//#if MC>=11600
//$$ import com.mojang.blaze3d.platform.InputConstants;
//#else
import org.lwjgl.input.Keyboard;
//#endif

//#if MC>=11600
//$$ @Mod(KairosMod.MOD_ID)
//#else
@Mod(modid = KairosMod.MOD_ID, name = "Kairos UI", version = "0.2.0", clientSideOnly = true)
//#endif
public final class KairosMod {
    public static final String MOD_ID = "kairos_ui";
    private static int openKeyCode =
        //#if MC>=11600
        //$$ KairosGuiActivation.GLFW_RIGHT_CONTROL;
        //#else
        KairosGuiActivation.LWJGL2_RIGHT_CONTROL;
        //#endif
    private boolean openKeyWasDown;

    public KairosMod() {
        //#if MC>=11600
        //$$ MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
        //$$ MinecraftForge.EVENT_BUS.addListener(this::onClientChat);
        //#else
        MinecraftForge.EVENT_BUS.register(this);
        //#endif
    }

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
    }

    //#if MC<11600
    @SubscribeEvent
    //#endif
    public void onClientChat(ClientChatEvent event) {
        if (!KairosGuiActivation.matchesAnyPunctuationPrefix(event.getMessage())) return;
        event.setCanceled(true);
        openGui();
    }

    public static int getOpenKeyCode() { return openKeyCode; }

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

    /** Integration hook for a consuming client's own prefix-aware command manager. */
    public static void openGui() {
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
        //$$ minecraft.setScreen(minecraft.screen instanceof KairosScreen ? null : new KairosScreen());
        //#else
        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.displayGuiScreen(minecraft.currentScreen instanceof KairosScreen ? null : new KairosScreen());
        //#endif
    }
}
