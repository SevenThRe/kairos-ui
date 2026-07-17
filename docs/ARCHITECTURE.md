# Architecture decisions

## Product surface

The production ClickGUI exposes one WebView composition over the shared data model:

1. left category navigation;
2. center module-card list;
3. right settings inspector with in-context mode-specific settings;
4. shared design tokens for ClickGUI and HUD widgets.

Bundled HTML/CSS/JavaScript implements the layout. `KairosWebBridge` serializes the live
module catalog, accepts validated toggle/setting actions, and schedules mutations on the
client thread. `KairosResourceScheme` exposes only the bundled asset directory and one
explicit user theme file. HUD widgets continue to use native rendering and `ThemeRegistry`.
The deleted Minecraft canvas is not a fallback path; WebView failure is reported directly.

## Boundary rule

Dependencies point inward: Minecraft/loader → platform adapter → `platform-api` →
`ui-components` → `ui-core` → `ui-api`.

Core code cannot import Minecraft or a loader. `web-surface-core` defines `WebSurface`,
damage planning and the thread handoff without importing CEF, Minecraft or LWJGL. The
concrete MCEF/LiquidBounce host stays in `integrations/liquidbounce-1.12.2`. A CI source scan enforces the initial
rule; later it should be replaced with an ArchUnit test.

## Rendering

Native HUD/ESP scenes record high-level commands such as rounded rectangle, text, image and clip.
`CommandRenderer` preserves order, batches adjacent shapes, routes text/images to
dedicated draws, and executes at most one shared blur capture per frame.
Modern renderers may use newer submission APIs, but visual semantics stay identical.

Capability levels:

- Level 0: opaque/translucent rectangles, texture font, scissor;
- Level 1: GLSL 1.20 SDF rounded shapes and shadows;
- Level 2: framebuffer-backed shared blur;
- Level 3: optional instanced/batched enhancements.

The ClickGUI does not use this capability ladder: Chromium owns its layout and text,
while the endpoint owns the Minecraft framebuffer blur behind its transparent surface.

### Browser pixel path

The Java 8 endpoint embeds the pinned 1.12.2 MCEF runtime, but replaces MCEF's stock OSR
staging and uploader classes. CEF's paint callback supplies a full BGRA view plus damage
rectangles. Kairos clamps and coalesces those rectangles, copies only their rows into a
packed three-slot mailbox, and keeps the newest frame. When a waiting frame is replaced,
its damage is merged into the new frame so the GL texture cannot miss an update. Damage
above 42% of the surface or a resize becomes one full upload.

The render tick is the sole GPU consumer. It uses `glTexImage2D` for allocation/full
frames and packed `glTexSubImage2D` calls for partial frames, restoring texture binding
and every modified `GL_UNPACK_*` value. The CEF callback never calls OpenGL and the GL
thread never waits while the producer copies pixels.

CEF popup surfaces (including HTML select menus) use a separate mailbox and are clipped
and composited into the main texture. Closing a popup discards its pending frame and
invalidates the page so the area underneath is repainted.

`KairosStateSync` scans module state at 20 Hz and sends revisioned Java-to-JavaScript
patches. This covers toggles made by keybinds or commands, not only clicks originating in
the page. Value changes request a full bridge refresh at 4 Hz. JavaScript-to-Java calls
remain origin checked, whitelisted and scheduled onto Minecraft's client thread.

## Fonts

Font loading belongs to the platform/render boundary. `FontAtlasBuilder` packs measured
glyph bitmaps without a Minecraft dependency. Inter, JetBrains Mono and a CJK fallback
remain theme font IDs so clients can supply licensed files or their own font provider.

## ESP

`ui-esp` consumes immutable entity and world-object snapshots plus a
`WorldToScreenProjector`. It projects all eight AABB corners before drawing a clipped 2D
overlay. A separate `WorldOverlaySink` performs native 3D AABB submission. Minecraft
endpoints own entity filtering, interpolation, camera-relative matrices, native target
entity/item resolution, depth policy, and render-event timing; style and layout stay shared.

## Settings

`UiSetting` owns value access and a visibility predicate. A mode switch invalidates
the selected module's setting view and changes the visible rows in place. It never
creates another settings window.

## Version strategy

The shared engine is Java 8 bytecode. Endpoint-specific event, key, coordinate and
scissor handling lives in thin modules. The production 1.12.2 island is the pinned GPL
LiquidBounce integration. Later Forge/Fabric islands can reuse the UI protocol and Web
assets without presenting the old standalone demo client as a production endpoint.
