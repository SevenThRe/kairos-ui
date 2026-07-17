package dev.kairos.ui.liquidbounce;

import dev.kairos.ui.web.WebSurface;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.montoyo.mcef.api.API;
import net.montoyo.mcef.api.IBrowser;
import org.lwjgl.opengl.GL11;

/** Java 8 MCEF backend hidden behind Kairos' renderer-neutral surface API. */
final class McefWebSurface implements WebSurface {
    private final String trustedFrame;
    private IBrowser browser;

    McefWebSurface(API api, String url) {
        if (api == null || api.isVirtual()) throw new IllegalStateException("MCEF runtime is unavailable");
        trustedFrame = url;
        browser = api.createBrowser(url, true);
        if (browser == null) throw new IllegalStateException("MCEF returned no browser");
    }

    @Override public void resize(int pixelWidth, int pixelHeight) {
        requireOpen().resize(Math.max(1, pixelWidth), Math.max(1, pixelHeight));
    }

    @Override
    public void render(double left, double top, double right, double bottom) {
        int texture = textureId();
        if (texture == 0) return;

        int oldTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GlStateManager.bindTexture(texture);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertices = tessellator.getBuffer();
        vertices.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        vertices.pos(left, top, 0.0D).tex(0.0D, 1.0D).color(255, 255, 255, 255).endVertex();
        vertices.pos(right, top, 0.0D).tex(1.0D, 1.0D).color(255, 255, 255, 255).endVertex();
        vertices.pos(right, bottom, 0.0D).tex(1.0D, 0.0D).color(255, 255, 255, 255).endVertex();
        vertices.pos(left, bottom, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
        tessellator.draw();
        GlStateManager.bindTexture(oldTexture);
    }

    @Override public void executeJavaScript(String script) { requireOpen().runJS(script, trustedFrame); }
    @Override public void mouseMove(int x, int y, int modifiers) { requireOpen().injectMouseMove(x, y, modifiers, false); }
    @Override public void mouseButton(int x, int y, int modifiers, int button, boolean pressed) {
        requireOpen().injectMouseButton(x, y, modifiers, button, pressed, 1);
    }
    @Override public void mouseWheel(int x, int y, int modifiers, int delta) {
        requireOpen().injectMouseWheel(x, y, modifiers, 1, delta);
    }
    @Override public void keyPressed(int keyCode, char character, int modifiers) {
        requireOpen().injectKeyPressedByKeyCode(keyCode, character, modifiers);
    }
    @Override public void keyTyped(char character, int modifiers) { requireOpen().injectKeyTyped(character, modifiers); }
    @Override public void keyReleased(int keyCode, char character, int modifiers) {
        requireOpen().injectKeyReleasedByKeyCode(keyCode, character, modifiers);
    }
    @Override public boolean isPageLoading() { return requireOpen().isPageLoading(); }
    @Override public int textureId() { return requireOpen().getTextureID(); }

    @Override
    public void close() {
        IBrowser current = browser;
        browser = null;
        if (current != null) current.close();
    }

    private IBrowser requireOpen() {
        if (browser == null) throw new IllegalStateException("web surface is closed");
        return browser;
    }
}
