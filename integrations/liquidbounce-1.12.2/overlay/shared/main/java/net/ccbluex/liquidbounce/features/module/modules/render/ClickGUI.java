/*
 * LiquidBounce Hacked Client / Kairos UI integration
 * GPL-3.0
 */
package net.ccbluex.liquidbounce.features.module.modules.render;

import dev.kairos.ui.liquidbounce.KairosScreen;
import java.awt.Color;
import net.ccbluex.liquidbounce.api.minecraft.network.IPacket;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

@ModuleInfo(
    name = "ClickGUI",
    description = "Opens the Kairos Web ClickGUI.",
    category = ModuleCategory.RENDER,
    keyBind = Keyboard.KEY_RCONTROL,
    canEnable = false
)
public final class ClickGUI extends Module {
    public final ListValue themeValue = new ListValue(
        "Theme", new String[] {"ObsidianViolet", "NeonCyan", "RoseQuartz"}, "ObsidianViolet");
    public final BoolValue blurValue = new BoolValue("Blur", true);
    public final BoolValue animationsValue = new BoolValue("Animations", true);

    // Retained because the old HUD designer and script system reference these fields.
    public final FloatValue scaleValue = new FloatValue("Scale", 1F, 0.75F, 1.25F);
    public final IntegerValue maxElementsValue = new IntegerValue("MaxElements", 15, 1, 30);

    private static final IntegerValue colorRedValue = new IntegerValue("R", 139, 0, 255);
    private static final IntegerValue colorGreenValue = new IntegerValue("G", 92, 0, 255);
    private static final IntegerValue colorBlueValue = new IntegerValue("B", 246, 0, 255);
    private static final BoolValue colorRainbow = new BoolValue("Rainbow", false);

    @Override
    public void onEnable() {
        Minecraft.getMinecraft().displayGuiScreen(new KairosScreen());
    }

    public static Color generateColor() {
        return colorRainbow.get()
            ? ColorUtils.rainbow()
            : new Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get());
    }

    @EventTarget(ignoreCondition = true)
    public void onPacket(PacketEvent event) {
        IPacket packet = event.getPacket();
        if (classProvider.isSPacketCloseWindow(packet)
            && Minecraft.getMinecraft().currentScreen instanceof KairosScreen) {
            event.cancelEvent();
        }
    }
}
