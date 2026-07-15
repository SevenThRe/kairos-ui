package dev.kairos.ui.minecraft;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.render.UiCanvas;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.Minecraft;
//#if MC>=11600
//$$ import net.minecraft.client.gui.GuiGraphics;
//#else
import dev.kairos.ui.platform.ScissorBox;
import dev.kairos.ui.platform.legacy.Legacy112ScissorMapper;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;
//#endif

/** Always-available compatibility renderer; the shader backend can replace it without changing scenes. */
final class MinecraftFallbackCanvas implements UiCanvas {
    private final Minecraft minecraft;
    private final Deque<Rect> clips = new ArrayDeque<Rect>();
    //#if MC>=11600
    //$$ private final GuiGraphics graphics;
    //$$ MinecraftFallbackCanvas(GuiGraphics graphics) {
    //$$     this.minecraft = Minecraft.getInstance();
    //$$     this.graphics = graphics;
    //$$ }
    //#else
    MinecraftFallbackCanvas() { this.minecraft = Minecraft.getMinecraft(); }
    //#endif

    @Override public void fillRect(Rect rect, int argb) {
        //#if MC>=11600
        //$$ graphics.fill(i(rect.getX()), i(rect.getY()), i(rect.getRight()), i(rect.getBottom()), argb);
        //#else
        Gui.drawRect(i(rect.getX()), i(rect.getY()), i(rect.getRight()), i(rect.getBottom()), argb);
        //#endif
    }

    @Override public void roundedRect(Rect rect, float radius, int argb) { fillRect(rect, argb); }
    @Override public void glass(Rect rect, float cornerRadius, float blurRadius, int tintArgb) { fillRect(rect, tintArgb); }

    @Override public void text(String fontId, String text, float x, float baseline, float size, int argb) {
        int y = i(baseline - size);
        //#if MC>=11600
        //$$ graphics.drawString(minecraft.font, text, i(x), y, argb, false);
        //#else
        minecraft.fontRenderer.drawString(text, i(x), y, argb, false);
        //#endif
    }

    @Override public void image(String textureId, Rect rect, int tintArgb) { roundedRect(rect, 4f, tintArgb); }

    @Override public void pushClip(Rect rect) {
        Rect clip = clips.isEmpty() ? rect : intersect(clips.peek(), rect);
        clips.push(clip);
        //#if MC>=11600
        //$$ graphics.enableScissor(i(clip.getX()), i(clip.getY()), i(clip.getRight()), i(clip.getBottom()));
        //#else
        ScaledResolution scaled = new ScaledResolution(minecraft);
        ScissorBox box = Legacy112ScissorMapper.map(clip, scaled.getScaleFactor(), minecraft.displayHeight);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(box.x, box.y, box.width, box.height);
        //#endif
    }

    @Override public void popClip() {
        if (clips.isEmpty()) throw new IllegalStateException("Unbalanced clip stack");
        clips.pop();
        //#if MC>=11600
        //$$ if (clips.isEmpty()) graphics.disableScissor();
        //$$ else {
        //$$     Rect clip = clips.peek();
        //$$     graphics.enableScissor(i(clip.getX()), i(clip.getY()), i(clip.getRight()), i(clip.getBottom()));
        //$$ }
        //#else
        if (clips.isEmpty()) GL11.glDisable(GL11.GL_SCISSOR_TEST);
        else {
            ScaledResolution scaled = new ScaledResolution(minecraft);
            ScissorBox box = Legacy112ScissorMapper.map(clips.peek(), scaled.getScaleFactor(), minecraft.displayHeight);
            GL11.glScissor(box.x, box.y, box.width, box.height);
        }
        //#endif
    }

    private static Rect intersect(Rect a, Rect b) {
        float left = Math.max(a.getX(), b.getX());
        float top = Math.max(a.getY(), b.getY());
        float right = Math.max(left, Math.min(a.getRight(), b.getRight()));
        float bottom = Math.max(top, Math.min(a.getBottom(), b.getBottom()));
        return new Rect(left, top, right - left, bottom - top);
    }

    private static int i(float value) { return Math.round(value); }
}
