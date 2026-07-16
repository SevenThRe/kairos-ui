package dev.kairos.ui.minecraft;

import dev.kairos.ui.api.geometry.Rect;
import dev.kairos.ui.api.input.EventResult;
import dev.kairos.ui.api.input.KeyAction;
import dev.kairos.ui.api.input.PointerAction;
import dev.kairos.ui.api.input.PointerEvent;
import dev.kairos.ui.api.input.UiKeyEvent;
import dev.kairos.ui.api.theme.ThemeTokens;
import dev.kairos.ui.components.model.ModuleCatalog;
import dev.kairos.ui.components.scene.ModernWorkbench;
import dev.kairos.ui.components.scene.PanelDesktop;
import dev.kairos.ui.components.scene.WorkbenchState;
import dev.kairos.ui.core.input.InputDispatcher;
import dev.kairos.ui.core.node.UiNode;
//#if MC>=11600
//$$ import dev.kairos.ui.platform.modern.Modern120InputAdapter;
//$$ import net.minecraft.client.gui.GuiGraphics;
//$$ import net.minecraft.client.gui.screens.Screen;
//$$ import net.minecraft.network.chat.Component;
//#else
import dev.kairos.ui.platform.legacy.Legacy112InputAdapter;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
//#endif

public final class KairosScreen
    //#if MC>=11600
    //$$ extends Screen
    //#else
    extends GuiScreen
    //#endif
{
    private final ModernWorkbench workbench;
    private final PanelDesktop panels;
    private UiNode scene;
    private InputDispatcher input;
    private boolean panelMode;
    //#if MC>=11600
    //$$ private final Modern120InputAdapter modernInput = new Modern120InputAdapter();
    //#endif

    public KairosScreen() {
        //#if MC>=11600
        //$$ super(Component.literal("Kairos UI"));
        //#endif
        ModuleCatalog catalog = KairosCatalog.create();
        WorkbenchState state = new WorkbenchState();
        state.setSelectedCategoryId("combat");
        state.setSelectedModuleId("kill-aura");
        workbench = new ModernWorkbench(catalog, state, ThemeTokens.kairosDark());
        panels = new PanelDesktop(catalog, ThemeTokens.kairosDark());
        switchScene(false);
    }

    private void switchScene(boolean usePanels) {
        panelMode = usePanels;
        scene = usePanels ? panels : workbench;
        input = new InputDispatcher(scene);
        layoutScene();
    }

    private void layoutScene() {
        if (width <= 0 || height <= 0) return;
        if (panelMode) scene.layout(new Rect(0f, 0f, width, height));
        else {
            float contentWidth = Math.min(1060f, Math.max(600f, width - 24f));
            float contentHeight = Math.min(600f, Math.max(360f, height - 24f));
            scene.layout(new Rect((width - contentWidth) * 0.5f, (height - contentHeight) * 0.5f,
                contentWidth, contentHeight));
        }
    }

    //#if MC>=11600
    //$$ @Override protected void init() { layoutScene(); }
    //$$ @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    //$$     scene.renderTree(new MinecraftFallbackCanvas(graphics));
    //$$ }
    //$$ @Override public boolean mouseClicked(double x, double y, int button) {
    //$$     return input.dispatch(modernInput.pointer(x, y, button, 1, 0d)) != EventResult.IGNORED;
    //$$ }
    //$$ @Override public boolean mouseReleased(double x, double y, int button) {
    //$$     return input.dispatch(modernInput.pointer(x, y, button, 0, 0d)) != EventResult.IGNORED;
    //$$ }
    //$$ @Override public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
    //$$     return input.dispatch(modernInput.pointer(x, y, -1, -1, 0d)) != EventResult.IGNORED;
    //$$ }
    //$$ @Override public boolean mouseScrolled(double x, double y, double delta) {
    //$$     return input.dispatch(modernInput.pointer(x, y, -1, -1, delta)) != EventResult.IGNORED;
    //$$ }
    //$$ @Override public boolean keyPressed(int key, int scanCode, int modifiers) {
    //$$     if (key == KairosMod.getOpenKeyCode()) { onClose(); return true; }
    //$$     if (key == 295) { switchScene(!panelMode); return true; }
    //$$     EventResult result = input.dispatch(modernInput.key(key, scanCode, 1, modifiers));
    //$$     if (result != EventResult.IGNORED) return true;
    //$$     return super.keyPressed(key, scanCode, modifiers);
    //$$ }
    //$$ @Override public boolean keyReleased(int key, int scanCode, int modifiers) {
    //$$     return input.dispatch(modernInput.key(key, scanCode, 0, modifiers)) != EventResult.IGNORED;
    //$$ }
    //$$ @Override public boolean charTyped(char character, int modifiers) {
    //$$     return input.dispatch(modernInput.character(character, modifiers)) != EventResult.IGNORED;
    //$$ }
    //$$ @Override public boolean isPauseScreen() { return false; }
    //#else
    @Override public void initGui() { layoutScene(); }
    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        scene.renderTree(new MinecraftFallbackCanvas());
    }
    @Override protected void mouseClicked(int x, int y, int button) throws IOException {
        input.dispatch(new PointerEvent(x, y, button, 0f, PointerAction.DOWN));
    }
    @Override protected void mouseReleased(int x, int y, int state) {
        input.dispatch(new PointerEvent(x, y, state, 0f, PointerAction.UP));
    }
    @Override protected void mouseClickMove(int x, int y, int button, long elapsed) {
        input.dispatch(new PointerEvent(x, y, button, 0f, PointerAction.MOVE));
    }
    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            ScaledResolution scaled = new ScaledResolution(Minecraft.getMinecraft());
            Legacy112InputAdapter adapter = new Legacy112InputAdapter(Minecraft.getMinecraft().displayHeight,
                scaled.getScaleFactor());
            input.dispatch(adapter.pointer(Mouse.getEventX(), Mouse.getEventY(), -1, false, wheel));
        }
    }
    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == KairosMod.getOpenKeyCode()) {
            Minecraft.getMinecraft().displayGuiScreen(null);
            return;
        }
        if (keyCode == 64) { switchScene(!panelMode); return; }
        EventResult result = input.dispatch(new UiKeyEvent(keyCode, keyCode, 0, typedChar, KeyAction.DOWN));
        if (result == EventResult.IGNORED) super.keyTyped(typedChar, keyCode);
    }
    @Override public boolean doesGuiPauseGame() { return false; }
    //#endif
}
