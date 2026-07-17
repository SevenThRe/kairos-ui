// Copyright (c) 2014 The Chromium Embedded Framework Authors.
// Modified by montoyo for MCEF and by Kairos for non-blocking frame handoff.
// Use of this source code is governed by the BSD license in MCEF's LICENSE file.

package org.cef.browser;

import dev.kairos.ui.web.DirtyRect;
import dev.kairos.ui.web.PixelFrameMailbox;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import net.montoyo.mcef.MCEF;
import net.montoyo.mcef.api.IBrowser;
import net.montoyo.mcef.api.IStringVisitor;
import net.montoyo.mcef.client.ClientProxy;
import net.montoyo.mcef.client.StringVisitor;
import net.montoyo.mcef.utilities.Log;
import org.cef.CefClient;
import org.cef.DummyComponent;
import org.cef.callback.CefDragData;
import org.cef.handler.CefRenderHandler;
import org.lwjgl.input.Keyboard;

/** Off-screen CEF browser with a three-slot packed damage mailbox. */
public class CefBrowserOsr extends CefBrowser_N implements CefRenderHandler, IBrowser {
    private final CefRenderer renderer_;
    private final PixelFrameMailbox frameMailbox_ = new PixelFrameMailbox();
    private final Rectangle browser_rect_ = new Rectangle(0, 0, 1, 1);
    private final Point screenPoint_ = new Point(0, 0);
    private final boolean isTransparent_;
    private final DummyComponent dc_ = new DummyComponent();
    private MouseEvent lastMouseEvent = new MouseEvent(dc_, MouseEvent.MOUSE_MOVED, 0, 0, 0, 0, 0, false);

    public static boolean CLEANUP = true;

    CefBrowserOsr(CefClient client, String url, boolean transparent, CefRequestContext context) {
        this(client, url, transparent, context, null, null);
    }

    private CefBrowserOsr(CefClient client, String url, boolean transparent,
                          CefRequestContext context, CefBrowserOsr parent, Point inspectAt) {
        super(client, url, context, parent, inspectAt);
        isTransparent_ = transparent;
        renderer_ = new CefRenderer(transparent);
    }

    @Override public void createImmediately() { createBrowserIfRequired(false); }
    @Override public Component getUIComponent() { return dc_; }
    @Override public CefRenderHandler getRenderHandler() { return this; }

    @Override
    protected CefBrowser_N createDevToolsBrowser(CefClient client, String url,
            CefRequestContext context, CefBrowser_N parent, Point inspectAt) {
        return new CefBrowserOsr(client, url, isTransparent_, context, this, inspectAt);
    }

    @Override public Rectangle getViewRect(CefBrowser browser) { return browser_rect_; }

    @Override
    public Point getScreenPoint(CefBrowser browser, Point viewPoint) {
        Point screenPoint = new Point(screenPoint_);
        screenPoint.translate(viewPoint.x, viewPoint.y);
        return screenPoint;
    }

    @Override
    public void onPopupShow(CefBrowser browser, boolean show) {
        if (!show) {
            renderer_.clearPopupRects();
            invalidate();
        }
    }

    @Override public void onPopupSize(CefBrowser browser, Rectangle size) { renderer_.onPopupSize(size); }

    @Override
    public void onPaint(CefBrowser browser, boolean popup, Rectangle[] dirtyRects,
                        ByteBuffer buffer, int width, int height) {
        if (popup) return;

        List<DirtyRect> damage = new ArrayList<DirtyRect>();
        if (dirtyRects != null) {
            for (Rectangle rect : dirtyRects) {
                if (rect != null) damage.add(new DirtyRect(rect.x, rect.y, rect.width, rect.height));
            }
        }
        try {
            frameMailbox_.publish(buffer, width, height, damage, false);
        } catch (RuntimeException exception) {
            Log.warning("Kairos skipped invalid CEF paint data: " + exception.getMessage());
        }
    }

    /** Consumed by MCEF ClientProxy on RenderTickEvent.START. */
    public void mcefUpdate() {
        PixelFrameMailbox.Lease lease = frameMailbox_.acquireLatest();
        if (lease != null) {
            try {
                renderer_.onFrame(lease.frame());
            } finally {
                lease.close();
            }
        }
        sendMouseEvent(lastMouseEvent);
    }

