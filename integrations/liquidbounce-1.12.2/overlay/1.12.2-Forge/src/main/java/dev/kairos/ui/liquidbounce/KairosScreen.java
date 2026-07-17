package dev.kairos.ui.liquidbounce;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.Value;
import dev.kairos.ui.web.WebSurface;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.montoyo.mcef.api.API;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/** Full-screen transparent MCEF host. No Minecraft canvas fallback exists. */
public final class KairosScreen extends GuiScreen {
    private static final long OPEN_KEY_GUARD_MS = 180L;

    private final long openedAt = System.currentTimeMillis();
    private WebSurface surface;
    private final KairosStateSync stateSync = new KairosStateSync();
    private boolean ownsBlurShader;

    @Override
    public void initGui() {
        API api = KairosWebBridge.api();
        if (api == null || api.isVirtual()) {
            reportFailure("MCEF could not initialize its Chromium runtime. Check the game log and network access on first launch.");
            mc.displayGuiScreen(null);
            return;
        }

        if (surface == null) {
            try {
                surface = new McefWebSurface(api, KairosWebBridge.UI_URL);
            } catch (Throwable throwable) {
                reportFailure("MCEF browser creation failed: " + throwable.getClass().getSimpleName());
                mc.displayGuiScreen(null);
                return;
            }
        }

        if (surface != null) {
            surface.resize(mc.displayWidth, mc.displayHeight);
        }

        if (blurEnabled() && !mc.entityRenderer.isShaderActive()) {
            try {
                mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
                ownsBlurShader = mc.entityRenderer.isShaderActive();
            } catch (Throwable ignored) {
                ownsBlurShader = false;
            }
        }

        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (surface == null) {
            return;
        }

        stateSync.tick(surface);

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        surface.render(0.0D, height, width, 0.0D);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
    }

    @Override
    public void handleInput() {
        while (Keyboard.next()) {
            int keyCode = Keyboard.getEventKey();
            boolean pressed = Keyboard.getEventKeyState();
            char character = Keyboard.getEventCharacter();

            if (pressed && keyCode == Keyboard.KEY_ESCAPE) {
                mc.displayGuiScreen(null);
                return;
            }
            if (pressed && keyCode == Keyboard.KEY_RCONTROL
                && System.currentTimeMillis() - openedAt > OPEN_KEY_GUARD_MS) {
                mc.displayGuiScreen(null);
                return;
            }

            if (surface == null) {
                continue;
            }

            int modifiers = keyboardModifiers();
            if (pressed) {
                surface.keyPressed(keyCode, character, modifiers);
                if (character != 0) {
                    surface.keyTyped(character, modifiers);
                }
            } else {
                surface.keyReleased(keyCode, character, modifiers);
            }
        }

        while (Mouse.next()) {
            if (surface == null) {
                continue;
            }

            int button = Mouse.getEventButton();
            boolean pressed = Mouse.getEventButtonState();
            int x = Mouse.getEventX();
            int y = mc.displayHeight - Mouse.getEventY();
            int wheel = Mouse.getEventDWheel();
            int modifiers = keyboardModifiers();

            if (wheel != 0) {
                surface.mouseWheel(x, y, modifiers, wheel);
            } else if (button == -1) {
                surface.mouseMove(x, y, modifiers);
            } else {
                surface.mouseButton(x, y, modifiers, button + 1, pressed);
            }
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        if (surface != null) {
            surface.close();
            surface = null;
        }
        if (ownsBlurShader) {
            mc.entityRenderer.stopUseShader();
            ownsBlurShader = false;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private int keyboardModifiers() {
        int modifiers = 0;
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            modifiers |= 1;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
            modifiers |= 2;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)) {
            modifiers |= 8;
        }
        return modifiers;
    }

    private boolean blurEnabled() {
        try {
            Module clickGui = LiquidBounce.INSTANCE.getModuleManager().getModule("ClickGUI");
            Value<?> value = clickGui == null ? null : clickGui.getValue("Blur");
            return !(value instanceof BoolValue) || ((BoolValue) value).get();
        } catch (Throwable ignored) {
            return true;
        }
    }

    private void reportFailure(String message) {
        if (mc.player != null) {
            mc.player.sendMessage(new TextComponentString("§8[§5Kairos§8] §c" + message));
        }
        System.err.println("[Kairos] " + message);
    }
}
