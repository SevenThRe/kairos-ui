# Roadmap

## M0 — repository skeleton (complete)

- Java 8 API boundary
- retained node tree and basic layouts
- deterministic animation
- pointer capture and focus
- version-independent module/settings model
- fixed three-column Kairos WebView workspace
- command-recording renderer and smoke tests

## M1 — interaction and dual composition (complete)

- bundled HTML/CSS/JavaScript ClickGUI
- search, focus, pointer capture, z-order, collapse and clipped scrolling
- boolean, number, enum, range, multi-select, color, text and keybind settings
- movable HUD scene and reference widgets

## M2 — render framework (complete)

- raster font-atlas packing
- GLSL 1.20 SDF shapes
- ordered shape/text/image dispatch
- one-capture shared Kawase blur planning with capability fallback
- SVG and raster preview backends plus visual acceptance checks

## M3 — endpoint framework (complete)

- LWJGL2/GLFW input conversion and framebuffer scissor mapping tests
- Forge 1.12.2 and Forge 1.20.1 EGT screen/bootstrap source
- Java 8/17 endpoint CI compilation
- strict MCEF WebView host with no native fallback

## M4 — client visual systems (complete)

- modern compact floating-panel theme and interactive inline settings
- typed notification queue with enter/exit/progress lifecycle
- content-sized, sorted ModuleList with name/suffix colors
- hot-switchable theme registry, three built-ins and user theme persistence
- shared ESP entity/projector/style renderer with matrix projection tests
- Right Ctrl plus prefix-aware GUI and theme commands

## M5 — consuming client integration

- bind `UiModule`/`UiSetting` to the consuming client's real module/value registry
- connect `CommandRenderer` to the consuming client's GL state and texture managers
- package licensed Inter/JetBrains Mono/CJK fonts and icon assets
- persist panel and HUD layout through the consuming client's config service
- implement each endpoint's live entity collector and camera-matrix provider for ESP
- add 1.16.5/1.18.2 islands or Fabric endpoints when a distribution actually needs them