    @Override public void onCursorChange(CefBrowser browser, int cursorType) { }
    @Override public boolean startDragging(CefBrowser browser, CefDragData dragData, int mask, int x, int y) { return false; }
    @Override public void updateDragCursor(CefBrowser browser, int operation) { }

    private void createBrowserIfRequired(boolean hasParent) {
        if (getNativeRef("CefBrowser") == 0) {
            if (getParentBrowser() != null) {
                createDevTools(getParentBrowser(), getClient(), 0, true, isTransparent_, null, getInspectAt());
            } else {
                createBrowser(getClient(), 0, getUrl(), true, isTransparent_, null, getRequestContext());
            }
        } else {
            setFocus(true);
        }
    }

    @Override
    public void close() {
        if (CLEANUP) {
            ((ClientProxy) MCEF.PROXY).removeBrowser(this);
            renderer_.cleanup();
        }
        super.close(true);
    }

    @Override
    public void resize(int width, int height) {
        browser_rect_.setBounds(0, 0, width, height);
        dc_.setBounds(browser_rect_);
        dc_.setVisible(true);
        wasResized(width, height);
    }

    @Override public void draw(double x1, double y1, double x2, double y2) { renderer_.render(x1, y1, x2, y2); }
    @Override public int getTextureID() { return renderer_.texture_id_[0]; }

    @Override
    public void injectMouseMove(int x, int y, int mods, boolean left) {
        MouseEvent event = new MouseEvent(dc_, MouseEvent.MOUSE_MOVED, 0, mods, x, y, 0, false);
        lastMouseEvent = event;
        sendMouseEvent(event);
    }

    @Override
    public void injectMouseButton(int x, int y, int mods, int button, boolean pressed, int clickCount) {
        MouseEvent event = new MouseEvent(dc_, pressed ? MouseEvent.MOUSE_PRESSED : MouseEvent.MOUSE_RELEASED,
            0, mods, x, y, clickCount, false, button);
        sendMouseEvent(event);
    }

    @Override public void injectKeyTyped(char character, int mods) {
        sendKeyEvent(new KeyEvent(dc_, KeyEvent.KEY_TYPED, 0, mods, 0, character));
    }

    public static int remapKeycode(int keyCode, char character) {
        switch (keyCode) {
            case Keyboard.KEY_BACK: return 0x08;
            case Keyboard.KEY_DELETE: return 0x2E;
            case Keyboard.KEY_DOWN: return 0x28;
            case Keyboard.KEY_RETURN: return 0x0D;
            case Keyboard.KEY_ESCAPE: return 0x1B;
            case Keyboard.KEY_LEFT: return 0x25;
            case Keyboard.KEY_RIGHT: return 0x27;
            case Keyboard.KEY_TAB: return 0x09;
            case Keyboard.KEY_UP: return 0x26;
            case Keyboard.KEY_PRIOR: return 0x21;
            case Keyboard.KEY_NEXT: return 0x22;
            case Keyboard.KEY_END: return 0x23;
            case Keyboard.KEY_HOME: return 0x24;
            default: return (int) character;
        }
    }

    @Override public void injectKeyPressedByKeyCode(int keyCode, char character, int mods) {
        sendKeyEvent(new KeyEvent(dc_, KeyEvent.KEY_PRESSED, 0, mods,
            remapKeycode(keyCode, character), character));
    }

    @Override public void injectKeyReleasedByKeyCode(int keyCode, char character, int mods) {
        sendKeyEvent(new KeyEvent(dc_, KeyEvent.KEY_RELEASED, 0, mods,
            remapKeycode(keyCode, character), character));
    }

    @Override public void injectMouseWheel(int x, int y, int mods, int amount, int rotation) {
        sendMouseWheelEvent(new MouseWheelEvent(dc_, MouseEvent.MOUSE_WHEEL, 0, mods, x, y, 0, false,
            MouseWheelEvent.WHEEL_UNIT_SCROLL, amount, rotation));
    }

    @Override public void runJS(String script, String frame) { executeJavaScript(script, frame, 0); }
    @Override public void visitSource(IStringVisitor visitor) { getSource(new StringVisitor(visitor)); }
    @Override public boolean isPageLoading() { return isLoading(); }
}
