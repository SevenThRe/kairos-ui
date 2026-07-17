package dev.kairos.ui.web;

/** Renderer-neutral browser surface consumed by Minecraft screens and HUDs. */
public interface WebSurface extends AutoCloseable {
    void resize(int pixelWidth, int pixelHeight);
    void render(double left, double top, double right, double bottom);
    void executeJavaScript(String script);
    void mouseMove(int x, int y, int modifiers);
    void mouseButton(int x, int y, int modifiers, int button, boolean pressed);
    void mouseWheel(int x, int y, int modifiers, int delta);
    void keyPressed(int keyCode, char character, int modifiers);
    void keyTyped(char character, int modifiers);
    void keyReleased(int keyCode, char character, int modifiers);
    boolean isPageLoading();
    int textureId();
    @Override void close();
}
