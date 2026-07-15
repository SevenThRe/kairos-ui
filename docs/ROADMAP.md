# Roadmap

## M0 — repository skeleton (complete)

- Java 8 API boundary
- retained node tree and basic layouts
- deterministic animation
- pointer capture and focus
- version-independent module/settings model
- fixed three-column Kairos workbench scene
- command-recording renderer and smoke tests

## M1 — interaction and dual composition (complete)

- fixed workbench and draggable category panels
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
- compatibility renderer and F6 dual-layout switch

## M4 — production client integration

- bind `UiModule`/`UiSetting` to the consuming client's real module/value registry
- connect `CommandRenderer` to the consuming client's GL state and texture managers
- package licensed Inter/JetBrains Mono/CJK fonts and icon assets
- persist theme, panel and HUD layout through the consuming client's config service
- add 1.16.5/1.18.2 islands or Fabric endpoints when a distribution actually needs them
