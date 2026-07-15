package dev.kairos.ui.minecraft;

import net.minecraft.client.Minecraft;
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
    private boolean openKeyWasDown;

    public KairosMod() {
        //#if MC>=11600
        //$$ MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
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
        //$$ boolean keyDown = InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 297);
        //$$ if (keyDown && !openKeyWasDown && minecraft.screen == null) minecraft.setScreen(new KairosScreen());
        //#else
        Minecraft minecraft = Minecraft.getMinecraft();
        boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_F8);
        if (keyDown && !openKeyWasDown && minecraft.currentScreen == null) minecraft.displayGuiScreen(new KairosScreen());
        //#endif
        openKeyWasDown = keyDown;
    }
}
