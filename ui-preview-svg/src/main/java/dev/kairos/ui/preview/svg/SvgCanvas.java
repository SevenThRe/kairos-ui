package dev.kairos.ui.preview.svg;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.render.UiCanvas;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

/** Deterministic visual-test backend; it deliberately mirrors semantic canvas operations. */
public final class SvgCanvas implements UiCanvas {
    private final int width;
    private final int height;
    private final StringBuilder body = new StringBuilder();
    private final Deque<String> clips = new ArrayDeque<String>();
    private int nextClipId;

    public SvgCanvas(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override public void fillRect(Rect rect, int argb) {
        body.append("<rect ").append(rect(rect)).append(" fill=\"").append(color(argb)).append("\"")
            .append(opacity(argb)).append("/>");
    }

    @Override public void roundedRect(Rect rect, float radius, int argb) {
        body.append("<rect ").append(rect(rect)).append(" rx=\"").append(f(radius)).append("\" fill=\"")
            .append(color(argb)).append("\"").append(opacity(argb)).append("/>");
    }

    @Override public void glass(Rect rect, float cornerRadius, float blurRadius, int tintArgb) {
        body.append("<rect ").append(rect(rect)).append(" rx=\"").append(f(cornerRadius))
            .append("\" fill=\"").append(color(tintArgb)).append("\"").append(opacity(tintArgb))
            .append(" filter=\"url(#glass)\" stroke=\"#ffffff\" stroke-opacity=\"0.10\"/>");
    }

    @Override public void text(String fontId, String text, float x, float baseline, float size, int argb) {
        body.append("<text x=\"").append(f(x)).append("\" y=\"").append(f(baseline))
            .append("\" font-family=\"").append(escape(fontFamily(fontId))).append("\" font-size=\"")
            .append(f(size)).append("\" fill=\"").append(color(argb)).append("\"")
            .append(opacity(argb)).append(">").append(escape(text)).append("</text>");
    }

    @Override public void image(String textureId, Rect rect, int tintArgb) {
        body.append("<rect ").append(rect(rect)).append(" fill=\"").append(color(tintArgb))
            .append("\"").append(opacity(tintArgb)).append(" data-texture=\"")
            .append(escape(textureId)).append("\"/>");
    }

    @Override public void pushClip(Rect rect) {
        String id = "clip" + nextClipId++;
        body.append("<defs><clipPath id=\"").append(id).append("\"><rect ").append(rect(rect))
            .append("/></clipPath></defs><g clip-path=\"url(#").append(id).append(")\">");
        clips.push(id);
    }

    @Override public void popClip() {
        if (clips.isEmpty()) throw new IllegalStateException("Unbalanced SVG clip stack");
        clips.pop();
        body.append("</g>");
    }

    public String toSvg() {
        if (!clips.isEmpty()) throw new IllegalStateException("Cannot serialize with open clips");
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + width + "\" height=\"" + height
            + "\" viewBox=\"0 0 " + width + " " + height + "\">"
            + "<defs><filter id=\"glass\" x=\"-20%\" y=\"-20%\" width=\"140%\" height=\"140%\">"
            + "<feGaussianBlur stdDeviation=\"8\"/><feComponentTransfer><feFuncA type=\"linear\" slope=\"1.1\"/>"
            + "</feComponentTransfer></filter></defs>" + body + "</svg>";
    }

    private static String rect(Rect r) {
        return "x=\"" + f(r.getX()) + "\" y=\"" + f(r.getY()) + "\" width=\"" + f(r.getWidth())
            + "\" height=\"" + f(r.getHeight()) + "\"";
    }

    private static String color(int argb) { return String.format(Locale.ROOT, "#%06x", argb & 0xFFFFFF); }
    private static String opacity(int argb) {
        int alpha = (argb >>> 24) & 255;
        return alpha == 255 ? "" : " fill-opacity=\"" + f(alpha / 255f) + "\"";
    }
    private static String f(float value) { return String.format(Locale.ROOT, "%.2f", value); }
    private static String fontFamily(String id) {
        if (id.contains("mono")) return "JetBrains Mono, monospace";
        return "Inter, Noto Sans CJK SC, sans-serif";
    }
    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
