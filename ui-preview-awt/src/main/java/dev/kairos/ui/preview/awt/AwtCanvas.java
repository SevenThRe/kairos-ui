package dev.kairos.ui.preview.awt;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.render.UiCanvas;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.ArrayDeque;
import java.util.Deque;

/** Headless raster reference backend used for visual regression previews. */
public final class AwtCanvas implements UiCanvas {
    private final BufferedImage image;
    private final Graphics2D graphics;
    private final Deque<Shape> clips = new ArrayDeque<Shape>();

    public AwtCanvas(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    public void paintBackdrop() {
        GradientPaint gradient = new GradientPaint(0f, 0f, new Color(25, 43, 57),
            0f, image.getHeight(), new Color(8, 14, 18));
        graphics.setPaint(gradient);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

        // A deterministic, code-generated block world. It is only a preview
        // backdrop: every UI pixel above it still comes from UiCanvas calls.
        int horizon = image.getHeight() * 48 / 100;
        graphics.setColor(new Color(36, 59, 51));
        graphics.fillRect(0, horizon, image.getWidth(), image.getHeight() - horizon);
        int block = 64;
        for (int y = horizon; y < image.getHeight(); y += block) {
            for (int x = -block; x < image.getWidth() + block; x += block) {
                int parity = ((x / block) + (y / block)) & 1;
                graphics.setColor(parity == 0 ? new Color(43, 64, 53) : new Color(34, 54, 45));
                graphics.fillRect(x, y, block - 2, block - 2);
                graphics.setColor(new Color(255, 255, 255, 8));
                graphics.fillRect(x, y, block - 2, 2);
            }
        }
        graphics.setColor(new Color(13, 22, 26, 118));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
    }

    @Override public void fillRect(Rect rect, int argb) {
        graphics.setColor(new Color(argb, true));
        graphics.fillRect(i(rect.getX()), i(rect.getY()), i(rect.getWidth()), i(rect.getHeight()));
    }

    @Override public void roundedRect(Rect rect, float radius, int argb) {
        graphics.setColor(new Color(argb, true));
        graphics.fill(new RoundRectangle2D.Float(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(),
            radius * 2f, radius * 2f));
    }

    @Override public void glass(Rect rect, float cornerRadius, float blurRadius, int tintArgb) {
        int x = Math.max(0, i(rect.getX()));
        int y = Math.max(0, i(rect.getY()));
        if (x >= image.getWidth() || y >= image.getHeight() || rect.getRight() <= 0f || rect.getBottom() <= 0f) return;
        int width = Math.min(image.getWidth() - x, Math.max(1, i(rect.getWidth())));
        int height = Math.min(image.getHeight() - y, Math.max(1, i(rect.getHeight())));
        BufferedImage source = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D copy = source.createGraphics();
        copy.drawImage(image, 0, 0, width, height, x, y, x + width, y + height, null);
        copy.dispose();
        int kernelSize = Math.max(3, Math.min(9, i(blurRadius / 2f) | 1));
        float[] kernel = new float[kernelSize * kernelSize];
        for (int index = 0; index < kernel.length; index++) kernel[index] = 1f / kernel.length;
        BufferedImage blurred = new ConvolveOp(new Kernel(kernelSize, kernelSize, kernel), ConvolveOp.EDGE_NO_OP, null)
            .filter(source, null);
        Shape old = graphics.getClip();
        graphics.clip(new RoundRectangle2D.Float(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(),
            cornerRadius * 2f, cornerRadius * 2f));
        graphics.drawImage(blurred, x, y, null);
        graphics.setColor(new Color(tintArgb, true));
        graphics.fill(new RoundRectangle2D.Float(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(),
            cornerRadius * 2f, cornerRadius * 2f));
        graphics.setClip(old);
        graphics.setColor(new Color(255, 255, 255, 24));
        graphics.setStroke(new BasicStroke(1f));
        graphics.draw(new RoundRectangle2D.Float(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(),
            cornerRadius * 2f, cornerRadius * 2f));
    }

    @Override public void text(String fontId, String text, float x, float baseline, float size, int argb) {
        graphics.setFont(font(fontId, size));
        graphics.setColor(new Color(argb, true));
        graphics.drawString(text, x, baseline);
    }

    @Override public float measureText(String fontId, String text, float size) {
        if (text == null || text.isEmpty()) return 0f;
        FontMetrics metrics = graphics.getFontMetrics(font(fontId, size));
        return metrics.stringWidth(text);
    }

    @Override public void image(String textureId, Rect rect, int tintArgb) {
        if (textureId != null && textureId.startsWith("entity:")) {
            drawEntityPreview(rect, tintArgb);
            return;
        }
        if (textureId != null && textureId.startsWith("item:")) {
            drawItemPreview(textureId.substring(5), rect, tintArgb);
            return;
        }
        roundedRect(rect, Math.min(rect.getWidth(), rect.getHeight()) * 0.18f, tintArgb);
    }

    private void drawEntityPreview(Rect r, int tintArgb) {
        float unit = Math.max(2f, Math.min(r.getWidth() / 8f, r.getHeight() / 12f));
        float cx = r.getX() + r.getWidth() * .5f;
        int skin = multiply(0xFFB98262, tintArgb);
        int cloth = multiply(0xFFB82F36, tintArgb);
        int dark = multiply(0xFF252A31, tintArgb);
        fillRect(new Rect(cx - unit * 2f, r.getY() + unit, unit * 4f, unit * 3.4f), skin);
        fillRect(new Rect(cx - unit * 2.4f, r.getY() + unit * 4.5f, unit * 4.8f, unit * 4.1f), cloth);
        fillRect(new Rect(cx - unit * 3.5f, r.getY() + unit * 4.8f, unit, unit * 4.2f), skin);
        fillRect(new Rect(cx + unit * 2.5f, r.getY() + unit * 4.8f, unit, unit * 4.2f), skin);
        fillRect(new Rect(cx - unit * 2.2f, r.getY() + unit * 8.5f, unit * 1.8f, unit * 3.2f), dark);
        fillRect(new Rect(cx + unit * .4f, r.getY() + unit * 8.5f, unit * 1.8f, unit * 3.2f), dark);
        fillRect(new Rect(cx - unit * 1.25f, r.getY() + unit * 2.2f, unit * .55f, unit * .4f), 0xFF20242A);
        fillRect(new Rect(cx + unit * .7f, r.getY() + unit * 2.2f, unit * .55f, unit * .4f), 0xFF20242A);
    }

    private void drawItemPreview(String id, Rect r, int tintArgb) {
        float s = Math.min(r.getWidth(), r.getHeight());
        float x = r.getX() + (r.getWidth() - s) * .5f;
        float y = r.getY() + (r.getHeight() - s) * .5f;
        if (id.contains("sword")) {
            graphics.setStroke(new BasicStroke(Math.max(2f, s * .11f)));
            graphics.setColor(new Color(multiply(0xFFDCE7F1, tintArgb), true));
            graphics.drawLine(i(x + s * .72f), i(y + s * .18f), i(x + s * .30f), i(y + s * .70f));
            graphics.setColor(new Color(0xFF7E4B28, true));
            graphics.drawLine(i(x + s * .25f), i(y + s * .75f), i(x + s * .15f), i(y + s * .88f));
            graphics.drawLine(i(x + s * .20f), i(y + s * .63f), i(x + s * .38f), i(y + s * .79f));
        } else {
            fillRect(new Rect(x + s * .18f, y + s * .22f, s * .64f, s * .58f), multiply(0xFF7F8FA3, tintArgb));
            fillRect(new Rect(x + s * .25f, y + s * .29f, s * .5f, s * .12f), 0x55FFFFFF);
        }
    }

    private static int multiply(int argb, int tint) {
        int a = ((argb >>> 24) & 255) * ((tint >>> 24) & 255) / 255;
        int red = ((argb >>> 16) & 255) * ((tint >>> 16) & 255) / 255;
        int green = ((argb >>> 8) & 255) * ((tint >>> 8) & 255) / 255;
        int blue = (argb & 255) * (tint & 255) / 255;
        return (a << 24) | (red << 16) | (green << 8) | blue;
    }

    @Override public void pushClip(Rect rect) {
        Shape current = graphics.getClip();
        clips.push(current == null ? new Rectangle(0, 0, image.getWidth(), image.getHeight()) : current);
        graphics.clipRect(i(rect.getX()), i(rect.getY()), i(rect.getWidth()), i(rect.getHeight()));
    }

    @Override public void popClip() {
        if (clips.isEmpty()) throw new IllegalStateException("Unbalanced raster clip stack");
        graphics.setClip(clips.pop());
    }

    public BufferedImage getImage() { return image; }
    public void dispose() { graphics.dispose(); }
    private static Font font(String fontId, float size) {
        int style = fontId.contains("semibold") ? Font.BOLD : Font.PLAIN;
        String family = fontId.contains("mono") ? Font.MONOSPACED : Font.SANS_SERIF;
        return new Font(family, style, Math.max(1, i(size)));
    }
    private static int i(float value) { return Math.round(value); }
}
