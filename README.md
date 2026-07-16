# Kairos UI Engine

Version-independent retained-mode UI foundations for a modern Minecraft client UI.

Kairos ships a finished modern default rather than an unstyled widget kit: blue-black
glass surfaces, violet controls, compact floating categories, a three-column workbench,
animated toast notifications, a right-aligned ModuleList, draggable HUD widgets, a dense
competitive TargetHUD, and entity plus world-object ESP. Client authors can replace every
ClickGUI palette, combat-HUD profile, ESP style, font ID, radius, spacing, blur and motion
token without forking a component.

The engine supports both accepted Kairos compositions: a fixed three-column workspace
with category navigation, module cards, and settings edited in context; and a floating
category-panel desktop that keeps traditional ClickGUI muscle memory. They share the
same module model, visual primitives, typography, glass effects, and theme tokens.

## Design constraints

- Public core targets Java 8 bytecode.
- `ui-api`, `ui-core`, and `ui-components` contain no Minecraft, Forge, Fabric, LWJGL,
  GLFW, `MatrixStack`, `PoseStack`, or `GuiGraphics` references.
- Minecraft lifecycle, coordinates, input, textures, framebuffers, and GL state live
  behind `platform-api`.
- The render core emits semantic drawing operations. Platform renderers decide how
  those operations are batched and submitted.
- Mode-specific settings use visibility rules and remain inside the selected module.

## Implemented modules

| Module | Responsibility |
| --- | --- |
| `ui-api` | Geometry, theme tokens, drawing and input contracts |
| `ui-core` | Node tree, layout, focus, pointer capture and animation |
| `ui-components` | Settings, workbench, panel desktop, modern HUD and competitive combat HUD |
| `ui-esp` | Entity/world-object snapshots, 2D projection, native 3D sink, boxes, health, equipment and tags |
| `platform-api` | Screen, clock and render host boundary |
| `ui-render-opengl` | Frame planning, shape batches, font atlas packing and GLSL 1.20 resources |
| `ui-preview-awt` / `ui-preview-svg` | Deterministic headless visual preview backends |
| `platform-1.12.2-forge` | Tested LWJGL2 coordinate, key and scissor conversion |
| `platform-1.20.1-common` | Tested GLFW coordinate, key and scissor conversion |
| `minecraft-build` | Real EGT multi-version Forge screen/bootstrap for 1.12.2 and 1.20.1 |
| `examples/modern-clickgui` | Executable dual-layout and HUD reference scenes |

The Minecraft build opens/closes with `Right Ctrl`; `F6` switches between the fixed
workbench and floating panels. `.kairos gui`, `!kairos gui`, `/kairos gui`, and other
punctuation prefixes are recognized by the standalone bridge. Theme commands are:

```text
.kairos themes
.kairos theme arctic-glass
.kairos themes reload
```

Custom theme files live in `.minecraft/kairos-ui/themes/*.properties`; the selected
theme is persisted. Consuming clients can call `KairosMod.openGui()` or
`KairosMod.handleCommand(message, prefix)` from an existing command manager.

The Forge 1.12.2 `0.3.0` playable preview now uses a persistent live module manager.
Sprint, AutoJump, AutoRespawn, FastPlace, FullBright, PlayerESP, HUD, ModuleList,
Coordinates, and Notifications are connected to Forge tick/render events and toggled by
the same module objects displayed by Kairos. This is a clean-room Forge implementation;
no LiquidBounce GPL source is copied into the repository.

The endpoint compatibility canvas now preserves rounded geometry, tint, font scaling,
scissor and interaction without shaders. The OpenGL pipeline remains the enhanced path
for actual framebuffer blur, custom font atlases and batched SDF shapes.

## Preview truth

The PNG/SVG files are rendered by the same Java scene classes, models and `UiCanvas`
operations used by the endpoints; they are not AI mockups. Layout, colors, content,
sorting, clipping and render-command structure therefore reflect code that exists.
AWT uses a deterministic block-world backdrop and a CPU blur approximation. Entity and
item slots are intentionally blank: native pixels require the endpoint to bind
`GameVisualRenderer`; the layout and all surrounding pixels are already the production
scene. See [docs/PREVIEWS.md](docs/PREVIEWS.md) for the exact parity matrix.

## Guides

- [Theme packs and hot switching](docs/THEMING.md)
- [ESP data, matrices and renderer integration](docs/ESP.md)
- [Architecture and platform boundaries](docs/ARCHITECTURE.md)

## Verify without Gradle

The shared engine has no external runtime dependencies. On a JDK:

```bash
./scripts/verify.sh
```

The script compiles with `--release 8`, runs engine, component, renderer and endpoint
mapping tests, checks both preprocessor branches, scans shared sources for forbidden
platform imports, and emits PNG/SVG previews under `out/verify/previews`.

## Minecraft endpoint build

`minecraft-build` is independent so Forge mappings and downloads cannot destabilize
the Java-8 engine build. With Gradle 9.2 running on JDK 21 plus JDK 8 and JDK 17
available as compile toolchains:

```bash
gradle -p minecraft-build :1.12.2-forge:build :1.20.1-forge:build
```

GitHub Actions performs endpoint compilation in addition to the dependency-free engine verification.
