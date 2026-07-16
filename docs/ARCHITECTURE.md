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

Core code cannot import Minecraft or a loader. The MCEF host stays in `minecraft-build`.
A CI source scan enforces the initial
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
scissor handling lives in thin modules. `minecraft-build` uses Essential Gradle Toolkit
preprocessing for one screen/bootstrap source tree and currently compiles Forge 1.12.2
and Forge 1.20.1 endpoints. Intermediate versions are added as graph nodes/platform
islands; they do not fork the UI implementation.
