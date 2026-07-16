# Kairos UI Engine

Version-independent retained-mode UI foundations for a modern Minecraft client UI.

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
| `ui-components` | Settings, workbench, draggable panel desktop and HUD scenes |
| `platform-api` | Screen, clock and render host boundary |
| `ui-render-opengl` | Frame planning, shape batches, font atlas packing and GLSL 1.20 resources |
| `ui-preview-awt` / `ui-preview-svg` | Deterministic headless visual preview backends |
| `platform-1.12.2-forge` | Tested LWJGL2 coordinate, key and scissor conversion |
| `platform-1.20.1-common` | Tested GLFW coordinate, key and scissor conversion |
| `minecraft-build` | Real EGT multi-version Forge screen/bootstrap for 1.12.2 and 1.20.1 |
| `examples/modern-clickgui` | Executable dual-layout and HUD reference scenes |

The Minecraft build opens/closes with `Right Ctrl`; `F6` switches between the fixed
workbench and floating panels. `.kairos gui`, `!kairos gui`, `/kairos gui`, and other
punctuation prefixes are recognized by the standalone bridge. Consuming clients can
call `KairosMod.openGui()` from their own prefix-aware command manager. It includes a
compatibility canvas so both endpoints remain usable
without shaders. The OpenGL pipeline is the production path for SDF corners, atlas
text and shared blur; its final Minecraft GL-state binding remains isolated from UI code.

## Verify without Gradle

The repository has no external runtime dependencies in this milestone. On a JDK:

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
