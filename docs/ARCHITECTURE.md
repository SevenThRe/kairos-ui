# Architecture decisions

## Product surface

The concept image and the pasted design notes disagree about the primary layout.
The concept image is authoritative for milestone one:

1. left category navigation;
2. center module-card list;
3. right settings inspector with in-context mode-specific settings;
4. shared design tokens for ClickGUI and HUD widgets.

Legacy draggable category panels may be added later as another scene composition.
They must reuse the same modules, settings, components, renderer, and theme.

## Boundary rule

Dependencies point inward:

```text
Minecraft/loader -> platform adapter -> platform-api
                                      -> ui-components -> ui-core -> ui-api
```

Core code cannot import Minecraft or a loader. A CI source scan enforces the initial
rule; later it should be replaced with an ArchUnit test.

## Rendering

The UI records high-level commands such as rounded rectangle, text, image and clip.
The first 1.12.2 renderer will translate them into an OpenGL 2.1-compatible batch.
Modern renderers may use newer submission APIs, but visual semantics stay identical.

Capability levels:

- Level 0: opaque/translucent rectangles, texture font, scissor;
- Level 1: GLSL 1.20 SDF rounded shapes and shadows;
- Level 2: framebuffer-backed shared blur;
- Level 3: optional instanced/batched enhancements.

Missing capabilities affect effects, never layout or interaction.

## Fonts

Font loading belongs to the platform/render boundary. Text measurement is exposed
through `UiCanvas` so layout never depends on Minecraft's font renderer. Inter and a
CJK fallback will be packaged only after their font licenses and atlas strategy are
recorded.

## Settings

`UiSetting` owns value access and a visibility predicate. A mode switch invalidates
the selected module's setting view and changes the visible rows in place. It never
creates another settings window.
