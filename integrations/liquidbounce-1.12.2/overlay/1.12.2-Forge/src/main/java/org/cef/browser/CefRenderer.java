// Copyright (c) 2013 The Chromium Embedded Framework Authors.
// Modified by montoyo for MCEF and by Kairos for packed dirty-region uploads.
// Use of this source code is governed by the BSD license in MCEF's LICENSE file.

package org.cef.browser;

import dev.kairos.ui.web.DirtyRect;
import dev.kairos.ui.web.PixelFrame;
import java.awt.Rectangle;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.montoyo.mcef.MCEF;
import net.montoyo.mcef.utilities.Log;
import org.lwjgl.opengl.EXTBgra;

import static org.lwjgl.opengl.GL11.*;

/** GL-thread consumer for Kairos packed browser frames. */
public class CefRenderer {
    private static final ArrayList<Integer> GL_TEXTURES = new ArrayList<Integer>();

    public static void dumpVRAMLeak() {
        Log.info(">>>>> MCEF: Beginning VRAM leak report");
        for (Integer texture : GL_TEXTURES) {
            Log.warning(">>>>> MCEF: This texture has not been freed: " + texture);
        }
        Log.info(">>>>> MCEF: End of VRAM leak report");
    }

    private final boolean transparent_;
    public int[] texture_id_ = new int[1];
    private int view_width_;
    private int view_height_;
    private Rectangle popup_rect_ = new Rectangle(0, 0, 0, 0);
    private Rectangle original_popup_rect_ = new Rectangle(0, 0, 0, 0);

    protected CefRenderer(boolean transparent) {
        transparent_ = transparent;
        initialize();
    }

    protected boolean isTransparent() {
        return transparent_;
    }

    protected void initialize() {
        texture_id_[0] = glGenTextures();
        if (MCEF.CHECK_VRAM_LEAK) GL_TEXTURES.add(texture_id_[0]);

        int oldBinding = glGetInteger(GL_TEXTURE_BINDING_2D);
        GlStateManager.bindTexture(texture_id_[0]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
        glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        GlStateManager.bindTexture(oldBinding);
    }

    protected void cleanup() {
        if (texture_id_[0] == 0) return;
        if (MCEF.CHECK_VRAM_LEAK) GL_TEXTURES.remove((Object) texture_id_[0]);
        glDeleteTextures(texture_id_[0]);
        texture_id_[0] = 0;
        view_width_ = 0;
        view_height_ = 0;
    }

    public void render(double x1, double y1, double x2, double y2) {
        if (view_width_ == 0 || view_height_ == 0 || texture_id_[0] == 0) return;

        int oldBinding = glGetInteger(GL_TEXTURE_BINDING_2D);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.bindTexture(texture_id_[0]);
        buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(x1, y1, 0.0D).tex(0.0D, 1.0D).color(255, 255, 255, 255).endVertex();
        buffer.pos(x2, y1, 0.0D).tex(1.0D, 1.0D).color(255, 255, 255, 255).endVertex();
        buffer.pos(x2, y2, 0.0D).tex(1.0D, 0.0D).color(255, 255, 255, 255).endVertex();
        buffer.pos(x1, y2, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
        tessellator.draw();
        GlStateManager.bindTexture(oldBinding);
    }

    /** Called only by ClientProxy's render tick while the GL context is current. */
    protected void onFrame(PixelFrame frame) {
        if (frame == null || frame.patches().isEmpty() || texture_id_[0] == 0) return;

        int oldBinding = glGetInteger(GL_TEXTURE_BINDING_2D);
        int oldAlignment = glGetInteger(GL_UNPACK_ALIGNMENT);
        int oldRowLength = glGetInteger(GL_UNPACK_ROW_LENGTH);
        int oldSkipPixels = glGetInteger(GL_UNPACK_SKIP_PIXELS);
        int oldSkipRows = glGetInteger(GL_UNPACK_SKIP_ROWS);

        try {
            GlStateManager.bindTexture(texture_id_[0]);
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
            glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
            glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);

            if (frame.isFull() || frame.width() != view_width_ || frame.height() != view_height_) {
                PixelFrame.Patch patch = frame.patches().get(0);
                view_width_ = frame.width();
                view_height_ = frame.height();
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, view_width_, view_height_, 0,
                    EXTBgra.GL_BGRA_EXT, GL_UNSIGNED_BYTE, frame.pixels(patch));
                return;
            }

            for (PixelFrame.Patch patch : frame.patches()) {
                DirtyRect rect = patch.rect();
                if (rect.x() < 0 || rect.y() < 0 || rect.right() > view_width_
                    || rect.bottom() > view_height_) {
                    Log.warning("Kairos rejected an out-of-bounds browser dirty rectangle");
                    continue;
                }
                ByteBuffer pixels = frame.pixels(patch);
                glTexSubImage2D(GL_TEXTURE_2D, 0, rect.x(), rect.y(), rect.width(), rect.height(),
                    EXTBgra.GL_BGRA_EXT, GL_UNSIGNED_BYTE, pixels);
            }
        } finally {
            glPixelStorei(GL_UNPACK_ALIGNMENT, oldAlignment);
            glPixelStorei(GL_UNPACK_ROW_LENGTH, oldRowLength);
            glPixelStorei(GL_UNPACK_SKIP_PIXELS, oldSkipPixels);
            glPixelStorei(GL_UNPACK_SKIP_ROWS, oldSkipRows);
            GlStateManager.bindTexture(oldBinding);
        }
    }

    protected void onPopupSize(Rectangle rect) {
        if (rect.width <= 0 || rect.height <= 0) return;
        original_popup_rect_ = rect;
        popup_rect_ = getPopupRectInWebView(original_popup_rect_);
    }

    protected Rectangle getPopupRectInWebView(Rectangle rect) {
        if (rect.x < 0) rect.x = 0;
        if (rect.y < 0) rect.y = 0;
        if (rect.x + rect.width > view_width_) rect.x = view_width_ - rect.width;
        if (rect.y + rect.height > view_height_) rect.y = view_height_ - rect.height;
        if (rect.x < 0) rect.x = 0;
        if (rect.y < 0) rect.y = 0;
        return rect;
    }

    protected void clearPopupRects() {
        popup_rect_.setBounds(0, 0, 0, 0);
        original_popup_rect_.setBounds(0, 0, 0, 0);
    }

    public int getViewWidth() { return view_width_; }
    public int getViewHeight() { return view_height_; }
}
