# Architecture decisions

## Product surface

The engine exposes both accepted compositions over the same data model:

1. left category navigation;
2. center module-card list;
3. right settings inspector with in-context mode-specific settings;
4. shared design tokens for ClickGUI and HUD widgets.

`ModernWorkbench` implements that fixed layout. `PanelDesktop` provides draggable,
collapsible, z-ordered category windows with inline module expansion, clipped scrolling,
pointer capture, and interactive boolean/enum/number/range controls. HUD widgets reuse
the same theme and can be moved independently. `ThemeRegistry` hot-switches all bound
scenes and `ThemeDirectory` loads/persists user packs without Minecraft leaking inward.

## Boundary rule

Dependencies point inward: Minecraft/loader → platform adapter → `platform-api` →
`ui-components` → `ui-core` → `ui-api`.

Core code cannot import Minecraft or a loader. A CI source scan enforces the initial
rule; later it should be replaced with an ArchUnit test.

## Rendering

The UI records high-level commands such as rounded rectangle, text, image and clip.
`CommandRenderer` preserves order, batches adjacent shapes, routes text/images to
dedicated draws, and executes at most one shared blur capture per frame.
Modern renderers may use newer submission APIs, but visual semantics stay identical.

Capability levels:

- Level 0: opaque/translucent rectangles, texture font, scissor;
- Level 1: GLSL 1.20 SDF rounded shapes and shadows;
- Level 2: framebuffer-backed shared blur;
- Level 3: optional instanced/batched enhancements.

Missing capabilities affect effects, never layout or interaction.

## Fonts

Font loading belongs to the platform/render boundary. `FontAtlasBuilder` packs measured
glyph bitmaps without a Minecraft dependency. Inter, JetBrains Mono and a CJK fallback
remain theme font IDs so clients can supply licensed files or their own font provider.

## ESP

`ui-esp` consumes immutable entity snapshots and a `WorldToScreenProjector`. It projects
all eight AABB corners before drawing a clipped 2D overlay. Minecraft endpoints own
entity filtering, interpolation, camera-relative matrices and render-event timing; the
style and drawing code remain shared.

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
