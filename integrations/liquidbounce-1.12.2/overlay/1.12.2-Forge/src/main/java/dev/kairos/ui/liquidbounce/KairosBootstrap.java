package dev.kairos.ui.liquidbounce;

import net.montoyo.mcef.MCEF;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/** Forge lifecycle hook for the web-only Kairos ClickGUI. */
@Mod(
    modid = KairosBootstrap.MOD_ID,
    name = "Kairos UI",
    version = "1.12.2-b73-kairos-r2",
    clientSideOnly = true,
    dependencies = "after:mcef"
)
public final class KairosBootstrap {
    public static final String MOD_ID = "kairos_ui";

    @Mod.EventHandler
    public void onPreInitialization(FMLPreInitializationEvent event) {
        // The example F10 browser is not part of Kairos. MCEF has already read its
        // config at this point, while CefApp has not been created yet.
        MCEF.ENABLE_EXAMPLE = false;
        KairosWebBridge.prepareScheme();
    }
}
