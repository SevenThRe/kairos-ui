package dev.kairos.ui.minecraft;

//#if MC>=11600
//$$ import net.minecraft.client.gui.GuiGraphics;
//$$ import net.minecraft.client.gui.screens.Screen;
//$$ import net.minecraft.network.chat.Component;
//#else
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.montoyo.mcef.api.API;
import net.montoyo.mcef.api.IBrowser;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
//#endif

/**
 * Web-only ClickGUI host. There is deliberately no native canvas fallback: a browser
 * failure must stay visible as an integration error instead of silently changing UI.
 */
public final class KairosScreen
    //#if MC>=11600
    //$$ extends Screen
    //#else
    extends GuiScreen
    //#endif
{
    //#if MC<11600
    private IBrowser browser;
    private boolean ownsBlurShader;
    //#endif

    public KairosScreen() {
        //#if MC>=11600
        //$$ super(Component.literal("Kairos Web UI"));
        //#endif
    }

    //#if MC>=11600
    //$$ @Override protected void init() {
    //$$     KairosMod.reportWebEngineUnavailable();
    //$$     onClose();
    //$$ }
    //$$ @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}
    //$$ @Override public boolean isPauseScreen() { return false; }
    //#else
    @Override public void initGui() {
        API api = KairosWebBridge.api();
        if (api == null || api.isVirtual()) {
            KairosMod.reportWebEngineUnavailable();
            mc.displayGuiScreen(null);
            return;
        }
        if (browser == null) browser = api.createBrowser(KairosWebBridge.UI_URL, true);
        if (browser != null) browser.resize(mc.displayWidth, mc.displayHeight);
        if (!mc.entityRenderer.isShaderActive()) {
            try {
                mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
                ownsBlurShader = mc.entityRenderer.isShaderActive();
            } catch (RuntimeException exception) {
                ownsBlurShader = false;
            }
        }
        Keyboard.enableRepeatEvents(true);
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (browser == null) return;
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1f, 1f, 1f, 1f);
        browser.draw(0d, height, width, 0d);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
    }

    @Override public void handleInput() {
        while (Keyboard.next()) {
            int keyCode = Keyboard.getEventKey();
            boolean pressed = Keyboard.getEventKeyState();
            char character = Keyboard.getEventCharacter();
            if (pressed && (keyCode == Keyboard.KEY_ESCAPE || keyCode == KairosMod.getOpenKeyCode())) {
                mc.displayGuiScreen(null);
                return;
            }
            if (browser == null) continue;
            int modifiers = keyboardModifiers();
            if (pressed) browser.injectKeyPressedByKeyCode(keyCode, character, modifiers);
            else browser.injectKeyReleasedByKeyCode(keyCode, character, modifiers);
            if (pressed && character != 0) browser.injectKeyTyped(character, modifiers);
        }

        while (Mouse.next()) {
            if (browser == null) continue;
            int button = Mouse.getEventButton();
            boolean pressed = Mouse.getEventButtonState();
            int x = Mouse.getEventX();
            int y = mc.displayHeight - Mouse.getEventY();
            int wheel = Mouse.getEventDWheel();
            int modifiers = keyboardModifiers();
            if (wheel != 0) browser.injectMouseWheel(x, y, modifiers, 1, wheel);
            else if (button == -1) browser.injectMouseMove(x, y, modifiers, false);
            else browser.injectMouseButton(x, y, modifiers, button + 1, pressed, 1);
        }
    }

    private int keyboardModifiers() {
        int modifiers = 0;
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) modifiers |= 1;
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) modifiers |= 2;
        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)) modifiers |= 8;
        return modifiers;
    }

    @Override public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        if (browser != null) {
            browser.close();
            browser = null;
        }
        if (ownsBlurShader) {
            mc.entityRenderer.stopUseShader();
            ownsBlurShader = false;
        }
    }

    @Override public boolean doesGuiPauseGame() { return false; }
    //#endif
}
